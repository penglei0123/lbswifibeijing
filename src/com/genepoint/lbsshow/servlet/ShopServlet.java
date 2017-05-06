package com.genepoint.lbsshow.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.genepoint.custom.Configs;
import com.genepoint.lbsshow.service.ShopService;
import com.genepoint.lbsshow.service.impl.ShopServiceImpl;
import com.genepoint.tool.Log;

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String action = request.getParameter("action");
		if(action==null){
			try {
				request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/shop.jsp").forward(request, response);
			} catch (ServletException e) {
				Log.trace(this.getClass(), e);
			} catch (IOException e) {
				Log.trace(this.getClass(), e);
			}
		}else{
			JSONObject result = new JSONObject();
			ShopService service = new ShopServiceImpl();
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
	
	public ShopServlet() {
        super();
    }
}
