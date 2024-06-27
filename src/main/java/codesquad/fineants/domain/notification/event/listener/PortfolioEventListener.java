package codesquad.fineants.domain.notification.event.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.domain.notification.event.domain.CurrentPriceEvent;
import codesquad.fineants.domain.notification.service.NotificationService;
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
		PortfolioNotifyMessagesResponse response =
			(PortfolioNotifyMessagesResponse)notificationService.notifyTargetGainAll();
		log.debug("event : {}, 목표수익률 알림 전송 결과 : {}", event, response);
	}

	// 현재가 변경 이벤트가 발생하면 포트폴리오 최대손실율에 도달하면 푸시 알림
	@Async
	@EventListener
	public void notifyPortfolioMaxLossMessages(CurrentPriceEvent event) {
		PortfolioNotifyMessagesResponse response =
			(PortfolioNotifyMessagesResponse)notificationService.notifyMaxLossAll();
		log.debug("event : {}, 최대손실율 알림 전송 결과 : {}", event, response);
	}
}
