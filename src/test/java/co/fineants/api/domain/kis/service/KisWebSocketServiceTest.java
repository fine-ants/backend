package co.fineants.api.domain.kis.service;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisWebSocketClient;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;

class KisWebSocketServiceTest extends AbstractContainerBaseTest {

	@LocalServerPort
	private int port;

	@Autowired
	private KisWebSocketService kisWebSocketService;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private WebSocketApprovalKeyRedisRepository webSocketApprovalKeyRedisRepository;

	@Autowired
	private KisWebSocketClient kisWebSocketClient;

	@BeforeEach
	void setup() throws URISyntaxException {
		webSocketApprovalKeyRedisRepository.saveApprovalKey("approvalKey");

		String wsUri = "ws://localhost:" + port + "/ws/test";
		kisWebSocketClient.connect(new URI(wsUri));
	}

	@DisplayName("삼성전자의 현재가는 70100원이다")
	@Test
	void fetchCurrentPrice() throws InterruptedException {
		// given
		String ticker = "005930";
		// when
		kisWebSocketService.fetchCurrentPrice(ticker);
		// then
		Thread.sleep(100);
		assertThat(currentPriceRedisRepository.getCachedPrice(ticker)).isPresent();
		assertThat(currentPriceRedisRepository.getCachedPrice(ticker).orElseThrow()).isEqualTo("70100");
	}
}
