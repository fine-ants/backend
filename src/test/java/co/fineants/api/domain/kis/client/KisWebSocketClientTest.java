package co.fineants.api.domain.kis.client;

import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.factory.WebSocketContainerProviderFactory;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

class KisWebSocketClientTest extends AbstractContainerBaseTest {

	@MockBean
	private WebSocketContainerProviderFactory webSocketContainerProviderFactory;

	@MockBean
	private WebSocketContainer webSocketContainer;

	@MockBean
	private Session session;

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

}
