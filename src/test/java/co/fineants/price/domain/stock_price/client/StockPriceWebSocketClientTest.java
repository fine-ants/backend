package co.fineants.price.domain.stock_price.client;

import static org.assertj.core.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;

class StockPriceWebSocketClientTest extends AbstractContainerBaseTest {

	@Autowired
	private StockPriceWebSocketClient client;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private WebSocketApprovalKeyRedisRepository webSocketApprovalKeyRedisRepository;

	@LocalServerPort
	private int port;

	@BeforeEach
	void setup() {
		webSocketApprovalKeyRedisRepository.saveApprovalKey("approvalKey");
		String uri = "ws://localhost:" + port + "/ws/test";
		client.connect(uri);
	}

	@DisplayName("웹소켓 연결된 상태에서 티커심볼을 전달하고 종목의 실시간 체결가를 응답받는다")
	@Test
	void sendMessage() {
		// given
		String ticker = "005930";
		// when
		client.sendMessage(ticker);
		// then
		await().atMost(Duration.ofSeconds(5))
			.until(() -> currentPriceRedisRepository.getCachedPrice(ticker).isPresent());
		assertThat(currentPriceRedisRepository.getCachedPrice(ticker).orElseThrow()).isEqualTo("70100");
	}
}
