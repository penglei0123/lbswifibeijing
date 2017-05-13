package com.genepoint.lbsshow.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.genepoint.custom.Action;
import com.genepoint.custom.Configs;
import com.genepoint.custom.Status;
import com.genepoint.dao.DBUtil;
import com.genepoint.lbsshow.service.TrackService;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;

public class TrackServiceImplBak implements TrackService {

	private Connection dbConn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
	public static List<HotPosition> hotPositions = new ArrayList<HotPosition>();

	@Override
	public void getData(HttpServletRequest request, JSONObject result) {
		String action = request.getParameter("action");
		switch (action) {
		case Action.ACTION_GET_ALL_ONLINE_USER:
			getOnlineUserList(request, result);
			break;
		case Action.ACTION_GET_ALL_OFFLINE_USER:
			getOfflineUserList(request, result);
			break;
		case Action.ACTION_GET_TRACK_REALTIME:
			getRealtimeData(request, result);
			break;
		case Action.ACTION_GET_TRACK_HISTORY:
			getHistoryList(request, result);
			break;
		case Action.ACTION_GET_TRACK_HISTORY_DETAIL:
			getHistoryTrackDetail(request, result);
			break;
		case Action.ACTION_GET_TRACK_TREND:
			getTrackTrend(request, result);
			break;
		case Action.ACTION_GET_HEATMAP_TREND:
			getHeatMapTrend(request, result);
			break;
		default:
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			result.put("status", Status.STATUS_UNKNOWN_ACTION);
			result.put("message", Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			break;
		}
	}

	public void getOnlineUserList(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
			redis.select(Configs.REDIS_DB_INDEX);
			Map<String, String> users = redis.hgetAll("user_hashset");
			JSONArray arr = new JSONArray();
			for (String user : users.keySet()) {
				JSONObject obj = new JSONObject();
				obj.put("mac", user);
				arr.put(obj);
			}
			redis.close();
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
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			return;
		}
	}

