package codesquad.fineants.domain.gainhistory.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.gainhistory.domain.dto.response.PortfolioGainHistoryCreateResponse;
import codesquad.fineants.domain.gainhistory.service.PortfolioGainHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioGainHistoryScheduler {

	private final PortfolioGainHistoryService service;

	@Scheduled(cron = "0 0 16 * * ?") // 매일 16시에 실행
	@Transactional
	public void scheduledPortfolioGainHistory() {
		PortfolioGainHistoryCreateResponse response = service.addPortfolioGainHistory();
		log.info("포트폴리오 수익 내역 기록 결과, size = {}", response.getIds().size());
	}
}
