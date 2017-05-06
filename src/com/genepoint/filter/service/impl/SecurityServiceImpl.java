package com.genepoint.filter.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import com.genepoint.custom.Configs;
import com.genepoint.dao.DBUtil;
import com.genepoint.filter.service.SecurityService;
import com.genepoint.tool.Log;
import com.genepoint.tool.RegexUtil;

public class SecurityServiceImpl implements SecurityService{
	private Connection db = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	
	public boolean checkLogin(HttpServletRequest request){
		String userLevel = getUserLevelBySession(request);
		if (userLevel.equals(Configs.USER_LEVEL_GUEST))
			userLevel = getUserLevelByToken(request);
		String url = request.getServletPath();
		if(userLevel.equals(Configs.USER_LEVEL_GUEST)){
			return false;
		}else if(userLevel.equals(Configs.USER_LEVEL_DEVELOPER)){
			if(url.startsWith("/developer/")){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
	
	public String getUserLevelBySession(HttpServletRequest request) {
		String userLevel = (String) request.getSession().getAttribute("user_level");
		if(userLevel==null){
			return Configs.USER_LEVEL_GUEST;
		}else if(userLevel.equals(Configs.USER_LEVEL_DEVELOPER)){
			return Configs.USER_LEVEL_DEVELOPER;
		}else if(userLevel.equals(Configs.USER_LEVEL_ADMIN)){
			return Configs.USER_LEVEL_ADMIN;
		}else{
			return Configs.USER_LEVEL_GUEST;
		}
	}

	public String getUserLevelByToken(HttpServletRequest request) {
		String token = request.getParameter("token");
		if (token == null || "".equals(token) || !RegexUtil.isVaildToken(token))
			return Configs.USER_LEVEL_GUEST;
		try {
			db = DBUtil.getConnection();
			db.setAutoCommit(true);
			pstmt = db
					.prepareStatement("select count(uid) from developer_info where developer_key=? limit 1");
			pstmt.setString(1, token);
			rs = pstmt.executeQuery();
			rs.first();
			int count = rs.getInt(1);
			rs.close();
			rs = null;
			pstmt.close();
			pstmt = null;
			db.close();
			db = null;
			return count == 1 ? Configs.USER_LEVEL_DEVELOPER
					: Configs.USER_LEVEL_GUEST;
		} catch (SQLException e) {
			Log.trace(this.getClass(), "token check fail", e);
			return Configs.USER_LEVEL_GUEST;
		} finally {
			DBUtil.release(rs, pstmt, db);
		}

	}
}
