package co.fineants.api.domain.kis.scheduler;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.repository.KisAccessTokenRepository;
import co.fineants.api.domain.kis.service.KisAccessTokenRedisService;
import reactor.core.publisher.Mono;

class KisAccessTokenSchedulerTest extends AbstractContainerBaseTest {

	@Autowired
	private KisAccessTokenScheduler accessTokenScheduler;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@Autowired
	private KisAccessTokenRedisService kisAccessTokenRedisService;

	@MockBean
	private KisClient kisClient;

	@BeforeEach
	void clean() {
		kisAccessTokenRepository.refreshAccessToken(null);
		kisAccessTokenRedisService.deleteAccessTokenMap();
	}

	@DisplayName("액세스 토큰의 만료시간이 1시간 이전이어서 재발급하여 저장소에 저장된다")
	@Test
	void checkAndReissueAccessToken() {
		// given
		BDDMockito.given(kisClient.fetchAccessToken())
			.willReturn(Mono.just(createKisAccessToken()).delayElement(Duration.ofSeconds(5)));
		// when
		accessTokenScheduler.checkAndReissueAccessToken();
		// then
		Assertions.assertThat(kisAccessTokenRepository.getAccessToken()).isPresent();
		Assertions.assertThat(kisAccessTokenRedisService.getAccessTokenMap()).isPresent();
	}

}
