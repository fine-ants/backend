package codesquad.fineants.spring.api.kis;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.spring.api.kis.request.MessageData;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class KisMessageController {

	private final KisService kisService;

	@MessageMapping("/portfolio/{portfolioId}")
	@SendTo("/portfolio/{portfolioId}")
	public PortfolioHoldingsResponse publishPortfolioSubscription(
		@DestinationVariable Long portfolioId,
		@Payload MessageData messageData,
		SimpMessageHeaderAccessor headerAccessor) {
		log.info("portfolioId : {}, messageData : {}, sessionid : {}", portfolioId, messageData,
			headerAccessor.getSessionId());
		kisService.addPortfolioSubscription(headerAccessor.getSessionId(),
			new PortfolioSubscription(portfolioId, messageData.getTickerSymbols()));
		return kisService.publishPortfolioDetail(portfolioId)
			.exceptionally(e -> {
				log.error(e.getMessage(), e);
				return null;
			}).join();
	}
}
