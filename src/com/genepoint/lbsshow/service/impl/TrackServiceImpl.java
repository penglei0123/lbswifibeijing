package com.genepoint.lbsshow.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.genepoint.custom.Action;
import com.genepoint.custom.Configs;
import com.genepoint.custom.Status;
import com.genepoint.dao.DBUtil;
import com.genepoint.lbsshow.service.TrackService;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;
import com.genepoint.tool.PositionConvertUtil;

import redis.clients.jedis.Jedis;

public class TrackServiceImpl implements TrackService {

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
			getHistoryTrack(request, result);
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
		case Action.ACTION_GET_TOPK_MAC:
			getTopKMAC(request, result);
			break;
		case Action.ACTION_GET_MAP_PATH_FORBIDDEN_REGION:
			getMapPath(request, result);
		case Action.ACTION_GET_HISTORY_PERSON_TRACK:
			getHistoryPersonTrack(request,result);
			boolean success = false;
			if (result.getInt("status") == Status.STATUS_SUCCESS) {
				success = true;
			}
			getForbiddenList(request, result);
			if (success) {
				result.put("status", Status.STATUS_SUCCESS);
				result.put("message", Status.getMessage(Status.STATUS_SUCCESS));
			}
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
			Map<String, String> users = redis.hgetAll(building + "_user_hashset");
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
			String sqlStr = "select mac,count(mac) as size from `" + table + "` where building=? group by mac order by size desc";
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
			List<String> posList = redis.lrange(building + "_" + mac, 0, Configs.TRACK_WINDOW_SIZE - 1);
			Map<String, String> users = redis.hgetAll(building + "_user_hashset");
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
					if (obj.getString("building").equals(building)) {
						double[] newPos = PositionConvertUtil.convert(building, obj.getDouble("corx"), obj.getDouble("cory"));
						obj.put("corx", newPos[0]);
						obj.put("cory", newPos[1]);
						arr.put(obj);
					}
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

