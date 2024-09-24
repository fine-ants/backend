package co.fineants.price.domain.stockprice.client;

import static org.mockito.BDDMockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;

class StockPriceWebSocketHandlerTest extends AbstractContainerBaseTest {

	@Autowired
	private WebSocketApprovalKeyRedisRepository approvalKeyRedisRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@MockBean
	private ApplicationEventPublisher eventPublisher;

	@BeforeEach
	void setup() {
		approvalKeyRedisRepository.saveApprovalKey("approvalKey");
	}

	@DisplayName("퐁 데이터 전송시 에러가 발생하면 세션을 종료 및 재연결한다")
	@Test
	void sendMessage_whenSentPongMessage_thenCloseAndReconnectSession() throws IOException {
		// given
		WebSocketSession session = Mockito.mock(WebSocketSession.class);
		willThrow(new IOException("broken pipe"))
			.given(session).sendMessage(ArgumentMatchers.any(TextMessage.class));
		StockPriceWebSocketHandler handler = new StockPriceWebSocketHandler(currentPriceRedisRepository,
			eventPublisher);
		// when
		handler.handleMessage(session, new TextMessage("PINGPONG"));
		// then
		verify(eventPublisher, Mockito.times(1)).publishEvent(ArgumentMatchers.any(ApplicationEvent.class));
	}

}
