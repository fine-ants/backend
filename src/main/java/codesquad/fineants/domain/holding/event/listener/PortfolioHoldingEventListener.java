package codesquad.fineants.domain.holding.event.listener;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.holding.event.domain.PortfolioHoldingEvent;
import codesquad.fineants.domain.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioHoldingEventListener {

	private final KisService kisService;

	@Async
	@EventListener
	public void listenPortfolioHolding(PortfolioHoldingEvent event) {
		log.info("포트폴리오 종목 추가로 인한 종목 현재가 및 종가 갱신 수행");
		String tickerSymbol = event.getTickerSymbol();
		kisService.refreshStockCurrentPrice(List.of(tickerSymbol));
		kisService.refreshLastDayClosingPrice(List.of(tickerSymbol));
	}
}
