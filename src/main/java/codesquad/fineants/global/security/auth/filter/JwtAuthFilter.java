package codesquad.fineants.global.security.auth.filter;

import java.io.IOException;
import java.util.Arrays;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import codesquad.fineants.global.security.auth.dto.UserDto;
import codesquad.fineants.global.security.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends GenericFilterBean {
	private final TokenService tokenService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		String token = ((HttpServletRequest)request).getHeader("Auth");

		if (token != null && tokenService.verifyToken(token)) {
			UserDto userDto = tokenService.parseUserDtoFrom(token);
			log.debug("userDto : {}", userDto);
			Authentication auth = getAuthentication(userDto);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		chain.doFilter(request, response);
	}

	private Authentication getAuthentication(UserDto userDto) {
		return new UsernamePasswordAuthenticationToken(
			userDto,
			Strings.EMPTY,
			Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
		);
	}
}