	public void getHistoryTrack(HttpServletRequest request, JSONObject result) {
		try {
			String action = request.getParameter("action");
			String data = request.getParameter("data");
			System.out.println(data+"-------"+action);
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			String floor = json.getString("floor");
			String mac = json.getString("mac");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			System.out.println("开始时间"+timeTail+"结束时间"+timeNow);
			if (Configs.CACHE_ENABLE) {
				String key = action + "_" + building + "_" + floor + "_" + mac + "_" + timeTail + "_" + timeNow;
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
			dbConn = DBUtil.getConnection();
			// 判断时间跨度为几天（确定查询的表名）
			List<String> tableList = Function.parseTablenameList(dbConn, "track_" + building + "_", timeNow, timeTail);
			System.out.println("tableList"+tableList.size());
			// 对用户轨迹按照楼层划分，生成列表返回
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				String sqlStr = "select corx,cory,time from `" + table + "` where floor=? and mac=? and time between ? and ? order by time asc";
				pstmt = dbConn.prepareStatement(sqlStr);
				// pstmt.setString(1, building);
				pstmt.setString(1, floor);
				pstmt.setString(2, mac);
				pstmt.setLong(3, timeTail);
				pstmt.setLong(4, timeNow);
				rs = pstmt.executeQuery();
				long lastTime = 0;
				JSONArray oneTrack = new JSONArray();
				while (rs.next()) {
					System.out.println("rs222:"+rs.getDouble(1));
					double x = rs.getDouble(1);// 用double以兼容经纬度和像素
					double y = rs.getDouble(2);
					double[] newPos = PositionConvertUtil.convert(building, x, y);
					long time = rs.getLong(3);
					if (lastTime == 0) {
						lastTime = time;
					}

					JSONObject obj = new JSONObject();
					obj.put("x", newPos[0]);
					obj.put("y", newPos[1]);
					obj.put("time", time);
					oneTrack.put(obj);
					/**
					 * 功能暂时取消
					 */
					// 连续两次定位结果之间相差超过10分钟则认为该段轨迹结束
					// if ((time - lastTime) > 1000 * 60 * 10) {
					// arr.put(oneTrack);
					// oneTrack = new JSONArray();
					// } else {
					// JSONObject obj = new JSONObject();
					// obj.put("x", x);
					// obj.put("y", y);
					// obj.put("time", time);
					// oneTrack.put(obj);
					// }
					lastTime = time;
				}
				rs.close();
				pstmt.close();
				if (oneTrack.length() > 0)
					arr.put(oneTrack);
			}
			dbConn.close();
			if (arr.length() > 0) {
				result.put("status", Status.STATUS_SUCCESS);
				result.put("data", arr);
				result.put("timeStart", timeTail);
				result.put("timeEnd", timeNow);
	           System.out.println("result1:"+result);
				if (Configs.CACHE_ENABLE) {
					String key = action + "_" + building + "_" + floor + "_" +  mac + "_" + timeTail + "_" + timeNow;
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

	public void getHistoryPersonTrack(HttpServletRequest request, JSONObject result) {
		try {
			String action = request.getParameter("action");
			String data = request.getParameter("data");
	//		System.out.println(data+"......"+action);
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				System.out.println("error");
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			String mac = json.getString("mac");
			long duration = json.getLong("duration");
			long timeNow, timeTail;
			double timeDuration ;
			long endTime = 0,startTime=0;
			long allTimeDuration = 0;
			if (duration == -1) {
				timeNow = json.getLong("duration_end") * 1000L;
				timeTail = json.getLong("duration_start") * 1000L;
			} else {
				timeNow = System.currentTimeMillis();
				timeTail = timeNow - duration * 1000L;
			}
			System.out.println("结束时间"+timeNow+"开始时间"+timeTail);
			/*if (Configs.CACHE_ENABLE) {
				String key = action + "_" + building + "_" + mac + "_" + timeTail + "_" + timeNow;
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
			}*/
			dbConn = DBUtil.getConnection();
			// 判断时间跨度为几天（确定查询的表名）
			List<String> tableListPerson = Function.parseTablenameList(dbConn, "track_" + building + "_", timeNow, timeTail);
		//	System.out.println("track_" + building + "_"+ timeNow+timeTail);
		//	System.out.println("tableListPerson"+tableListPerson.toString());
			// 对用户轨迹按照楼层划分，生成列表返回
			JSONArray arr = new JSONArray();
			JSONArray floorTime=new JSONArray();
			for (String table : tableListPerson) {
		//		System.out.println("tableList"+table);
				String sqlStr = "select corx,cory,time,floor from `" + table + "` where mac=? and time between ? and ? order by time asc";
				pstmt = dbConn.prepareStatement(sqlStr);
				// pstmt.setString(1, building);
				pstmt.setString(1, mac);
				pstmt.setLong(2, timeTail);
				pstmt.setLong(3, timeNow);
				rs = pstmt.executeQuery();
				boolean firstFlag=true;
				long floorStartTime = 0;
				long floorEndTime = 0;
				String lastFloor = null;
				JSONArray oneTrack = new JSONArray();
				boolean flagNull = false;
				while (rs.next()) {
					flagNull = true ;
					double x = rs.getDouble(1);// 用double以兼容经纬度和像素
					double y = rs.getDouble(2);
					double[] newPos = PositionConvertUtil.convert(building, x, y);
					long time = rs.getLong(3);
					endTime = time ;
					String floor = rs.getString(4);
					if(firstFlag){
						floorStartTime=time;
						if(floorStartTime>timeTail){
							floorStartTime = time-1000;
						}
						startTime = floorStartTime;
						lastFloor = floor;
						firstFlag=false;
					}
					JSONObject obj = new JSONObject();
					obj.put("x", newPos[0]);
					obj.put("y", newPos[1]);
					obj.put("time", time);
					obj.put("floor", floor);
					//oneTrack.put(obj);
				//	System.out.println("obj:"+obj.toString());
					/**
					 * 定位轨迹按楼层分组
					 */
					 if (!floor.equals(lastFloor)) {
						 arr.put(oneTrack);
						 oneTrack = new JSONArray();
						 oneTrack.put(obj);
						 JSONObject jsonObject=new JSONObject();
						 timeDuration = floorEndTime-floorStartTime;
						 if (timeDuration == 0) {
							 timeDuration += 1000;
						 }
						 allTimeDuration += timeDuration;
						 jsonObject.put("floor", lastFloor);
						 jsonObject.put("timeDuration",timeDuration);
						 jsonObject.put("floorEndTime", floorEndTime);
						 jsonObject.put("floorStartTime", floorStartTime+1000);
						 floorTime.put(jsonObject);
						 floorStartTime=floorEndTime;
					 } else {
						 /*
						 obj = new JSONObject();
						 obj.put("x", newPos[0]);
						 obj.put("y", newPos[1]);
						 obj.put("time", time);
						 obj.put("floor", floor);
						 */
						 oneTrack.put(obj);
					 }
					 floorEndTime=time;
					 lastFloor = floor;
				}
		//		System.out.println("floorEndTime"+floorEndTime);
				rs.close();
				pstmt.close();
                if (flagNull == true) {
                 JSONObject jsonObject=new JSONObject();
   				 jsonObject.put("floor", lastFloor);
   				 jsonObject.put("floorEndTime", floorEndTime);
   				 jsonObject.put("floorStartTime", floorStartTime+1000);
   				 timeDuration = floorEndTime-floorStartTime;
   				 if (timeDuration == 0) {
   					 timeDuration += 1000;
   				}
   				 allTimeDuration += timeDuration ;
   				 jsonObject.put("timeDuration",timeDuration);
   			     floorTime.put(jsonObject);
   				if (oneTrack.length() > 0)
   					arr.put(oneTrack);
   			}
				}
//			System.out.println("floorTime"+floorTime);
			dbConn.close();
			if (arr.length() > 0) {
                long w =0;
                double ww = 0;
                JSONArray  time = new JSONArray();
				for(int i = 0;i< floorTime.length();i++){
	                JSONObject width = new JSONObject();
					JSONObject jOb = floorTime.getJSONObject(i);
			//		System.out.println("job"+jOb.toString());
					width.put("floor", jOb.get("floor"));
					width.put("floorEndTime", jOb.get("floorEndTime"));
					width.put("floorStartTime", jOb.get("floorStartTime"));
					w = jOb.getLong("timeDuration");
					ww = w*100.0/(endTime - startTime)+308.0;
					if (ww < 308.0) {
						ww = 308.0 ;
					}
					//width.put("width", w*100.0/(endTime - startTime));
					width.put("width", ww);
					time.put(width);
				}
         //       System.out.println("time"+time.toString());
				result.put("timeQueryStart", timeTail);
				result.put("timeQueryEnd", timeNow);
				result.put("endTime", endTime);
				result.put("startTime", startTime);
				result.put("status", Status.STATUS_SUCCESS);
				result.put("data", arr);
				result.put("floorOrder", time);
			//	result.put("allTimeDuration", allTimeDuration);
	        //   System.out.println("result:"+result);
				if (Configs.CACHE_ENABLE) {
					String key = action + "_" + building + "_" +  mac + "_" + timeTail + "_" + timeNow;
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

	@Deprecated
	public void getHistoryTrackDetail(HttpServletRequest request, JSONObject result) {
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
			long startTime = json.getLong("startTime");
			long endTime = json.getLong("endTime");
			String mac = json.getString("mac");
			dbConn = DBUtil.getConnection();
			// 确定查询的表名
			String table = "track_" + sdfDate.format(new Date(endTime));
			String sqlStr = "select corx,cory from `" + table + "` where building=? and floor=? and mac=? and time between ? and ? limit 20 order by time asc";
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
			List<String> tableList = Function.parseTablenameList(dbConn, "track_" + building + "_", timeNow, timeTail);
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				String sqlStr = "select mac,corx,cory,time from `" + table + "` where floor=? and time between ? and ? order by time asc";
				pstmt = dbConn.prepareStatement(sqlStr);
				// pstmt.setString(1, building);
				pstmt.setString(1, floor);
				pstmt.setLong(2, timeTail);
				pstmt.setLong(3, timeNow);
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
					double[] newPos = PositionConvertUtil.convert(building, rs.getDouble(2), rs.getDouble(3));
					obj.put("x", newPos[0]);
					obj.put("y", newPos[1]);

					if (time - timeStart > stepSize * 1000L) {
						for (String key : countMap.keySet()) {
							// System.out.println(key + "\t" + countMap.get(key));
							if (countMap.get(key) > 0) {
								arrGroup.put(posMap.get(key));
							}
						}
						// System.out.println(arrGroup.length());
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
			List<String> tableList = Function.parseTablenameList(dbConn, "track_" + building + "_", timeNow, timeTail);
			JSONArray arr = new JSONArray();
			for (String table : tableList) {
				String sqlStr = "select mac,corx,cory,time from `" + table
						+ "` where building=? and floor=? and time between ? and ? order by time asc";
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
					int x = rs.getInt(2);
					int y = rs.getInt(3);
					x = x / Configs.MAP_GRID_SIZE * Configs.MAP_GRID_SIZE;
					y = y / Configs.MAP_GRID_SIZE * Configs.MAP_GRID_SIZE;
					String keyStr = x + "_" + y;

					if (time - timeStart > stepSize * 1000L) {
						for (String key : countMap.keySet()) {
							String[] tmpArr = key.split("_");
							JSONObject obj = new JSONObject();
							obj.put("x", Double.parseDouble(tmpArr[0]));
							obj.put("y", Double.parseDouble(tmpArr[1]));
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
					obj.put("x", Double.parseDouble(tmpArr[0]));
					obj.put("y", Double.parseDouble(tmpArr[1]));
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

	// 获取TOPKmac
	public void getTopKMAC(HttpServletRequest request, JSONObject result) {
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
			Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
			redis.select(Configs.REDIS_DB_INDEX);
			String redisKey = building + "_mac_topk_sortset";
			Set<String> macs = redis.zrevrange(redisKey, 0, 99);// 降序取出前100个mac
			redis.close();
			JSONArray arr = new JSONArray();
			if (macs != null && macs.size() > 0) {
				Iterator<String> it = macs.iterator();
				while (it.hasNext()) {
					arr.put(it.next());
				}
			} else {
				dbConn = DBUtil.getConnection();
				// 按照mac字典序
				String sqlStr = "select mac,count from mac_topk where building=? order by mac";
				pstmt = dbConn.prepareStatement(sqlStr);
				pstmt.setString(1, building);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					arr.put(rs.getString(1));
				}
				dbConn.close();
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

	public void getMapPath(HttpServletRequest request, JSONObject result) {
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
			if (building == null || "".equals(building)) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			dbConn = DBUtil.getConnection();
			String sqlStr = "select floor,path_id,start_id,end_id,start_corx,start_cory,end_corx,end_cory from map_path where building=?";
			pstmt = dbConn.prepareStatement(sqlStr);
			pstmt.setString(1, building);
			rs = pstmt.executeQuery();
			// 按楼层拆分路径数据
			Map<String, List<MapPath>> mapPaths = new HashMap<>();
			// 按楼层拆分节点数据
			Map<String, Map<String, Point>> mapNodes = new HashMap<>();
			String floor = null;
			List<MapPath> list = null;
			Map<String, Point> map = null;
			MapPath path = null;
			int pathId,startId,endId;
			double startX,startY,endX,endY;
			while (rs.next()) {
				floor = rs.getString(1);
				list = mapPaths.get(floor);
				map = mapNodes.get(floor);
				if (list == null) {
					list = new ArrayList<>();
					mapPaths.put(floor, list);
				}
				if(map==null){
					map = new HashMap<>();
					mapNodes.put(floor, map);
				}
				pathId = rs.getInt(2);
				startId = rs.getInt(3);
				endId = rs.getInt(4);
				startX = rs.getDouble(5);
				startY = rs.getDouble(6);
				endX = rs.getDouble(7);
				endY = rs.getDouble(8);
				path = new MapPath(pathId,startId,endId,startX,startY,endX,endY);
				list.add(path);
				map.put(Integer.toString(startId), new Point((float) startX, (float) startY));
				map.put(Integer.toString(endId), new Point((float) endX, (float) endY));
			}
			dbConn.close();
			if (mapPaths.size() > 0) {
				result.put("status", Status.STATUS_SUCCESS);
				result.put("mapPaths", JSON.toJSON(mapPaths));
				result.put("mapNodes", JSON.toJSON(mapNodes));
				System.out.println(JSON.toJSONString(mapPaths));
				System.out.println(JSON.toJSONString(mapNodes));
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

	public void getForbiddenList(HttpServletRequest request, JSONObject result) {
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
			if (building == null || "".equals(building)) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			dbConn = DBUtil.getConnection();
			String sqlStr = "select id,floor,description,path from forbidden_list where building=?";
			pstmt = dbConn.prepareStatement(sqlStr);
			pstmt.setString(1, building);
			rs = pstmt.executeQuery();
			// 按楼层拆分禁止区域数据
			Map<String, List<ForbiddenRegion>> mapRegions = new HashMap<>();
			String floor = null;
			List<ForbiddenRegion> list = null;
			int regionId;
			String pathStr = null;
			String desc = null;
			while (rs.next()) {
				regionId = rs.getInt(1);
				floor = rs.getString(2);
				desc = rs.getString(3);
				pathStr = rs.getString(4);
				String[] arr = pathStr.split("#");
				List<Point> points = new ArrayList<>();
				for (int i = 0; i < arr.length; i++) {
					String[] pointStr = arr[i].split(",");
					if (pointStr.length == 2) {
						points.add(new Point(Float.parseFloat(pointStr[0]), Float.parseFloat(pointStr[1])));
					}
				}
				list = mapRegions.get(floor);
				if (list == null) {
					list = new ArrayList<>();
					mapRegions.put(floor, list);
				}
				if (points.size() > 0)
					list.add(new ForbiddenRegion(regionId, points, desc));
			}
			dbConn.close();
			if (mapRegions.size() > 0) {
				result.put("status", Status.STATUS_SUCCESS);
				result.put("mapRegions", JSON.toJSON(mapRegions));
				System.out.println(JSON.toJSONString(mapRegions));
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
