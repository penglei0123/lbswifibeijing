package com.genepoint.lbsshow.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.fastjson.JSONException;
import com.genepoint.custom.Action;
import com.genepoint.custom.Global;
import com.genepoint.custom.Status;
import com.genepoint.dao.DBUtil;
import com.genepoint.lbsshow.service.BuildingService;
import com.genepoint.tool.Log;

public class BuildingServiceImpl implements BuildingService {
	private Connection dbConn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	@Override
	public void getData(HttpServletRequest request, JSONObject result) {
		String action = request.getParameter("action");
		switch (action) {
		case Action.ACTION_GET_ALL_BUILDING:
			getAllBuilding(result);
			break;
		default:
			break;
		}
	}

	@Override
	public void postData(HttpServletRequest request, JSONObject result) {
		String action = request.getParameter("action");
		switch (action) {
		case Action.ACTION_SWITCH_BUILDING:
			switchBuilding(request, result);
			break;
		default:
			break;
		}
	}

	@Deprecated
	public void getAllBuildingOld(JSONObject result) {
		String sql = "select table_name from table_list where table_name like 'track_%'";
		try {
			dbConn = DBUtil.getConnection();
			rs = dbConn.createStatement().executeQuery(sql);
			String table = null;
			String building = null;
			Set<String> buildingSet = new HashSet<>();
			while (rs.next()) {
				table = rs.getString(1);
				String[] arrSplit = table.split("_", 3);
				if (arrSplit.length >= 2) {
					building = arrSplit[1];
				} else {
					building = table;
				}
				buildingSet.add(building);
			}
			rs.close();
			dbConn.close();
			JSONArray arr = new JSONArray();
			JSONObject obj = null;
			Iterator<String> it = buildingSet.iterator();
			while (it.hasNext()) {
				obj = new JSONObject();
				obj.put("building", it.next());
				arr.put(obj);
			}
			result.put("status", Status.STATUS_SUCCESS);
			result.put("buildings", arr);
			Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
		} catch (SQLException e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (JSONException e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (Exception e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

	public void getAllBuilding(JSONObject result) {
		String sql = "select building_code,building_name from building_list";
		try {
			dbConn = DBUtil.getConnection();
			rs = dbConn.createStatement().executeQuery(sql);
			JSONArray arr = new JSONArray();
			JSONObject obj = null;
			while (rs.next()) {
				obj = new JSONObject();
				obj.put("buildingCode", rs.getString(1));
				obj.put("buildingName", rs.getString(2));
				arr.put(obj);
			}
			rs.close();
			dbConn.close();
			result.put("status", Status.STATUS_SUCCESS);
			result.put("buildings", arr);
			Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
		} catch (SQLException e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (JSONException e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (Exception e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

	public void switchBuilding(HttpServletRequest request, JSONObject result) {
		String sql = "select building_name,floors from building_list where building_code=? limit 1";
		try {
			String data = request.getParameter("data");
			if (data == null || "".equals(data)) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			Global.buildingCode = null;
			Global.buildingName = null;
			Global.floorList = null;
			JSONObject json = new JSONObject(data);
			String buildingCode = json.getString("building");
			dbConn = DBUtil.getConnection();
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setString(1, buildingCode);
			rs = pstmt.executeQuery();
			boolean flag = false;
			String buildingName = null;
			String floorList = null;
			while (rs.next()) {
				flag = true;
				buildingName = rs.getString(1);
				floorList = rs.getString(2);
			}
			rs.close();
			dbConn.close();
			if (flag) {
				Global.buildingCode = buildingCode;
				Global.buildingName = buildingName;
				Global.floorList = floorList;
				result.put("status", Status.STATUS_SUCCESS);
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
			} else {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
			}
		} catch (SQLException e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (JSONException e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (Exception e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}

	@Deprecated
	public void switchBuildingOld(HttpServletRequest request, JSONObject result) {
		String sql = "select count(*) from table_list where table_name like ?";
		try {
			String data = request.getParameter("data");
			if (data == null || "".equals(data)) {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
				return;
			}
			JSONObject json = new JSONObject(data);
			String building = json.getString("building");
			dbConn = DBUtil.getConnection();
			pstmt = dbConn.prepareStatement(sql);
			pstmt.setString(1, "track_" + building + "%");
			rs = pstmt.executeQuery();
			rs.first();
			int count = rs.getInt(1);
			rs.close();
			dbConn.close();
			if (count > 0) {
				request.getSession().setAttribute("building", building);
				result.put("status", Status.STATUS_SUCCESS);
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_SUCCESS));
			} else {
				result.put("status", Status.STATUS_FORM_ERROR);
				result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
				Log.info(this.getClass(), Status.getMessage(Status.STATUS_FORM_ERROR));
			}
		} catch (SQLException e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (JSONException e) {
			result.put("status", Status.STATUS_FORM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_FORM_ERROR));
			Log.trace(this.getClass(), e);
		} catch (Exception e) {
			result.put("status", Status.STATUS_SYSTEM_ERROR);
			result.put("message", Status.getMessage(Status.STATUS_SYSTEM_ERROR));
			Log.trace(this.getClass(), e);
		} finally {
			DBUtil.release(rs, pstmt, dbConn);
		}
	}
}
