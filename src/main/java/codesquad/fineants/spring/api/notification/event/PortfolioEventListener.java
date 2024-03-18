package codesquad.fineants.spring.api.notification.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioEventListener {

	private final NotificationService notificationService;

	// 현재가 변경 이벤트가 발생하면 포트폴리오 목표수익률에 달성하면 푸시 알림
	@Async
	@EventListener
	public void notifyTargetGain(CurrentPriceEvent event) {
		notificationService.notifyTargetGain();
	}

	// 현재가 변경 이벤트가 발생하면 포트폴리오 최대손실율에 도달하면 푸시 알림
	@Async
	@EventListener
	public void notifyPortfolioMaxLossMessages(CurrentPriceEvent event) {
		notificationService.notifyMaxLoss();
	}
}
