package codesquad.fineants.domain.oauth.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import codesquad.fineants.domain.oauth.client.KakaoOauthClient;
import codesquad.fineants.domain.oauth.client.NaverOauthClient;
import codesquad.fineants.domain.oauth.client.OauthClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ConfigurationProperties(prefix = "oauth2")
public class OauthProperties {

	private final Naver naver;
	private final Kakao kakao;

	@ConstructorBinding
	public OauthProperties(Naver naver, Kakao kakao) {
		this.naver = naver;
		this.kakao = kakao;
	}

	public Map<String, OauthClient> createOauthClientMap() {
		Map<String, OauthClient> oauthClientMap = new HashMap<>();
		oauthClientMap.put("naver", new NaverOauthClient(naver));
		oauthClientMap.put("kakao", new KakaoOauthClient(kakao));
		return oauthClientMap;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Naver {

		private final String clientId;
		private final String clientSecret;
		private final String tokenUri;
		private final String userInfoUri;
		private final String redirectUri;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Kakao {

		private final String clientId;
		private final String clientSecret;
		private final String tokenUri;
		private final String userInfoUri;
		private final String redirectUri;
	}
}
