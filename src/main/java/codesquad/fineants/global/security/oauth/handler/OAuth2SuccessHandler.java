package codesquad.fineants.global.security.oauth.handler;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import codesquad.fineants.global.security.factory.TokenFactory;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.dto.Token;
import codesquad.fineants.global.security.oauth.service.TokenService;
import codesquad.fineants.global.util.CookieUtils;
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
		if (principal instanceof DefaultOidcUser) {
			oAuth2User = (DefaultOidcUser)principal;
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

		List<ResponseCookie> cookies = List.of(tokenFactory.createAccessTokenCookie(token),
			tokenFactory.createRefreshTokenCookie(token));
		String cookieValue = cookies.stream()
			.map(ResponseCookie::toString)
			.collect(Collectors.joining("; "));
		CookieUtils.setCookie(response, cookieValue);

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
