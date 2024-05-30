package codesquad.fineants.global.security.oauth.filter;

import static org.springframework.http.HttpHeaders.*;

import java.io.IOException;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import codesquad.fineants.domain.member.service.OauthMemberRedisService;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
	private final TokenService tokenService;
	private final OauthMemberRedisService oauthMemberRedisService;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		String token = ((HttpServletRequest)request).getHeader(AUTHORIZATION);

		if (token != null && !oauthMemberRedisService.isAlreadyLogout(token)) {
			if (!token.startsWith("Bearer")) {
				throw new IllegalArgumentException("Invalid token type");
			}

			token = token.split(" ")[1];
			if (tokenService.verifyToken(token)) {
				MemberAuthentication memberAuthentication = tokenService.parseMemberAuthenticationToken(token);
				Authentication auth = getAuthentication(memberAuthentication);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}
		chain.doFilter(request, response);
	}

	private Authentication getAuthentication(MemberAuthentication memberAuthentication) {
		return new UsernamePasswordAuthenticationToken(
			memberAuthentication,
			Strings.EMPTY,
			memberAuthentication.getSimpleGrantedAuthority()
		);
	}
}
