package com.genepoint.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.genepoint.custom.Configs;

/**
 * 编码控制过滤器
 * @author jd
 *
 */
public class CharsetEncodingFilter implements Filter {
	private String encoding = null;
    @Override
    public void destroy() {
        //System.out.println("过滤器销毁");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding(this.encoding);
        response.setCharacterEncoding(this.encoding);
		request.setAttribute("buildingChineseName", Configs.BUILDING_NAME_CHINESE);
		chain.doFilter(request, response);

    }

    @Override
    
    public void init(FilterConfig config) throws ServletException {
        this.encoding = config.getInitParameter("encoding"); 
        Configs.ENCODING = this.encoding;
        //System.out.println("encoding："+this.encoding);
    }
}