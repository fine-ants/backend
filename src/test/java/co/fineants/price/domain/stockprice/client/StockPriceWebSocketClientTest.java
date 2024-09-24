package co.fineants.price.domain.stockprice.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

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

	@Autowired
	private StockPriceWebSocketHandler handler;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

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
		client.sendSubscribeMessage(ticker);
		// then
		await().atMost(Duration.ofSeconds(5))
			.until(() -> currentPriceRedisRepository.getCachedPrice(ticker).isPresent());
		assertThat(currentPriceRedisRepository.getCachedPrice(ticker).orElseThrow()).isEqualTo("70100");
	}

	@DisplayName("세션을 통해서 메시지를 전송하다가 파이프가 부러지면 세션을 종료하고 재연결한다")
	@Test
	void sendMessage_whenBrokenPipe_thenCloseAndReconnectSession() throws IOException {
		// given
		WebSocketClient webSocketClient = Mockito.mock(WebSocketClient.class);
		WebSocketSession session = Mockito.mock(WebSocketSession.class);
		String uri = "ws://localhost:" + port + "/ws/test";
		given(webSocketClient.execute(handler, uri))
			.willReturn(CompletableFuture.completedFuture(session));
		given(session.isOpen()).willReturn(true);
		BDDMockito.willThrow(new IOException("broken pipe"))
			.given(session).sendMessage(ArgumentMatchers.any(WebSocketMessage.class));
		StockPriceWebSocketClient stockPriceWebSocketClient = new StockPriceWebSocketClient(handler,
			webSocketApprovalKeyRedisRepository, webSocketClient, eventPublisher);
		stockPriceWebSocketClient.connect(uri);

		String ticker = "005930";

		// when
		boolean result = stockPriceWebSocketClient.sendSubscribeMessage(ticker);
		// then
		assertThat(result).isFalse();
		assertThat(session.isOpen()).isTrue();
	}
}
