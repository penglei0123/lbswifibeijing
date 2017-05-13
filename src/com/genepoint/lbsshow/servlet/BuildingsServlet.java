package com.genepoint.lbsshow.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.genepoint.custom.Configs;
import com.genepoint.lbsshow.service.BuildingService;
import com.genepoint.lbsshow.service.impl.BuildingServiceImpl;

@WebServlet("/buildings")
public class BuildingsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		JSONObject result = new JSONObject();
		BuildingService service = new BuildingServiceImpl();
		service.postData(request, result);
		try {
			response.getWriter().write(result.toString());
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String action = request.getParameter("action");
		System.out.println("get request");
		if(action==null){
			try {
				request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/buildings.jsp").forward(request, response);
			} catch (ServletException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			System.out.println(action);
			JSONObject result = new JSONObject();
			// call service
			BuildingService service = new BuildingServiceImpl();
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
	
	public BuildingsServlet() {
        super();
    }
}
