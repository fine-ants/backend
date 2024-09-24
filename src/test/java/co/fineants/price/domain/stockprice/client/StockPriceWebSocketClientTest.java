package co.fineants.price.domain.stockprice.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.testcontainers.shaded.org.awaitility.Awaitility.*;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.properties.KisProperties;
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

	@Autowired
	private KisProperties kisProperties;

	@LocalServerPort
	private int port;

	private String url;

	@BeforeEach
	void setup() {
		webSocketApprovalKeyRedisRepository.saveApprovalKey("approvalKey");
		url = "ws://localhost:" + port + "/ws/test";
		client.connect(url);
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
		given(webSocketClient.execute(handler, url))
			.willReturn(CompletableFuture.completedFuture(session));
		given(session.isOpen()).willReturn(true);
		BDDMockito.willThrow(new IOException("broken pipe"))
			.given(session).sendMessage(ArgumentMatchers.any(WebSocketMessage.class));
		StockPriceWebSocketClient stockPriceWebSocketClient = new StockPriceWebSocketClient(handler,
			webSocketApprovalKeyRedisRepository, webSocketClient, eventPublisher, kisProperties);
		stockPriceWebSocketClient.connect(url);

		String ticker = "005930";

		// when
		boolean result = stockPriceWebSocketClient.sendSubscribeMessage(ticker);
		// then
		assertThat(result).isFalse();
		assertThat(session.isOpen()).isTrue();
	}

	@DisplayName("웹소켓 연결을 성공적으로 해제하면 세션을 닫게된다")
	@Test
	void disconnect() {
		// given
		CloseStatus status = CloseStatus.NORMAL;
		// when
		client.disconnect(status);
		// then
		Assertions.assertThat(client.isConnect()).isFalse();
	}

	@DisplayName("세션을 해제에 실패하면 세션을 null로 저장한다")
	@Test
	void disconnect_whenSessionFailClose_thenSessionIsNull() throws IOException {
		// given
		WebSocketClient webSocketClient = Mockito.mock(WebSocketClient.class);
		WebSocketSession session = Mockito.mock(WebSocketSession.class);
		given(webSocketClient.execute(handler, url))
			.willReturn(CompletableFuture.completedFuture(session));
		given(session.isOpen()).willReturn(true);
		BDDMockito.willThrow(new IOException("error"))
			.given(session).close(CloseStatus.NORMAL);

		StockPriceWebSocketClient stockPriceWebSocketClient = new StockPriceWebSocketClient(handler,
			webSocketApprovalKeyRedisRepository, webSocketClient, eventPublisher, kisProperties);
		stockPriceWebSocketClient.connect(url);

		// when
		stockPriceWebSocketClient.disconnect(CloseStatus.NORMAL);
		// then
		Assertions.assertThat(stockPriceWebSocketClient.isConnect()).isFalse();
	}
}
