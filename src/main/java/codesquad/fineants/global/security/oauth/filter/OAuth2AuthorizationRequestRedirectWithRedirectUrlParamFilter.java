package codesquad.fineants.global.security.oauth.filter;

import java.io.IOException;

import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class OAuth2AuthorizationRequestRedirectWithRedirectUrlParamFilter extends GenericFilterBean {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		HttpServletRequest servletRequest = (HttpServletRequest)request;
		String redirectUrl = servletRequest.getParameter("redirect_url");
		if (redirectUrl != null) {
			servletRequest.getSession().setAttribute("redirect_url", redirectUrl);
		}
		chain.doFilter(request, response);
	}
}
