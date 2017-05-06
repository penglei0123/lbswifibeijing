package com.genepoint.filter.service;

import javax.servlet.http.HttpServletRequest;

public interface SecurityService {
	
	public boolean checkLogin(HttpServletRequest request);
}
