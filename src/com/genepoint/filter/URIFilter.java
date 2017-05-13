package com.genepoint.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.genepoint.custom.Configs;
import com.genepoint.custom.Global;

/**
 * URI控制过滤器
 * 
 * @author jd
 *
 */
public class URIFilter implements Filter {

    @Override
    public void destroy() {
        //System.out.println("过滤器销毁");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		// HttpServletResponse httpResponse = (HttpServletResponse) response;
		String uri = httpRequest.getRequestURI();
		String[] arr = uri.split("/");
		if (arr.length < 4) {
			httpRequest.getRequestDispatcher(Configs.VIEW_BASE_PATH + "/error.jsp").forward(request, response);
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 3; i < arr.length; i++) {
				sb.append("/" + arr[i]);
			}
			request.setAttribute("user", arr[2]);
			String building = Global.buildingCode;
			if (building == null && !uri.endsWith("/buildings") && !uri.contains(httpRequest.getContextPath() + "/" + arr[2] + "/static/")) {
				httpRequest.getRequestDispatcher("/buildings").forward(request, response);
			} else if (building != null && !uri.endsWith("/buildings")) {
				httpRequest.getRequestDispatcher(sb.toString()).forward(request, response);
				// 这是最后一个拦截器，不再有chain.doFilter
			} else {
				// 静态资源请求直接去掉user信息后直接转发请求
				httpRequest.getRequestDispatcher(sb.toString()).forward(request, response);
			}
		}
    }

    @Override
    
    public void init(FilterConfig config) throws ServletException {
        //System.out.println("encoding："+this.encoding);
    }
}