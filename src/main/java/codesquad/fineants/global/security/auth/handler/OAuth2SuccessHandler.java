package codesquad.fineants.global.security.auth.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import codesquad.fineants.global.security.auth.dto.Token;
import codesquad.fineants.global.security.auth.dto.UserDto;
import codesquad.fineants.global.security.auth.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final TokenService tokenService;
	private final UserRequestMapper userRequestMapper;
	private final String loginSuccessUri;

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
		UserDto userDto = userRequestMapper.toDto(oAuth2User);
		log.debug("oAuth2User : {}", oAuth2User);
		log.debug("userDto : {}", userDto);

		Token token = tokenService.generateToken(userDto.getEmail(), "ROLE_USER");
		log.debug("token : {}", token);

		String redirectUrl = (String)request.getSession().getAttribute("redirect_url");
		if (redirectUrl == null) {
			redirectUrl = loginSuccessUri;
		}

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
			.queryParam("provider", userDto.getProvider())
			.queryParam("accessToken", token.getAccessToken())
			.queryParam("refreshToken", token.getRefreshToken())
			.build()
			.toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
