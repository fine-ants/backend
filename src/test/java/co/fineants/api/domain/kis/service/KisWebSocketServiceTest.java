package co.fineants.api.domain.kis.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisWebSocketClient;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;

class KisWebSocketServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private KisWebSocketService kisWebSocketService;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private WebSocketApprovalKeyRedisRepository webSocketApprovalKeyRedisRepository;

	@MockBean
	private KisWebSocketClient kisWebSocketClient;

	@DisplayName("삼성전자의 현재가는 50000원이다")
	@Test
	void fetchCurrentPrice() {
		// given
		BDDMockito.willDoNothing().given(kisWebSocketClient).sendMessage(ArgumentMatchers.anyString());
		webSocketApprovalKeyRedisRepository.saveApprovalKey("approvalKey");
		String ticker = "005930";
		// when
		kisWebSocketService.fetchCurrentPrice(ticker);
		// then
		Assertions.assertThat(currentPriceRedisRepository.getCachedPrice(ticker)).isPresent();
	}
}
