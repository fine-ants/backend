package codesquad.fineants.spring.api.kis.manager;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.fineants.spring.api.kis.client.KisAccessToken;

class KisAccessTokenManagerTest {

	@DisplayName("액세스 토큰이 만료되었다")
	@Test
	void isAccessTokenExpired() {
		// given
		KisAccessToken accessToken = new KisAccessToken("accessTokenValue", "Bearer",
			LocalDateTime.of(2023, 12, 23, 14, 8, 26), 86400);
		KisAccessTokenManager manager = new KisAccessTokenManager(accessToken);
		LocalDateTime now = LocalDateTime.of(2023, 12, 22, 15, 0, 0);

		// when
		boolean actual = manager.isAccessTokenExpired(now);
		assertThat(actual).isFalse();

		LocalDateTime now2 = now.plusDays(2);
		boolean actual2 = manager.isAccessTokenExpired(now2);
		assertThat(actual2).isTrue();
	}

}
