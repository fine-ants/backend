package codesquad.fineants.domain.oauth.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import codesquad.fineants.domain.oauth.client.OauthClient;
import codesquad.fineants.domain.oauth.client.google.GoogleOauthClient;
import codesquad.fineants.domain.oauth.client.kakao.KakaoOauthClient;
import codesquad.fineants.domain.oauth.client.naver.NaverOauthClient;
import codesquad.fineants.spring.api.member.decoder.GoogleIDTokenDecoder;
import codesquad.fineants.spring.api.member.decoder.KakaoIDTokenDecoder;
import codesquad.fineants.spring.api.member.service.WebClientWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ConfigurationProperties(prefix = "oauth2")
public class OauthProperties {

	private final Naver naver;
	private final Kakao kakao;
	private final Google google;

	@ConstructorBinding
	public OauthProperties(Naver naver, Kakao kakao, Google google) {
		this.naver = naver;
		this.kakao = kakao;
		this.google = google;
	}

	public Map<String, OauthClient> createOauthClientMap(WebClientWrapper webClient) {
		Map<String, OauthClient> oauthClientMap = new HashMap<>();
		oauthClientMap.put("naver", new NaverOauthClient(naver, webClient));
		oauthClientMap.put("kakao", new KakaoOauthClient(kakao, webClient, new KakaoIDTokenDecoder()));
		oauthClientMap.put("google", new GoogleOauthClient(google, webClient, new GoogleIDTokenDecoder()));
		return oauthClientMap;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Naver {
		private final String clientId;
		private final String clientSecret;
		private final String authorizeUri;
		private final String tokenUri;
		private final String userInfoUri;
		private final String redirectUri;
		private final String responseType;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Kakao {
		private final String clientId;
		private final String clientSecret;
		private final String authorizeUri;
		private final String tokenUri;
		private final String jwksUri;
		private final String redirectUri;
		private final String responseType;
		private final String scope;
		private final String iss;
		private final String aud;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Google {
		private final String clientId;
		private final String clientSecret;
		private final String authorizeUri;
		private final String tokenUri;
		private final String jwksUri;
		private final String redirectUri;
		private final String responseType;
		private final String scope;
		private final String iss;
		private final String aud;
	}
}
