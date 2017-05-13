package com.genepoint.lbsshow.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.genepoint.custom.Action;
import com.genepoint.custom.Configs;
import com.genepoint.custom.Status;
import com.genepoint.dao.DBUtil;
import com.genepoint.lbsshow.service.HeatmapService;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;
import com.genepoint.tool.PositionConvertUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class HeatmapServiceImpl implements HeatmapService {
	private Connection dbConn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	@Override
	public void getData(HttpServletRequest request, JSONObject result) {
		String action = request.getParameter("action");
		switch (action) {
		case Action.ACTION_GET_HEATMAP_REALTIME:
			getRealtimeData(request, result);
			break;
		case Action.ACTION_GET_HEATMAP_HISTORY:
			getHistoryDataV2(request, result);
			break;
		default:
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			result.put("status", Status.STATUS_UNKNOWN_ACTION);
			result.put("message", Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			break;
		}
	}

	public void getRealtimeData(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			String floor = json.getString("floor");
			Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
			redis.select(Configs.REDIS_DB_INDEX);
			Map<String, String> users = redis.hgetAll(building + "_user_hashset");

			Pipeline pipeline = redis.pipelined();
			List<Response<String>> posList = new ArrayList<>();
			for (String user : users.keySet()) {
				posList.add(pipeline.get(building + "_" + user));
			}
			pipeline.sync();
			redis.close();
			JSONObject obj = null;
			JSONArray arr = new JSONArray();
			for (Response<String> r : posList) {
				if (r == null)
					continue;
				String pos = r.get();
				if (pos == null)
					continue;
				obj = new JSONObject(pos);
				if (obj.getString("building").equals(building) && obj.getString("floor").equals(floor)) {
					double[] newPos = PositionConvertUtil.convert(building, obj.getDouble("corx"), obj.getDouble("cory"));
					obj.put("corx", newPos[0]);
					obj.put("cory", newPos[1]);
					arr.put(obj);
				}
			}
			if (arr.length() > 0) {
				result.put("status", Status.STATUS_SUCCESS);
				result.put("data", arr);
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
			} else {
				result.put("status", Status.STATUS_EMPTY);
				result.put("message", Status.getMessage(Status.STATUS_EMPTY));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_EMPTY));
			}
		} catch (Exception e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			// Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			Log.trace(this.getClass(), e);
			return;
		}
	}

	public void getHistoryData(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			String action = request.getParameter("action");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			String floor = json.getString("floor");
			long duration = json.getInt("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			if (Configs.CACHE_ENABLE) {
				String key = action + "_" + building + "_" + floor + "_" + timeTail + "_" + timeNow;
				Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
				redis.select(Configs.REDIS_DB_INDEX);
				String value = redis.get(key);
				redis.close();
				if (value != null) {
					JSONArray arr = new JSONArray(value);
					result.put("status", Status.STATUS_SUCCESS);
					result.put("data", arr);
					Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
					return;
				}
			}
			// 判断时间跨度为几天（确定查询的表名）
			dbConn = DBUtil.getConnection();
			List<String> tableList = Function.parseTablenameList(dbConn, "track_" + building + "_", timeNow, timeTail);
			HashMap<String, Integer> hashMap = new HashMap<String, Integer>(100 * 100);
			for (String table : tableList) {
				String sqlStr = "select corx,cory from `" + table + "` where floor=? and time between ? and ?";
				pstmt = dbConn.prepareStatement(sqlStr);
				pstmt.setString(1, floor);
				pstmt.setLong(2, timeTail);
				pstmt.setLong(3, timeNow);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					int x = (int) rs.getDouble(1);
					int y = (int) rs.getDouble(2);
					x = x / Configs.MAP_GRID_SIZE * Configs.MAP_GRID_SIZE;
					y = y / Configs.MAP_GRID_SIZE * Configs.MAP_GRID_SIZE;
					String keyStr = x + "_" + y;
					if (hashMap.containsKey(keyStr)) {
						hashMap.put(keyStr, hashMap.get(keyStr) + 1);
					} else {
						hashMap.put(keyStr, 1);
					}
				}
				rs.close();
				pstmt.close();
			}
			dbConn.close();
			dbConn = null;
			List<Map.Entry<String, Integer>> sortList = new ArrayList<>();
			sortList.addAll(hashMap.entrySet());
			sortList.sort(new Comparator<Map.Entry<String, Integer>>() {
				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			});
			int size = sortList.size();
			size = size > Configs.HEATMAP_MAX_POSITION_COUNT ? Configs.HEATMAP_MAX_POSITION_COUNT : size;
			JSONArray arr = new JSONArray();
			for (int i = 0; i < size; i++) {
				String[] xyStr = sortList.get(i).getKey().split("_");
				int value = sortList.get(i).getValue();
				JSONObject obj = new JSONObject();
				obj.put("x", Integer.parseInt(xyStr[0]));
				obj.put("y", Integer.parseInt(xyStr[1]));
				obj.put("value", value);
				arr.put(obj);
				// System.out.println(xyStr[0] + "\t" + xyStr[1] + "\t" + value);
			}
			hashMap.clear();
			hashMap = null;
			sortList.clear();
			sortList = null;
			if (arr.length() >= 0) {
				result.put("status", Status.STATUS_SUCCESS);
				result.put("data", arr);
				if (Configs.CACHE_ENABLE) {
					String key = action + "_" + building + "_" + floor + "_" + timeTail + "_" + timeNow;
					Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
					redis.select(Configs.REDIS_DB_INDEX);
					redis.set(key, arr.toString());
					redis.expire(key, Configs.CACHE_TIMEOUT);
					redis.close();
				}
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
			} else {
				result.put("status", Status.STATUS_EMPTY);
				result.put("message", Status.getMessage(Status.STATUS_EMPTY));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_EMPTY));
			}
		} catch (Exception e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR) + "：" + e.getMessage());
			Log.trace(this.getClass(), e);
			return;
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

	public void getHistoryDataV2(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			String action = request.getParameter("action");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			String floor = json.getString("floor");
			long duration = json.getInt("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			if (Configs.CACHE_ENABLE) {
				String key = action + "_" + building + "_" + floor + "_" + timeTail + "_" + timeNow;
				Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
				redis.select(Configs.REDIS_DB_INDEX);
				String value = redis.get(key);
				redis.close();
				if (value != null) {
					JSONArray arr = new JSONArray(value);
					result.put("status", Status.STATUS_SUCCESS);
					result.put("data", arr);
					Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
					return;
				}
			}
			// 判断时间跨度为几天（确定查询的表名）
			dbConn = DBUtil.getConnection();
			List<String> tableList = Function.parseTablenameList(dbConn, "heatmap_by_minutes_", timeNow, timeTail);
			HashMap<String, Integer> hashMap = new HashMap<String, Integer>(100 * 100);
			for (String table : tableList) {
				String sqlStr = "select xy,sum(value) as value from `" + table
						+ "` where building=? and floor=? and time_start >= ? and time_end<=? group by xy";
				pstmt = dbConn.prepareStatement(sqlStr);
				pstmt.setString(1, building);
				pstmt.setString(2, floor);
				pstmt.setLong(3, timeTail);
				pstmt.setLong(4, timeNow);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					hashMap.put(rs.getString(1), rs.getInt(2));
				}
				rs.close();
				pstmt.close();
			}
			dbConn.close();
			dbConn = null;
			List<Map.Entry<String, Integer>> sortList = new ArrayList<>();
			sortList.addAll(hashMap.entrySet());
			sortList.sort(new Comparator<Map.Entry<String, Integer>>() {
				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			});
			int size = sortList.size();
			size = size > Configs.HEATMAP_MAX_POSITION_COUNT ? Configs.HEATMAP_MAX_POSITION_COUNT : size;
			JSONArray arr = new JSONArray();
			for (int i = 0; i < size; i++) {
				String[] xyStr = sortList.get(i).getKey().split("_");
				int value = sortList.get(i).getValue();
				JSONObject obj = new JSONObject();
				double[] newPos = PositionConvertUtil.convert(building, Double.parseDouble(xyStr[0]), Double.parseDouble(xyStr[1]));
				obj.put("x", newPos[0]);
				obj.put("y", newPos[1]);
				obj.put("value", value);
				arr.put(obj);
				// System.out.println(xyStr[0] + "\t" + xyStr[1] + "\t" + value);
			}
			hashMap.clear();
			hashMap = null;
			sortList.clear();
			sortList = null;
			if (arr.length() > 0) {
				result.put("status", Status.STATUS_SUCCESS);
				result.put("data", arr);
				if (Configs.CACHE_ENABLE) {
					String key = action + "_" + building + "_" + floor + "_" + timeTail + "_" + timeNow;
					Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
					redis.select(Configs.REDIS_DB_INDEX);
					redis.set(key, arr.toString());
					redis.expire(key, Configs.CACHE_TIMEOUT);
					redis.close();
				}
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
			} else {
				result.put("status", Status.STATUS_EMPTY);
				result.put("message", Status.getMessage(Status.STATUS_EMPTY));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_EMPTY));
			}
		} catch (Exception e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR) + "：" + e.getMessage());
			Log.trace(this.getClass(), e);
			return;
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}
}