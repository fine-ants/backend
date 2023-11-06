package codesquad.fineants.domain.oauth.client;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import codesquad.fineants.domain.oauth.properties.OauthProperties;
import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.OauthException;
import codesquad.fineants.spring.api.member.response.OauthAccessTokenResponse;
import codesquad.fineants.spring.api.member.response.OauthUserProfileResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KakaoOauthClient extends OauthClient {

	public KakaoOauthClient(OauthProperties.Kakao kakao) {
		super(kakao.getClientId(),
			kakao.getClientSecret(),
			kakao.getTokenUri(),
			kakao.getUserInfoUri(),
			kakao.getRedirectUri());
	}

	@Override
	public OauthAccessTokenResponse exchangeAccessTokenByAuthorizationCode(String authorizationCode,
		String redirectUrl) {
		MultiValueMap<String, String> formData = createFormData(authorizationCode, redirectUrl);

		log.info("formData : {}", formData);

		OauthAccessTokenResponse response = WebClient.create()
			.post()
			.uri(getTokenUri())
			.headers(header -> {
				header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
				header.setAccept(List.of(MediaType.APPLICATION_JSON));
				header.setAcceptCharset(List.of(StandardCharsets.UTF_8));
			})
			.bodyValue(formData)
			.exchangeToMono(clientResponse -> {
				log.info("statusCode : {}", clientResponse.statusCode());
				if (clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
					return clientResponse.bodyToMono(String.class).handle((body, sink) -> {
						log.info("responseBody : {}", body);
						sink.error(new OauthException(OauthErrorCode.FAIL_ACCESS_TOKEN));
					});
				}
				return clientResponse.bodyToMono(OauthAccessTokenResponse.class);
			}).block();
		log.info("response : {}", response);

		if (Objects.requireNonNull(response).getAccessToken() == null) {
			throw new BadRequestException(OauthErrorCode.WRONG_AUTHORIZATION_CODE);
		}

		return response;
	}

	@Override
	public MultiValueMap<String, String> createFormData(String authorizationCode, String redirectUrl) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		if (redirectUrl == null) {
			redirectUrl = getRedirectUri();
		}
		formData.add("code", authorizationCode);
		formData.add("client_id", getClientId());
		formData.add("client_secret", getClientSecret());
		formData.add("redirect_uri", redirectUrl);
		formData.add("grant_type", "authorization_code");
		return formData;
	}

	@Override
	public OauthUserProfileResponse createOauthUserProfileResponse(Map<String, Object> attributes) {
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");
		log.info("profile={}", profile);
		String email = (String)kakaoAccount.get("email");
		String profileImage = (String)profile.get("profile_image_url");
		return new OauthUserProfileResponse(email, profileImage);
	}
}
