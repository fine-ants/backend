package codesquad.fineants.global.security.ajax.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import codesquad.fineants.domain.member.domain.dto.request.OauthMemberLogoutRequest;
import codesquad.fineants.domain.member.service.MemberService;
import codesquad.fineants.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AjaxLogoutHandler implements LogoutHandler {
	private final MemberService memberService;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication);
		}
		// add the refreshToken to the blacklist
		String refreshToken = CookieUtils.getRefreshToken(request);
		memberService.logout(OauthMemberLogoutRequest.create(refreshToken), request, response);
	}
}
