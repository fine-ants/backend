package codesquad.fineants.domain.kis.aop;

import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.kis.client.KisClient;
import codesquad.fineants.domain.kis.repository.KisAccessTokenRepository;
import codesquad.fineants.domain.kis.service.KisAccessTokenRedisService;
import codesquad.fineants.global.common.delay.DelayManager;
import codesquad.fineants.global.errors.exception.kis.KisException;
import reactor.core.publisher.Mono;

class AccessTokenAspectTest extends AbstractContainerBaseTest {

	@Autowired
	private AccessTokenAspect accessTokenAspect;

	@Autowired
	private KisAccessTokenRedisService kisAccessTokenRedisService;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@SpyBean
	private DelayManager delayManager;

	@MockBean
	private KisClient client;

	@BeforeEach
	void clean() {
		kisAccessTokenRepository.refreshAccessToken(null);
		kisAccessTokenRedisService.deleteAccessTokenMap();
	}

	@AfterEach
	void tearDown() {
		kisAccessTokenRedisService.deleteAccessTokenMap();
	}

	@DisplayName("액세스 토큰을 새로 발급하여 redis에 저장한다")
	@Test
	void checkAccessTokenExpiration() {
		// given
		given(client.fetchAccessToken())
			.willReturn(Mono.just(createKisAccessToken()));
		// when
		accessTokenAspect.checkAccessTokenExpiration();
		// then
		Assertions.assertThat(kisAccessTokenRedisService.getAccessTokenMap()).isPresent();
	}

	@DisplayName("액세스 토큰 발급이 실패하면 empty Mono를 반환한다")
	@Test
	void checkAccessTokenExpiration_whenAccessTokenExpiredAndFailFetchAccessToken_thenReturnEmptyMono() {
		// given
		given(client.fetchAccessToken())
			.willReturn(Mono.error(KisException.tokenIssuanceRetryLater()));
		given(delayManager.fixedAccessTokenDelay()).willReturn(Duration.ZERO);
		// when
		accessTokenAspect.checkAccessTokenExpiration();
		// then
		Assertions.assertThat(kisAccessTokenRepository.createAuthorization()).isNull();
		Assertions.assertThat(kisAccessTokenRedisService.getAccessTokenMap()).isEmpty();
	}
}
