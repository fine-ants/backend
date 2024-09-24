package co.fineants.price.domain.stockprice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@EnableAspectJAutoProxy
@Configuration
public class StockPriceConfig {

	@Bean
	public WebSocketClient standardWebSocketClient() {
		return new StandardWebSocketClient();
	}
}
