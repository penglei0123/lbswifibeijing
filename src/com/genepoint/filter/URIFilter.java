package com.genepoint.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
		String uri = ((HttpServletRequest) request).getRequestURI();
		String[] arr = uri.split("/");
		if (arr.length >= 4) {
			request.setAttribute("user", arr[2]);
			StringBuffer sb = new StringBuffer();
			for (int i = 3; i < arr.length; i++) {
				sb.append("/" + arr[i]);
			}
			((HttpServletRequest) request).getRequestDispatcher(sb.toString()).forward(request, response);
			// 这是最后一个拦截器，不再有chain.doFilter
		}
    }

    @Override
    
    public void init(FilterConfig config) throws ServletException {
        //System.out.println("encoding："+this.encoding);
    }
}