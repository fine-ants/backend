package codesquad.fineants.spring.api.kis.manager;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KisAccessTokenManagerTest {

	@DisplayName("액세스 토큰이 만료되었다")
	@Test
	void isAccessTokenExpired() {
		// given
		Map<String, Object> map = new HashMap<>();
		map.put("access_token", "accessTokenValue");
		map.put("token_type", "Bearer");
		map.put("expires_in", 86400);
		KisAccessTokenManager manager = KisAccessTokenManager.from(map);
		LocalDateTime now = LocalDateTime.now();

		// when
		boolean actual = manager.isAccessTokenExpired(now);
		assertThat(actual).isFalse();

		LocalDateTime now2 = now.plusDays(2);
		boolean actual2 = manager.isAccessTokenExpired(now2);
		assertThat(actual2).isTrue();
	}

}
