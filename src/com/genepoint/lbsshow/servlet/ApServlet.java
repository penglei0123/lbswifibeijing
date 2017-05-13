package com.genepoint.lbsshow.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.genepoint.custom.Configs;
import com.genepoint.lbsshow.service.ApService;
import com.genepoint.lbsshow.service.impl.ApServiceImpl;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;

/**
 * Servlet implementation class ApServlet
 */
@WebServlet("/ap/*")
public class ApServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response){

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String type = Function.parseURI(request.getRequestURI());
		if (!type.equals("realtime") && !type.equals("history")) {
			try {
				response.sendRedirect(request.getContextPath() + "/ap/realtime");
				return;
			} catch (IOException e) {
				Log.trace(this.getClass(), e);
			}
		}
		request.setAttribute("page", type);
		String action = request.getParameter("action");
		if(action==null){
			try {
				request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/ap.jsp").forward(request, response);
			} catch (ServletException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			JSONObject result = new JSONObject();
			ApService service = new ApServiceImpl();
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

	public ApServlet() {
        super();
    }

}
