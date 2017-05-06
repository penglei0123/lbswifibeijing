package com.genepoint.lbsshow.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.genepoint.custom.Configs;
import com.genepoint.lbsshow.service.CustomerService;
import com.genepoint.lbsshow.service.impl.CustomerServiceImpl;
import com.genepoint.tool.Function;
import com.genepoint.tool.Log;

@WebServlet("/customer/*")
public class CustomerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String type = Function.parseURI(request.getRequestURI());
		if (!type.equals("realtime") && !type.equals("history")) {
			try {
				response.sendRedirect(request.getContextPath() + "/customer/realtime");
				return;
			} catch (IOException e) {
				Log.trace(this.getClass(), e);
			}
		}
		request.setAttribute("page", type);
		String action = request.getParameter("action");
		if(action==null){
			try {
				request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/customer.jsp").forward(request, response);
			} catch (ServletException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			JSONObject result = new JSONObject();
			CustomerService service = new CustomerServiceImpl();
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
	
	public CustomerServlet() {
        super();
    }
}
