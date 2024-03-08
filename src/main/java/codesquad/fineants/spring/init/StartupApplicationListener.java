package codesquad.fineants.spring.init;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.service.KisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"local", "dev", "analyze"})
@Component
@RequiredArgsConstructor
@Slf4j
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	private final KisService kisService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 시작");
		kisService.refreshAllStockCurrentPrice();
		kisService.refreshAllLastDayClosingPrice();
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 종료");
	}
}
