package codesquad.fineants.spring.api.kis.aop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.spring.api.kis.service.KisRedisService;

@ActiveProfiles("test")
@SpringBootTest
class AccessTokenAspectTest {

	@Autowired
	private AccessTokenAspect accessTokenAspect;

	@Autowired
	private KisRedisService kisRedisService;

	@DisplayName("액세스 토큰을 새로 발급하여 redis에 저장한다")
	@Test
	void checkAccessTokenExpiration() {
		// given
		kisRedisService.deleteAccessTokenMap();
		// when
		accessTokenAspect.checkAccessTokenExpiration();
		// then
		Assertions.assertThat(kisRedisService.getAccessTokenMap().isPresent()).isTrue();
	}
}
