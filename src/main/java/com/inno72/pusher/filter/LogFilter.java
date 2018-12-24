package com.inno72.pusher.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
@WebFilter(urlPatterns = "/*", filterName = "logFiter")
public class LogFilter implements Filter{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void init(FilterConfig filterConfig) throws ServletException {	
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
	
		if(StringUtils.isNotEmpty(request.getContentType())
				&& (request.getContentType().startsWith("application/x-www-form-urlencoded") 
				|| request.getContentType().startsWith("application/json") )) {
			
			HttpServletRequest httpRequest = (HttpServletRequest)request;
				
			Map<String, String[]> param = httpRequest.getParameterMap();
			StringBuilder sb = new StringBuilder("{");
			for(String key : param.keySet()) {
				String value = null;
				String[] values= param.get(key);
				if(values != null && values.length > 0) {
					value = String.join(",", values);
				}
				sb.append(String.format("\"%s\":\"%s\",", key, value));
			}
			sb.append("}");
			
			logger.info(String.format("http req %s %s:%s", httpRequest.getRequestURI(), httpRequest.getMethod(), sb.toString()));
			
		} 
			
		chain.doFilter(request, response);
			
	}

	public void destroy() {
	}

}
