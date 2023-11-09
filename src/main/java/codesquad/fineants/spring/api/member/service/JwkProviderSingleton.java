package codesquad.fineants.spring.api.member.service;

import java.util.concurrent.TimeUnit;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;

public class JwkProviderSingleton {

	private static final String JWK_URL = "https://kauth.kakao.com";
	private static final int CACHE_SIZE = 10;
	private static final int CACHE_EXPIRATION = 7; // days

	private static JwkProvider instance;

	private JwkProviderSingleton() {
		// private constructor to prevent instantiation
	}

	public static synchronized JwkProvider getInstance() {
		if (instance == null) {
			instance = new JwkProviderBuilder(JWK_URL)
				.cached(CACHE_SIZE, CACHE_EXPIRATION, TimeUnit.DAYS)
				.build();
		}
		return instance;
	}

	public void verifyIdToken(String kid) {

	}

}
