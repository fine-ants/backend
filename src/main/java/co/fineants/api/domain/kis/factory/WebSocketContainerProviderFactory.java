package co.fineants.api.domain.kis.factory;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

public interface WebSocketContainerProviderFactory {

	default WebSocketContainer getContainerProvider() {
		return ContainerProvider.getWebSocketContainer();
	}
}
