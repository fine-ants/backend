package co.fineants.api.domain.kis.client;

import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.factory.WebSocketContainerProviderFactory;
import jakarta.websocket.CloseReason;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

class KisWebSocketClientTest extends AbstractContainerBaseTest {

	@MockBean
	private WebSocketContainerProviderFactory webSocketContainerProviderFactory;

	@MockBean
	private WebSocketContainer webSocketContainer;

	@MockBean
	private Session session;

	@Mock
	private RemoteEndpoint.Async asyncRemote;

	private KisWebSocketClient kisWebSocketClient;

	@BeforeEach
	void setup() throws DeploymentException, IOException, URISyntaxException {
		kisWebSocketClient = new KisWebSocketClient(webSocketContainerProviderFactory);
		given(webSocketContainerProviderFactory.getContainerProvider())
			.willReturn(webSocketContainer);
		given(webSocketContainer.connectToServer(kisWebSocketClient, new URI("ws://localhost:8080/test")))
			.willReturn(session);
	}

	@DisplayName("웹소켓 연결을 하여 세션을 받는다")
	@Test
	void connect() throws URISyntaxException {
		// given
		URI endpointUri = new URI("ws://localhost:8080/test");
		// when
		Session connectSession = kisWebSocketClient.connect(endpointUri);
		// then
		Assertions.assertThat(connectSession).isEqualTo(session);
	}

	@DisplayName("웹소켓 클라이언트에 세션을 설정한다")
	@Test
	void onOpen() {
		// given

		// when
		kisWebSocketClient.onOpen(session);
		// then
		Assertions.assertThat(kisWebSocketClient)
			.extracting("session")
			.isInstanceOf(Session.class)
			.isEqualTo(session);
	}

	@DisplayName("웹소켓 클라이언트에 세션을 해제한다")
	@Test
	void onClose() {
		// given
		kisWebSocketClient.onOpen(session);
		// when
		kisWebSocketClient.onClose(session, new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "정상적인 종료"));
		// then
		Assertions.assertThat(kisWebSocketClient)
			.extracting("session")
			.isNull();
	}

	@DisplayName("웹소켓 클라이언트가 메시지를 송신하면 응답 메시지를 받는다")
	@Test
	void sendMessage() {
		// given
		kisWebSocketClient.onOpen(session);
		given(session.isOpen()).willReturn(true);
		given(session.getAsyncRemote()).willReturn(asyncRemote);
		// when
		kisWebSocketClient.sendMessage("hello world");
		// then
		verify(asyncRemote, times(1)).sendText("hello world");
	}
}
