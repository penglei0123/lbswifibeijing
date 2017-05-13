package com.genepoint.lbsshow.service;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public interface BuildingService {
	void getData(HttpServletRequest request, JSONObject result);

	void postData(HttpServletRequest request, JSONObject result);
}
