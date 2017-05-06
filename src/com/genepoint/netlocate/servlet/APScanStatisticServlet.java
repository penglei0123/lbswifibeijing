package com.genepoint.netlocate.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.genepoint.tool.Log;

@WebServlet("/netlocate/apdata-statistic")
public class APScanStatisticServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public APScanStatisticServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String buildingCode = request.getParameter("buildingCode");
		String mac = request.getParameter("mac");
		int startTime = Integer.parseInt(request.getParameter("startTime"));
		int endTime = Integer.parseInt(request.getParameter("endTime"));
		String path = "/data/test_data/target.txt";
		JSONArray jsonArr = null;
		try {
			jsonArr = Getwifibeijingdata.statistic(mac, startTime, endTime, path);
		} catch (Exception e) {
			Log.trace(this.getClass(), e);
		}
		JSONObject result = new JSONObject();
		if (jsonArr != null && jsonArr.length() > 0) {
			result.put("status", 1);
			result.put("message", "success");
			result.put("data", jsonArr);
		} else {
			result.put("status", -1);
			result.put("message", "fail");
		}
		try {
			response.getWriter().write(result.toString());
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			Log.trace(this.getClass(), e);
		}
	}
}
