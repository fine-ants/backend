package co.fineants.api.domain.kis.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import co.fineants.api.domain.kis.client.KisAccessToken;

@ActiveProfiles("test")
class KisAccessTokenRepositoryTest {

	@DisplayName("액세스 토큰이 만료되었다")
	@Test
	void isAccessTokenExpired() {
		// given
		KisAccessToken accessToken = new KisAccessToken("accessTokenValue", "Bearer",
			LocalDateTime.of(2023, 12, 23, 14, 8, 26), 86400);
		KisAccessTokenRepository repository = new KisAccessTokenRepository(accessToken);
		LocalDateTime now = LocalDateTime.of(2023, 12, 22, 15, 0, 0);

		// when
		boolean actual = repository.isAccessTokenExpired(now);
		assertThat(actual).isFalse();

		LocalDateTime now2 = now.plusDays(2);
		boolean actual2 = repository.isAccessTokenExpired(now2);
		assertThat(actual2).isTrue();
	}

}
