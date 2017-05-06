package com.genepoint.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.genepoint.filter.service.SecurityService;
import com.genepoint.filter.service.impl.SecurityServiceImpl;
import com.genepoint.tool.Log;

/**
 * 统一权限控制过滤器
 * @author jd
 *
 */
public class PrivilegesFilter implements Filter {
	private String[] excludePages;
    @Override
    public void destroy() {
        //System.out.println("过滤器销毁");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
    	HttpServletResponse httpResponse = (HttpServletResponse) response;
    	SecurityService securityService = new SecurityServiceImpl();
    	String url = httpRequest.getServletPath();
    	String contextPath = httpRequest.getContextPath();
    	Log.info(this.getClass(),"receive request:"+url);
    	boolean isExcluded=false;
    	for(String page:excludePages){
    		if(page.equals(url)){
    			isExcluded = true;
    			break;
    		}
    	}
    	boolean isLogin = securityService.checkLogin(httpRequest);
    	if(isExcluded || isLogin){
    		chain.doFilter(request, response);
    	}else{
    		if(url.startsWith("/admin/")){
    			httpResponse.sendRedirect(contextPath+"/admin/login");
    		}else{
    			httpResponse.sendRedirect(contextPath+"/developer/login");
    		}
    	}
    }

    @Override
    
    public void init(FilterConfig config) throws ServletException {
    	excludePages = config.getInitParameter("excludedPages").split(",");
    }
}