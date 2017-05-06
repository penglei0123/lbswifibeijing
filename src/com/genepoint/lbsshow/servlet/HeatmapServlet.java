package com.genepoint.lbsshow.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.genepoint.custom.Configs;
import com.genepoint.lbsshow.service.HeatmapService;
import com.genepoint.lbsshow.service.impl.HeatmapServiceImpl;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;

@WebServlet("/heatmap/*")
public class HeatmapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String page = Function.parseURI(request.getRequestURI());
		if (!page.equals("realtime") && !page.equals("history")) {
			try {
				response.sendRedirect(request.getContextPath() + "/heatmap/realtime");
				return;
			} catch (IOException e) {
				Log.trace(this.getClass(), e);
			}
		}
		request.setAttribute("page", page);
		String action = request.getParameter("action");
		if(action==null){
			try {
				request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/heatmap.jsp").forward(request, response);
			} catch (ServletException e) {
				Log.trace(this.getClass(), e);
			} catch (IOException e) {
				Log.trace(this.getClass(), e);
			}
		}else{
			JSONObject result = new JSONObject();
			HeatmapService service = new HeatmapServiceImpl();
			service.getData(request, result);
			try {
				response.getWriter().write(result.toString());
				response.getWriter().flush();
				response.getWriter().close();
				result = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public HeatmapServlet() {
        super();
    }
}
