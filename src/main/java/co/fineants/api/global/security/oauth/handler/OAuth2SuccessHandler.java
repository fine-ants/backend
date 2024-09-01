package co.fineants.api.global.security.oauth.handler;

import java.io.IOException;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import co.fineants.api.global.security.factory.TokenFactory;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.dto.Token;
import co.fineants.api.global.security.oauth.service.TokenService;
import co.fineants.api.global.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final TokenService tokenService;
	private final OAuth2UserMapper oauth2UserMapper;
	private final String loginSuccessUri;
	private final TokenFactory tokenFactory;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		OAuth2User oAuth2User;
		Object principal = authentication.getPrincipal();
		if (principal instanceof DefaultOidcUser defaultOidcUser) {
			oAuth2User = defaultOidcUser;
		} else {
			oAuth2User = (OAuth2User)principal;
		}
		MemberAuthentication memberAuthentication = oauth2UserMapper.toMemberAuthentication(oAuth2User);
		log.debug("oAuth2User : {}", oAuth2User);
		log.debug("userDto : {}", memberAuthentication);

		Token token = tokenService.generateToken(memberAuthentication, new Date());
		log.debug("token : {}", token);

		String redirectUrl = (String)request.getSession().getAttribute("redirect_url");
		if (redirectUrl == null) {
			redirectUrl = loginSuccessUri;
		}

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
			.queryParam("success", "true")
			.build()
			.toUriString();

		CookieUtils.setCookie(response, tokenFactory.createAccessTokenCookie(token));
		CookieUtils.setCookie(response, tokenFactory.createRefreshTokenCookie(token));

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
