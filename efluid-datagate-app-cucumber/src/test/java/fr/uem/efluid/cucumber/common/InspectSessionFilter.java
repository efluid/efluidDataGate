package fr.uem.efluid.cucumber.common;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class InspectSessionFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(InspectSessionFilter.class);

	/**
	 * @param req
	 * @param res
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {

		// Continue
		chain.doFilter(req, res);

		HttpSession ses = ((HttpServletRequest) req).getSession();
		Enumeration<String> en = ses.getAttributeNames();

		LOGGER.info("[INSPECT] Start inspecting session content");

		while (en.hasMoreElements()) {
			String attr = en.nextElement();
			LOGGER.info("[INSPECT]   -> attr \"{}\" : {}", attr, ses.getAttribute(attr));
		}
	}

	/**
	 * @param filterConfig
	 * @throws ServletException
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOGGER.info("[INSPECT] Start filter");
	}

	/**
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}