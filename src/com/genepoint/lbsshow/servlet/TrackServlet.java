package com.genepoint.lbsshow.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.genepoint.custom.Configs;
import com.genepoint.lbsshow.service.TrackService;
import com.genepoint.lbsshow.service.impl.TrackServiceImpl;
import com.genepoint.tool.Function;

@WebServlet("/track/*")
public class TrackServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String type = Function.parseURI(request.getRequestURI());
		// if (!type.equals("realtime") && !type.equals("history") && !type.equals("playback")) {
		// try {
		// response.sendRedirect(request.getContextPath() + "/track/realtime");
		// return;
		// } catch (IOException e) {
		// Log.trace(this.getClass(), e);
		// }
		// }
		request.setAttribute("page", type);
		String action = request.getParameter("action");
		if(action==null){
			try {
				if (type.equals("playback"))
					request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/playback.jsp").forward(request, response);
				else
					request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/track.jsp").forward(request, response);
			} catch (ServletException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			JSONObject result = new JSONObject();
			TrackService service = new TrackServiceImpl();
			service.getData(request, result);
			try {
				response.getWriter().write(result.toString());
				response.getWriter().flush();
				response.getWriter().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public TrackServlet() {
		super();
    }
}
