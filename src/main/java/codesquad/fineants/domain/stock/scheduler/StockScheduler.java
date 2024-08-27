package codesquad.fineants.domain.stock.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.stock.service.StockService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockScheduler {

	private final StockService stockService;

	@Scheduled(cron = "${cron.expression.reload-stocks:0 0 8 * * ?}") // 매일 오전 8시 (초, 분, 시간)
	@Transactional
	public void scheduledReloadStocks() {
		stockService.reloadStocks();
	}
}
