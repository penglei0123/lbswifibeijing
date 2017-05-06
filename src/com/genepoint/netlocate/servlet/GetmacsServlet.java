package com.genepoint.netlocate.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.genepoint.tool.Log;

@WebServlet("/netlocate/getmacs")
public class GetmacsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetmacsServlet() {
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
		String path = "/data/test_data/target.txt";
		List<Map.Entry<String, Integer>> macList = null;
		try {
			macList = Getwifibeijingdata.getMACList(path);
		} catch (Exception e) {
			Log.trace(this.getClass(), e);
		}
		JSONObject result = new JSONObject();
		if (macList!=null && macList.size() > 0) {
			result.put("status", 1);
			result.put("message", "success");
			JSONArray arr = new JSONArray();
			Iterator<Map.Entry<String, Integer>> it = macList.iterator();
			int count = 0;
			while (it.hasNext()) {
				arr.put(it.next().getKey());
				count++;
				if (count > 100) {
					break;
				}
			}
			result.put("data", arr);
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
