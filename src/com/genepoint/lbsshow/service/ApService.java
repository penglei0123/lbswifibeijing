package com.genepoint.lbsshow.service;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public interface ApService {
	void getData(HttpServletRequest request, JSONObject result);
}
