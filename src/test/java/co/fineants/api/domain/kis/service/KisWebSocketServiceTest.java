package co.fineants.api.domain.kis.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.api.AbstractContainerBaseTest;

class KisWebSocketServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private KisWebSocketService kisWebSocketService;

	@DisplayName("삼성전자의 현재가는 50000원이다")
	@Test
	void fetchCurrentPrice() throws InterruptedException {
		// given
		String ticker = "005930";
		// when
		kisWebSocketService.fetchCurrentPrice(ticker);
		// then
		Thread.sleep(10000);
	}

}
