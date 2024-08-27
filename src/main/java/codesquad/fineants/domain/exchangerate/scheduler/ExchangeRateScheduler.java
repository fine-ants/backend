package codesquad.fineants.domain.exchangerate.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.exchangerate.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

	private final ExchangeRateService service;

	@Scheduled(cron = "0 0 * * * *") // 매일 자정에 한번씩 수행
	@Transactional
	public void updateExchangeRates() {
		service.updateExchangeRates();
	}
}
