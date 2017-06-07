package com.genepoint.lbsshow.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.genepoint.custom.Action;
import com.genepoint.custom.Configs;
import com.genepoint.custom.Status;
import com.genepoint.dao.DBUtil;
import com.genepoint.dao.JDBC;
import com.genepoint.lbsshow.service.ShopService;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;

import redis.clients.jedis.Jedis;

public class ShopServiceImpl implements ShopService {
	public static Map<String, ShopInfo> shopMap = new HashMap<>();
	private Connection dbConn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	static {
		try {
			loadShopData();
		} catch (Exception e) {
			Log.trace(ShopServiceImpl.class.getClass(), e);
		}
	}

	@Override
	public void getData(HttpServletRequest request, JSONObject result) {
		String action = request.getParameter("action");
		switch (action) {
		case Action.ACITON_GET_SHOP_LIST:
			getShopList(request, result);
			break;
		case Action.ACITON_GET_SHOP_RANK:
			getShopRankV2(request, result);
			break;
		case Action.ACTION_GET_RATE_IN_SHOP:
			getRateInShopV2(request, result);
			break;
		case Action.ACTION_GET_STAYTIME_IN_SHOP:
			getStayTimeInShopV2(request, result);
			break;
		default:
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			result.put("status", Status.STATUS_UNKNOWN_ACTION);
			result.put("message", Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			break;
		}
	}

	public void getShopList(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			JSONArray shopArray = new JSONArray();
			for (String shopId : shopMap.keySet()) {
				ShopInfo info = shopMap.get(shopId);
				if (info != null) {
					if (!info.building.equals(building)) {
						continue;
					}
					JSONObject shop = new JSONObject();
					shop.put("id",shopId.split("_")[1]);
					shop.put("name", info.shopName);
					shop.put("floor", info.floor);
					shopArray.put(shop);
				}
			}
			JSONObject obj = new JSONObject();
			obj.put("shopList", shopArray);
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


	public void getShopRankV2(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			String action = request.getParameter("action");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			long time = json.getLong("time") * 1000L;

			if (Configs.CACHE_ENABLE) {
				String key = action + "_" + building + "_" + time;
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
			String sql = "select shop_id,count,count_inshop,stay_time_average from inshop_by_day where time=? and building=?";
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setLong(1, time);
			pstmt.setString(2, building);
			rs = pstmt.executeQuery();
			List<ShopBean> list = new ArrayList<>();
			while(rs.next()){
				int shopId = rs.getInt(1);
				int countInShop = rs.getInt(3);
				int count = rs.getInt(2);
				int averageStayTime = rs.getInt(4);
				list.add(new ShopBean(building,shopId, countInShop, count, averageStayTime, null));
			}
			dbConn.close();
			dbConn = null;
			list.sort(new Comparator<ShopBean>() {
				public int compare(ShopBean o1, ShopBean o2) {
					return o2.countMAC - o1.countMAC;
				}
			});
			JSONArray shopArray = new JSONArray();
			for (ShopBean bean : list) {
				JSONObject obj = new JSONObject();
				obj.put("id", bean.shopId);
				obj.put("name", shopMap.get(building+"_"+bean.shopId).shopName);
				obj.put("floor", shopMap.get(building+"_"+bean.shopId).floor);
				if (bean.countMAC == 0 || bean.countMACInShop == 0) {
					obj.put("rate", "0.0");
				} else {
					obj.put("rate", String.format("%.2f", bean.countMACInShop * 1.0 / bean.countMAC));
				}
				obj.put("stayTime", bean.averageStayTime);
				obj.put("flowSize", bean.countMAC);
				shopArray.put(obj);
			}
			result.put("status", Status.STATUS_SUCCESS);
			result.put("data", shopArray);
			if (Configs.CACHE_ENABLE) {
				String key = action + "_" + building + "_" + time;
				Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
				redis.select(Configs.REDIS_DB_INDEX);
				redis.set(key, shopArray.toString());
				redis.expire(key, Configs.CACHE_TIMEOUT);
				redis.close();
			}
			Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
		} catch (Exception e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			Log.trace(this.getClass(), e);
			return;
		}
	}


	public void getRateInShopV2(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			int shopId = json.getInt("shopId");
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
			dbConn.setAutoCommit(true);
			if (dbConn != null) {
				JSONArray arr = new JSONArray();
				String sql = "select count,count_inshop,time from inshop_by_day where shop_id=?  and building=? and time>=? and time<=? order by time";
				pstmt = dbConn.prepareStatement(sql);
				pstmt.setInt(1, shopId);
				pstmt.setString(2, building);
				pstmt.setLong(3, timeTail);
				pstmt.setLong(4, timeNow);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					JSONObject obj = new JSONObject();
					obj.put("time", sdf.format(new Date(rs.getLong(3))));
					int  count = rs.getInt(1);
					int countInShop = rs.getInt(2);
					int rate = 0;
					if(count!=0 && countInShop!=0){
						rate = (int)(countInShop *1.0/count * 100);
					}
					obj.put("rate", rate);
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
			}
		} catch (JSONException e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
			Log.trace(ShopServiceImpl.class.getClass(), e);
		} catch (SQLException e) {
			Log.trace(ShopServiceImpl.class.getClass(), e);
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}



	public void getStayTimeInShopV2(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			if (data == null || data.equals("")) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			int shopId = json.getInt("shopId");
			String building = json.getString("building");
			long time = json.getLong("time") * 1000L;
			dbConn = DBUtil.getConnection();
			dbConn.setAutoCommit(true);
			if (dbConn != null) {
				String sql = "select stay_time_type from inshop_by_day where shop_id=? and time=? and building=? limit 1";
				pstmt = dbConn.prepareStatement(sql);
				pstmt.setInt(1, shopId);
				pstmt.setLong(2, time);
				pstmt.setString(3, building);
				rs = pstmt.executeQuery();
				JSONObject obj = new JSONObject();
				boolean flag = false;
				while (rs.next()) {
					obj.put("typeNumList", rs.getString(1));
					flag = true;
				}
				rs.close();
				dbConn.close();
				if (flag) {
					result.put("status", Status.STATUS_SUCCESS);
					result.put("data", obj);
					Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
				} else {
					result.put("status", Status.STATUS_EMPTY);
					result.put("message", Status.getMessage(Status.STATUS_EMPTY));
					Log.info(this.getClass(), Status.getMessage(Status.STATUS_EMPTY));
				}
			}
		} catch (JSONException e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
			Log.trace(ShopServiceImpl.class.getClass(), e);
		} catch (SQLException e) {
			Log.trace(ShopServiceImpl.class.getClass(), e);
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}


	public static void loadShopData() throws Exception {
		Connection dbConn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			dbConn = DBUtil.getConnection();
			dbConn.setAutoCommit(true);
			if (dbConn != null) {
				String sql = "select building,floor,shop_id,shop_name,description,path from shop_list";
				pstmt = dbConn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					ShopInfo info = new ShopInfo(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5), rs.getString(6));
					shopMap.put(rs.getString(1)+"_"+rs.getInt(3), info);
				}
				rs.close();
				pstmt.close();
				dbConn.close();
			}
		} catch (SQLException e) {
			Log.trace(ShopServiceImpl.class.getClass(), e);
			e.printStackTrace();
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

//	public ShopBean statistic(int shopId, Connection dbConn) {
//		try {
//			String sql = "select mac,time from track_inshop_20160808 where shop_id=" + shopId;
//			pstmt = dbConn.prepareStatement(sql);
//			rs = pstmt.executeQuery();
//			Map<String, List<Long>> map = new HashMap<>();
//			while (rs.next()) {
//				String mac = rs.getString(1);
//				List<Long> list = map.get(mac);
//				if (list == null) {
//					list = new ArrayList<Long>();
//					map.put(mac, list);
//				}
//				list.add(rs.getLong(2));
//			}
//			rs.close();
//			int countMACInShop = 0;
//			int countMAC = map.size();
//			long totalStayTime = 0;
//			int[] stayTimeType = new int[6];
//			for (String mac : map.keySet()) {
//				List<Long> list = map.get(mac);
//				int index = 0;
//				long timeStart = list.get(0);
//				long timeEnd = list.get(0);
//				long stayTime = 0;
//				for (Long time : list) {
//					if (index == 0) {
//						index++;
//						continue;
//					}
//					// 某个MAC前后两次出现在店铺的时差超过10分钟则中断计时
//					if (time - timeEnd > 1000 * 600) {
//						stayTime += timeEnd - timeStart;
//						timeStart = time;
//						timeEnd = time;
//					} else {
//						timeEnd = time;
//					}
//					index++;
//				}
//				stayTime += timeEnd - timeStart;
//				if (stayTime > 3 * 60 * 1000) {
//					countMACInShop++;
//					totalStayTime += stayTime;
//					if (stayTime >= 0 && stayTime <= 5 * 60 * 1000) {
//						stayTimeType[0]++;
//					} else if (stayTime > 5 * 60 * 1000 && stayTime <= 10 * 60 * 1000) {
//						stayTimeType[1]++;
//					} else if (stayTime > 10 * 60 * 1000 && stayTime <= 20 * 60 * 1000) {
//						stayTimeType[2]++;
//					} else if (stayTime > 20 * 60 * 1000 && stayTime <= 30 * 60 * 1000) {
//						stayTimeType[3]++;
//					} else if (stayTime > 30 * 60 * 1000 && stayTime <= 60 * 60 * 1000) {
//						stayTimeType[4]++;
//					} else {
//						stayTimeType[5]++;
//					}
//				}
//			}
//			if (countMACInShop == 0) {
//				return new ShopBean(shopId, 0, countMAC, 0, stayTimeType);
//			} else {
//				return new ShopBean(shopId, countMACInShop, countMAC, totalStayTime / 1000 / 60 / countMACInShop, stayTimeType);
//			}
//		} catch (SQLException e) {
//			Log.trace(ShopServiceImpl.class.getClass(), e);
//			e.printStackTrace();
//		}
//		return null;
//	}

	public static void main(String[] args) {
		int[] shopList = { 20005, 30029, 10039, 10022, 30005, 10007, 30006, 10005, 10002, 10011, 30004, 10028, 10012, 10020, 10036, 10009, 10035,
				10021, 10040, 30009 };
		for (Integer shopId : shopList) {
			new Thread() {
				public void run() {
					JDBC db = new JDBC();
					// statistic(shopId);
				}
			}.start();
		}
	}
}

class ShopBean {
	public int shopId;
	public int countMACInShop;
	public int countMAC;
	public long averageStayTime;
	public double rateInShop;
	public int[] stayTimeType;
	public String building;

	public ShopBean(String builbing,int shopId, int countMACInShop, int countMAC, long averageStayTime, int[] stayTimeType) {
		this.building=builbing;
		this.shopId = shopId;
		this.countMACInShop = countMACInShop;
		this.countMAC = countMAC;
		this.averageStayTime = averageStayTime;
		if (countMAC == 0 || countMACInShop == 0) {
			this.rateInShop = 0.0;
		} else {
			this.rateInShop = countMACInShop * 1.0 / countMAC;
		}
		this.stayTimeType = stayTimeType;
	}
}
