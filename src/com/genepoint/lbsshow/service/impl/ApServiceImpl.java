package com.genepoint.lbsshow.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.genepoint.custom.Action;
import com.genepoint.custom.Configs;
import com.genepoint.custom.Status;
import com.genepoint.lbsshow.service.ApService;
import com.genepoint.tool.Log;
import com.genepoint.tool.PositionConvertUtil;

import redis.clients.jedis.Jedis;

public class ApServiceImpl implements ApService {
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
		case Action.ACTION_GET_AP_POSITION:
			getApPosition(request, result);
			break;
		default:
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			result.put("status", Status.STATUS_UNKNOWN_ACTION);
			result.put("message", Status.getMessage(Status.STATUS_UNKNOWN_ACTION));
			break;
		}
	}

	public void getApPosition(HttpServletRequest request, JSONObject result) {
		try {
			String data = request.getParameter("data");
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			Jedis redis = new Jedis(Configs.REDIS_HOST, Configs.REDIS_PORT);
			redis.select(Configs.REDIS_DB_INDEX);
			Map<String, String> aps = redis.hgetAll(building+"_AP_LOCATION");
			Map<String, String> aps2 = redis.hgetAll(building+"_AP_MONITOR_SCAN_NUM");
			redis.close();
			Map<String, List<JSONObject>> map = new HashMap<>();
			JSONObject t = null;
			for (String mac : aps.keySet()) {
				String values = aps.get(mac);
				String[] arr = values.split(",");
				String floor = arr[0];
				double x = Double.parseDouble(arr[1]);
				double y = Double.parseDouble(arr[2]);
				// 坐标转换插件
				double[] newPos = PositionConvertUtil.convert(building, x, y);
				List<JSONObject> list = map.get(floor);
				if (list == null) {
					list = new ArrayList<>();
				}
				t = new JSONObject();
				t.put("mac", mac);
				t.put("x", newPos[0]);
				t.put("y", newPos[1]);
				if (aps2.get(mac) == null) {
					t.put("count", 0);
				} else {
					t.put("count", Integer.parseInt(aps2.get(mac)));
				}
				list.add(t);
				map.put(floor, list);
			}
			if (aps.size() > 0) {
				result.put("status", Status.STATUS_SUCCESS);
				JSONObject dataJson = new JSONObject();
				for (String floor : map.keySet()) {
					JSONArray jsonArray = new JSONArray();
					for (JSONObject jsonObject : map.get(floor)) {
						jsonArray.put(jsonObject);
					}
					dataJson.put(floor, jsonArray);
				}
				result.put("data", dataJson);
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
			} else {
				result.put("status", Status.STATUS_EMPTY);
				result.put("message", Status.getMessage(Status.STATUS_EMPTY));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_EMPTY));
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			Log.warn(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR) + ":" + e.getMessage());
			return;
		}
	}
}
