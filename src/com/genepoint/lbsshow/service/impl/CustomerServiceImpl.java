package com.genepoint.lbsshow.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.genepoint.custom.Action;
import com.genepoint.custom.Configs;
import com.genepoint.custom.Status;
import com.genepoint.dao.DBUtil;
import com.genepoint.lbsshow.service.CustomerService;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;
import com.genepoint.tool.PositionConvertUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class CustomerServiceImpl implements CustomerService {
	private Connection dbConn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private ExecutorService exec = Executors.newFixedThreadPool(10);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public void getData(HttpServletRequest request, JSONObject result) {
		String action = request.getParameter("action");
		switch (action) {
		case Action.ACTION_GET_PERSON_REALTIME:
			getRealtimeData(request, result);
			break;
		case Action.ACTION_GET_REALTIME_CUSTOMER_SHOP:
			getRealtimeCustomerAndShopData(request, result);
			break;
		case Action.ACTION_GET_CUSTOMER_FLOW_HISTORY:
			getHistoryCustomerFlowV3(request, result);
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
			JSONArray arr = new JSONArray();
			Pipeline pipeline = redis.pipelined();
			List<Response<String>> posList = new ArrayList<>();
			for (String user : users.keySet()) {			
				posList.add(pipeline.get(building + "_" + user));
			}
			pipeline.sync();
			redis.close();
			JSONObject obj = null;
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
		//		System.out.println("arr:"+arr+"--------------------------");
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
			} else {
				result.put("status", Status.STATUS_EMPTY);
				result.put("message", Status.getMessage(Status.STATUS_EMPTY));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_EMPTY));
			}
		} catch (Exception e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			return;
		}
	}

	public void getRealtimeCustomerAndShopData(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
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
			int count = 0;
			Map<Integer, Integer> activeShopMap = new HashMap<Integer, Integer>();
			for (Response<String> r : posList) {
				if (r == null)
					continue;
				String pos = r.get();
				if (pos == null)
					continue;
				obj = new JSONObject(pos);
				if (obj.getString("building").equals(building)) {
					activeShopMap.put(obj.getInt("shopId"), activeShopMap.getOrDefault(obj.getInt("shopId"), 0) + 1);
					count++;
				}
			}

			// int count = 0;
			// Map<Integer, Integer> activeShopMap = new HashMap<Integer, Integer>();
			// for (String user : users.keySet()) {
			// long size = redis.llen(user);
			// long p = size == 0L ? 0 : size - 1;
			// String pos = redis.lindex(user, p);
			// if (pos != null) {
			// JSONObject obj = new JSONObject(pos);
			// if (obj.getString("building").equals(building)) {
			// activeShopMap.put(obj.getInt("shopId"), activeShopMap.getOrDefault(obj.getInt("shopId"), 0) + 1);
			// count++;
			// }
			// }
			// }
			// redis.close();
			List<Map.Entry<Integer, Integer>> list = new ArrayList<>();
			list.addAll(activeShopMap.entrySet());
			list.sort(new Comparator<Map.Entry<Integer, Integer>>() {
				@Override
				public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			});
			int maxShopSize = list.size() > 10 ? 10 : list.size();
			JSONArray shopRank = new JSONArray();
			for (int i = 0; i < maxShopSize; i++) {
				int shopId = list.get(i).getKey();
				int activeNum = list.get(i).getValue();
				ShopInfo info = ShopServiceImpl.shopMap.get(shopId);
				if (info != null) {
					JSONObject shop = new JSONObject();
					shop.put("name", info.shopName);
					shop.put("floor", info.floor);
					shop.put("activeNum", activeNum);
					shopRank.put(shop);
				}
			}
			obj = new JSONObject();
			obj.put("count", count);
			obj.put("shopRank", shopRank);
			result.put("status", Status.STATUS_SUCCESS);
			result.put("data", obj);
			Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
		} catch (Exception e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			return;
		}
	}

	// 统计商场客流
	public void getHistoryCustomerFlow(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			dbConn = DBUtil.getConnection();
			// 判断时间跨度为几天（确定查询的表名）
			List<String> tableList = Function.parseTablenameList(dbConn, "track_" + building + "_", timeNow, timeTail);
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				long start = System.currentTimeMillis();
				String sqlStr = "select count(*) from (select DISTINCT mac from `" + table + "`) as t";
				pstmt = dbConn.prepareStatement(sqlStr);
				rs = pstmt.executeQuery();
				rs.next();
				int count = rs.getInt(1);
				rs.close();
				pstmt.close();
				JSONObject obj = new JSONObject();
				obj.put("time", table.substring(6));
				obj.put("count", count);
				arr.put(obj);
				long end = System.currentTimeMillis();
				System.out.println(count + "\t" + (end - start));
			}
			dbConn.close();
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
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR) + "：" + e.getMessage());
			Log.trace(this.getClass(), e);
			return;
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

	// 统计商场客流(以天为单位，不允许细化到小时、分钟，统计粒度为N小时，N可设置，默认为1)
	public void getHistoryCustomerFlowV2(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			dbConn = DBUtil.getConnection();
			List<String> tableList = Function.parseTablenameList(dbConn, "track_" + building + "_", timeNow, timeTail);
			dbConn.close();
			List<Map.Entry<Long, Future<Integer>>> fs = new ArrayList<>();
			Map<Long, Future<Integer>> map = new HashMap<>();
			for (String table : tableList) {
				String dateStr = table.split("_")[1];
				long timeStartOneDay = sdf.parse(dateStr + " 00:00:00").getTime();
				for (int i = 0; i < 24; i++) {
					long timeStartRange = timeStartOneDay + i * 1000 * 60 * 60 * 1;
					long timeEndRange = timeStartRange + 1000 * 60 * 60 * 1 - 1000;
					System.out.println(timeStartRange + "\t" + timeEndRange);
					System.out.println(sdf2.format(new Date(timeStartRange)) + "\t" + sdf2.format(new Date(timeEndRange)));
					AsyncDoStatisticCallable callable = new AsyncDoStatisticCallable(table, timeStartRange, timeEndRange);
					Future<Integer> f = exec.submit(callable);
					map.put(timeEndRange, f);
				}
			}
			fs.addAll(map.entrySet());

			List<JSONObject> list = new ArrayList<JSONObject>();
			for (Map.Entry<Long, Future<Integer>> f : fs) {
				int count = f.getValue().get();
				long time = f.getKey();
				JSONObject obj = new JSONObject();
				obj.put("time", sdf2.format(new Date(time)));
				obj.put("timeStamp", time);
				obj.put("count", count);
				list.add(obj);
			}
			Collections.sort(list, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					return (int) (o1.getLong("timeStamp") - o2.getLong("timeStamp"));
				}
			});
			JSONArray arr = new JSONArray();
			for (JSONObject obj : list) {
				arr.put(obj);
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
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR) + "：" + e.getMessage());
			Log.trace(this.getClass(), e);
			return;
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

	public void getHistoryCustomerFlowV3(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			dbConn = DBUtil.getConnection();
			String sql = "select value,time_end from flow_by_hour where building=? and  time_start>=? and time_end<=? order by time_start";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setString(1, building);
			pstmt.setLong(2, timeTail);
			pstmt.setLong(3, timeNow);
			rs = pstmt.executeQuery();
			JSONArray arr = new JSONArray();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				obj.put("time", sdf2.format(new Date(rs.getLong(2))));
				obj.put("count", rs.getInt(1));
				arr.put(obj);
			}
			rs.close();
			pstmt.close();
			dbConn.close();
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
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR) + "：" + e.getMessage());
			Log.trace(this.getClass(), e);
			return;
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}
}

class AsyncDoStatisticCallable implements Callable<Integer> {
	private String table;
	private long timeStart;
	private long timeEnd;

	public AsyncDoStatisticCallable(String table, long start, long end) {
		this.table = table;
		this.timeStart = start;
		this.timeEnd = end;
	}

	@Override
	public Integer call() throws Exception {
		Connection dbConn = DBUtil.getConnection();
		String sqlStr = "select count(*) from (select DISTINCT mac from `" + table + "` where time between ? and ?) as t";
		PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
		pstmt.setLong(1, timeStart);
		pstmt.setLong(2, timeEnd);
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		int count = rs.getInt(1);
		rs.close();
		pstmt.close();
		dbConn.close();
		return count;
	}
}