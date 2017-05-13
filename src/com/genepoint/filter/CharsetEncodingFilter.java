package com.genepoint.filter;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.genepoint.custom.Configs;
import com.genepoint.tool.Log;

/**
 * 编码控制过滤器
 * 
 * @author jd
 *
 */
public class CharsetEncodingFilter implements Filter {
	private String encoding = null;

	@Override
	public void destroy() {
		// System.out.println("过滤器销毁");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		request.setCharacterEncoding(this.encoding);
		response.setCharacterEncoding(this.encoding);
		request.setAttribute("buildingChineseName", Configs.BUILDING_NAME_CHINESE);
		// 使用wrapper包装reponse，从而得到响应完成后的数据
		MyResponseWrapper wrapper = new MyResponseWrapper((HttpServletResponse) response);
		// 注意传递的参数为wrapper对象而不是response
		chain.doFilter(request, wrapper);
		String result = wrapper.getResult();
		byte[] outData = null;
		// 拦截通过AJAX方式请求的数据，当大小超过100KB时启用压缩传输
		String ajaxType = request.getHeader("X-Requested-With");
		if (ajaxType != null && ajaxType.equals("XMLHttpRequest") && result.startsWith("{") && result.endsWith("}") && result.length() > 1024 * 100) {
			outData = compress(result.getBytes());
			response.addHeader("Content-Encoding", "gzip");
		} else {
			outData = result.getBytes();
		}
		ServletOutputStream output = response.getOutputStream();
		output.write(outData);
		output.flush();
		output.close();

	}

	public byte[] compress(byte[] bt) {
		// 将byte数据读入文件流
		ByteArrayOutputStream bos = null;
		GZIPOutputStream gzipos = null;
		try {
			bos = new ByteArrayOutputStream();
			gzipos = new GZIPOutputStream(bos);
			gzipos.write(bt);
		} catch (Exception e) {
			Log.trace(this.getClass(), e);
		} finally {
			closeStream(gzipos);
			closeStream(bos);
		}
		return bos.toByteArray();
	}

	public void closeStream(Closeable oStream) {
		if (null != oStream) {
			try {
				oStream.close();
			} catch (IOException e) {
				Log.trace(this.getClass(), e);
			}
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.encoding = config.getInitParameter("encoding");
		Configs.ENCODING = this.encoding;
		// System.out.println("encoding："+this.encoding);
	}
}