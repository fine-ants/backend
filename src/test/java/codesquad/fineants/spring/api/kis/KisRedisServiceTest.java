package codesquad.fineants.spring.api.kis;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class KisRedisServiceTest {

	@Autowired
	private KisRedisService service;

	@AfterEach
	void tearDown() {
		service.deleteAccessTokenMap();
	}

	@DisplayName("kis 액세스 토큰맵을 저장한다")
	@Test
	void setAccessTokenMap() {
		// given
		Map<String, Object> accessTokenMap = createAccessTokenMap();

		// when
		service.setAccessTokenMap(accessTokenMap, createNow());

		// then
		assertThat(service.getAccessTokenMap().isPresent()).isTrue();
	}

	@DisplayName("kis 액세스 토큰맵을 가져온다")
	@Test
	void getAccessTokenMap() {
		// given
		service.setAccessTokenMap(createAccessTokenMap(), createNow());
		// when
		Map<String, Object> accessTokenMap = service.getAccessTokenMap().orElseThrow();
		// then
		assertThat(accessTokenMap).isNotNull();
	}

	private LocalDateTime createNow() {
		return LocalDateTime.of(2023, 12, 6, 14, 0, 0);
	}

	private Map<String, Object> createAccessTokenMap() {
		Map<String, Object> accessTokenMap = new HashMap<>();
		accessTokenMap.put("access_token",
			"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6ImE1OGY4YzAyLWMzMzYtNGY3ZC04OGE0LWZkZDRhZTA3NmQ5YyIsImlzcyI6InVub2d3IiwiZXhwIjoxNzAxOTE2ODg3LCJpYXQiOjE3MDE4MzA0ODcsImp0aSI6IlBTRGc4WlVJd041eVl5ZkR6bnA0TDM2Z2xhRUpic2RJNGd6biJ9.uLZAu9_ompf8ycwiRJ5jrdoB-MiUG9a8quoQ3OeVOrUDGxyEhHmzZTPnCdLRWOEHowFlmyNOf3v-lPZGZqi9Kw");
		accessTokenMap.put("access_token_token_expired", "2023-12-07 11:41:27");
		accessTokenMap.put("token_type", "Bearer");
		accessTokenMap.put("expires_in", 86400);
		return accessTokenMap;
	}

}