	public void getOfflineUserList(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			dbConn = DBUtil.getConnection();
			// 确定查询的表名
			String table = "track_20160819";
			String sqlStr = "select mac,count(mac) as size from " + table + " where building=? group by mac order by size desc";
			pstmt = dbConn.prepareStatement(sqlStr);
			pstmt.setString(1, building);
			rs = pstmt.executeQuery();
			JSONArray arr = new JSONArray();
			int max = 100;
			int size = 0;
			while (rs.next()) {
				size++;
				if (size >= max) {
					break;
				}
				String mac = rs.getString(1);
				int count = rs.getInt(2);
				// System.out.println(mac + "\t" + count);
				arr.put(mac);
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
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			return;
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

	public void getRealtimeData(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			String mac = json.getString("mac");
			Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
			redis.select(Configs.REDIS_DB_INDEX);
			List<String> posList = redis.lrange(mac, 0, Configs.TRACK_WINDOW_SIZE - 1);
			Map<String, String> users = redis.hgetAll("user_hashset");
			JSONArray userArr = new JSONArray();
			for (String user : users.keySet()) {
				JSONObject obj = new JSONObject();
				obj.put("mac", user);
				userArr.put(obj);
			}
			redis.close();
			result.put("userList", userArr);
			JSONArray arr = new JSONArray();
			if (posList != null) {
				for (String pos : posList) {
					JSONObject obj = new JSONObject(pos);
					if (obj.getString("building").equals(building))
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
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			return;
		}
	}

	public void getHistoryList(HttpServletRequest request, JSONObject result) {
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
			String floor = json.getString("floor");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			String mac = json.getString("mac");
			dbConn = DBUtil.getConnection();
			// 判断时间跨度为几天（确定查询的表名）
			List<String> tableList = Function.parseTablenameList(dbConn, "track_", timeNow, timeTail);
			// 对用户轨迹按照楼层划分，生成列表返回
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				String sqlStr = "select time from " + table
						+ " where building=? and floor=? and mac=? and time between ? and ? order by time asc";
				pstmt = dbConn.prepareStatement(sqlStr);
				pstmt.setString(1, building);
				pstmt.setString(2, floor);
				pstmt.setString(3, mac);
				pstmt.setLong(4, timeTail);
				pstmt.setLong(5, timeNow);
				rs = pstmt.executeQuery();
				long timeStart = 0, timeEnd = 0;
				while (rs.next()) {
					long time = rs.getLong(1);
					if(timeEnd==0){
						timeStart = time;
						timeEnd = time;
					}
					// 连续两次定位结果之间相差超过10分钟则认为离开商场
					if ((time - timeEnd) > 1000 * 60 * 10) {
						JSONObject obj = new JSONObject();
						obj.put("floor", floor);
						obj.put("startTime", sdf.format(new Date(timeStart)));
						obj.put("startTimestamp", timeStart);
						obj.put("endTime", sdf.format(new Date(timeEnd)));
						obj.put("endTimestamp", timeEnd);
						arr.put(obj);
						timeStart = time;
					}
					timeEnd = time;
				}
				rs.close();
				pstmt.close();
				JSONObject obj = new JSONObject();
				obj.put("floor", floor);
				obj.put("startTime", sdf.format(new Date(timeStart)));
				obj.put("startTimestamp", timeStart);
				obj.put("endTime", sdf.format(new Date(timeEnd)));
				obj.put("endTimestamp", timeEnd);
				arr.put(obj);
			}
			dbConn.close();
			System.out.println(arr.toString());
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

	/**
	 * @deprecated
	 * @param request
	 * @param result
	 */
	public void getHistoryListOld(HttpServletRequest request, JSONObject result) {
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
			String mac = json.getString("mac");
			dbConn = DBUtil.getConnection();
			// 判断时间跨度为几天（确定查询的表名）
			List<String> tableList = Function.parseTablenameList(dbConn, "track_", timeNow, timeTail);
			// 对用户轨迹按照楼层划分，生成列表返回
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				String sqlStr = "select floor,time from " + table + " where building=? and mac=? and time between ? and ? order by time asc";
				pstmt = dbConn.prepareStatement(sqlStr);
				pstmt.setString(1, building);
				pstmt.setString(2, mac);
				pstmt.setLong(3, timeTail);
				pstmt.setLong(4, timeNow);
				rs = pstmt.executeQuery();
				long timeStart = 0, timeEnd = 0;
				String lastFloor = null;
				while (rs.next()) {
					String floor = rs.getString(1);
					long time = rs.getLong(2);
					if (!floor.equals(lastFloor)) {
						if (lastFloor != null) {
							JSONObject obj = new JSONObject();
							obj.put("floor", lastFloor);
							obj.put("startTime", sdf.format(new Date(timeStart)));
							obj.put("startTimestamp", timeStart);
							obj.put("endTime", sdf.format(new Date(timeEnd)));
							obj.put("endTimestamp", timeEnd);
							arr.put(obj);
						}
						lastFloor = floor;
						timeStart = time;
						timeEnd = timeStart;
					} else {
						// 连续两次定位结果之间相差超过10分钟则认为离开商场
						if ((time - timeEnd) > 1000 * 60 * 10) {
							JSONObject obj = new JSONObject();
							obj.put("floor", lastFloor);
							obj.put("startTime", sdf.format(new Date(timeStart)));
							obj.put("startTimestamp", timeStart);
							obj.put("endTime", sdf.format(new Date(timeEnd)));
							obj.put("endTimestamp", timeEnd);
							arr.put(obj);
							obj = new JSONObject();
							obj.put("floor", "null");
							obj.put("startTime", sdf.format(new Date(timeEnd)));
							obj.put("startTimestamp", timeEnd);
							obj.put("endTime", sdf.format(new Date(time)));
							obj.put("endTimestamp", time);
							arr.put(obj);
							timeStart = time;
						}
						timeEnd = time;
					}
				}
				rs.close();
				pstmt.close();
				if (lastFloor != null) {
					JSONObject obj = new JSONObject();
					obj.put("floor", lastFloor);
					obj.put("startTime", sdf.format(new Date(timeStart)));
					obj.put("startTimestamp", timeStart);
					obj.put("endTime", sdf.format(new Date(timeEnd)));
					obj.put("endTimestamp", timeEnd);
					arr.put(obj);
				}
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

	public void getHistoryTrackDetail(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			System.out.println(data);
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			String floor = json.getString("floor");
			long startTime = json.getLong("startTime");
			long endTime = json.getLong("endTime");
			String mac = json.getString("mac");
			dbConn = DBUtil.getConnection();
			// 确定查询的表名
			String table = "track_" + sdfDate.format(new Date(endTime));
			String sqlStr = "select corx,cory from " + table + " where building=? and floor=? and mac=? and time between ? and ? order by time asc";
			pstmt = dbConn.prepareStatement(sqlStr);
			pstmt.setString(1, building);
			pstmt.setString(2, floor);
			pstmt.setString(3, mac);
			pstmt.setLong(4, startTime);
			pstmt.setLong(5, endTime);
			rs = pstmt.executeQuery();
			int count = DBUtil.getCounts(rs);
			// 从总记录中挑选若干条
			int targetCount = 100;
			int step = 1;
			if (count > targetCount) {
				step = count / targetCount;
			}
			rs.first();
			int index = 0;
			JSONArray arr = new JSONArray();
			while (rs.next()) {
				index++;
				if (index % step != 0)
					continue;
				int x = (int) rs.getDouble(1);
				int y = (int) rs.getDouble(2);
				// double minDis = Double.MAX_VALUE;
				// HotPosition targetPosition = null;
				// for (HotPosition p : hotPositions) {
				// double dis = Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
				// if (dis < minDis) {
				// targetPosition = p;
				// minDis = dis;
				// }
				// }
				// x = (int) targetPosition.x;
				// y = (int) targetPosition.y;
				JSONObject obj = new JSONObject();
				obj.put("corx", x);
				obj.put("cory", y);

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

	// 获取所有用户轨迹趋势
	public void getTrackTrend(HttpServletRequest request, JSONObject result) {
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
			String floor = json.getString("floor");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			int stepSize = json.getInt("stepSize");
			dbConn = DBUtil.getConnection();
			// 判断时间跨度为几天（确定查询的表名）
			List<String> tableList = Function.parseTablenameList(dbConn, "track_", timeNow, timeTail);
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				String sqlStr = "select mac,corx,cory,time from " + table
						+ " where building=? and floor=? and time between ? and ? order by time asc";
				pstmt = dbConn.prepareStatement(sqlStr);
				pstmt.setString(1, building);
				pstmt.setString(2, floor);
				pstmt.setLong(3, timeTail);
				pstmt.setLong(4, timeNow);
				rs = pstmt.executeQuery();
				long timeStart = 0;
				JSONArray arrGroup = null;
				// 记录窗口内MAC出现的次数
				Map<String, Integer> countMap = null;
				// 记录窗口内MAC最后一次的位置
				Map<String, JSONObject> posMap = null;
				while (rs.next()) {
					String mac = rs.getString(1);
					long time = rs.getLong(4);
					if (timeStart == 0) {
						timeStart = time;
						arrGroup = new JSONArray();
						countMap = new HashMap<>();
						posMap = new HashMap<>();
					}
					JSONObject obj = new JSONObject();
					obj.put("x", rs.getInt(2));
					obj.put("y", rs.getInt(3));

					if (time - timeStart > stepSize * 1000L) {
						for (String key : countMap.keySet()) {
							// System.out.println(key + "\t" + countMap.get(key));
							if (countMap.get(key) > 0) {
								arrGroup.put(posMap.get(key));
							}
						}
						System.out.println(arrGroup.length());
						JSONObject tmp = new JSONObject();
						tmp.put("data", arrGroup);
						tmp.put("time", time);
						arr.put(tmp);
						arrGroup = new JSONArray();
						arrGroup.put(obj);
						timeStart = time;

						countMap.clear();
						countMap.put(mac, 1);
						posMap.clear();
						posMap.put(mac, obj);
					} else {
						countMap.put(mac, countMap.getOrDefault(mac, 0) + 1);
						posMap.put(mac, obj);
					}
				}
				rs.close();
				pstmt.close();
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

	// 获取所有热力图趋势
	public void getHeatMapTrend(HttpServletRequest request, JSONObject result) {
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
			String floor = json.getString("floor");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			int stepSize = json.getInt("stepSize");
			dbConn = DBUtil.getConnection();
			// 判断时间跨度为几天（确定查询的表名）
			List<String> tableList = Function.parseTablenameList(dbConn, "track_", timeNow, timeTail);
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				String sqlStr = "select mac,corx,cory,time from " + table
						+ " where building=? and floor=? and time between ? and ? order by time asc";
				pstmt = dbConn.prepareStatement(sqlStr);
				pstmt.setString(1, building);
				pstmt.setString(2, floor);
				pstmt.setLong(3, timeTail);
				pstmt.setLong(4, timeNow);
				rs = pstmt.executeQuery();
				long timeStart = 0;
				JSONArray arrGroup = null;
				// 记录窗口内某网格命中的次数
				Map<String, Integer> countMap = null;
				long time = 0;
				while (rs.next()) {
					time = rs.getLong(4);
					if (timeStart == 0) {
						timeStart = time;
						arrGroup = new JSONArray();
						countMap = new HashMap<>();
					}
					int x = (int) rs.getInt(2);
					int y = (int) rs.getInt(3);
					x = x / Configs.MAP_GRID_SIZE * Configs.MAP_GRID_SIZE;
					y = y / Configs.MAP_GRID_SIZE * Configs.MAP_GRID_SIZE;
					String keyStr = x + "_" + y;

					if (time - timeStart > stepSize * 1000L) {
						for (String key : countMap.keySet()) {
							String[] tmpArr = key.split("_");
							JSONObject obj = new JSONObject();
							obj.put("x", Integer.parseInt(tmpArr[0]));
							obj.put("y", Integer.parseInt(tmpArr[1]));
							obj.put("value", countMap.get(key));
							arrGroup.put(obj);
						}
						JSONObject tmp = new JSONObject();
						tmp.put("data", arrGroup);
						tmp.put("time", time);
						arr.put(tmp);
						arrGroup = new JSONArray();
						timeStart = time;
						countMap.clear();
						countMap.put(keyStr, 1);
					} else {
						countMap.put(keyStr, countMap.getOrDefault(keyStr, 0) + 1);
					}
				}
				rs.close();
				pstmt.close();
				for (String key : countMap.keySet()) {
					String[] tmpArr = key.split("_");
					JSONObject obj = new JSONObject();
					obj.put("x", Integer.parseInt(tmpArr[0]));
					obj.put("y", Integer.parseInt(tmpArr[1]));
					obj.put("value", countMap.get(key));
					arrGroup.put(obj);
				}
				System.out.println(arrGroup.length());
				JSONObject tmp = new JSONObject();
				tmp.put("data", arrGroup);
				tmp.put("time", time);
				arr.put(tmp);
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
}
