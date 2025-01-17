package co.fineants.api.global.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class TraceIdFilter implements Filter {

	private static final String TRACE_ID = "traceId";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		try {
			MDC.put(TRACE_ID, UUID.randomUUID().toString());
			chain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}
