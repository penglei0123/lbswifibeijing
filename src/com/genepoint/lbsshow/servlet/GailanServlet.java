package com.genepoint.lbsshow.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.genepoint.custom.Configs;
import com.genepoint.tool.Log;

@WebServlet("/gailan")
public class GailanServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String action = request.getParameter("action");
		if(action==null){
			try {
				request.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/gailan.jsp").forward(request, response);
			} catch (ServletException e) {
				Log.trace(this.getClass(), e);
			} catch (IOException e) {
				Log.trace(this.getClass(), e);
			}
		}else{

		}
	}
	
	public GailanServlet() {
        super();
    }
}
