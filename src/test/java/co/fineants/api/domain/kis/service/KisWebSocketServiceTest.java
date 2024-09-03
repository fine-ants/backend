package co.fineants.api.domain.kis.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisWebSocketApprovalKey;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import reactor.core.publisher.Mono;

class KisWebSocketServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private KisWebSocketService kisWebSocketService;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@MockBean
	private KisClient kisClient;

	@DisplayName("삼성전자의 현재가는 50000원이다")
	@Test
	void fetchCurrentPrice() {
		// given
		BDDMockito.given(kisClient.fetchWebSocketApprovalKey())
			.willReturn(Mono.just(KisWebSocketApprovalKey.create("approvalKey")));

		String ticker = "005930";
		// when
		kisWebSocketService.fetchCurrentPrice(ticker);
		// then
		Assertions.assertThat(currentPriceRedisRepository.fetchPriceBy(ticker)).isPresent();
	}

}
