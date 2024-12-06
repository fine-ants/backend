package co.fineants.api.domain.notification.service;

import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final NotifyMessageFactory notifyMessageFactory;
	private final NotificationSender notificationSender;
	private final TargetGainNotificationStrategy targetGainNotificationStrategy;
	private final MaximumLossNotificationStrategy maximumLossNotificationStrategy;
	private final TargetPriceNotificationStrategy targetPriceNotificationStrategy;
	private final NotifiableFactory notifiableFactory;
	private final PortfolioCalculator calculator;

	@Transactional
	public List<NotifyMessageItem> notifyTargetGain(Long portfolioId) {
		return notifyAll(List.of(notifiableFactory.getPortfolio(portfolioId, calculator::reachedTargetGainBy)),
			PORTFOLIO_TARGET_GAIN);
	}

	@Transactional
	public List<NotifyMessageItem> notifyTargetGainAll() {
		return notifyAll(notifiableFactory.getAllPortfolios(calculator::reachedTargetGainBy), PORTFOLIO_TARGET_GAIN);
	}

	@Transactional
	public List<NotifyMessageItem> notifyMaxLoss(Long portfolioId) {
		return notifyAll(List.of(notifiableFactory.getPortfolio(portfolioId, calculator::reachedMaximumLossBy)),
			PORTFOLIO_MAX_LOSS);
	}

	@Transactional
	public List<NotifyMessageItem> notifyMaxLossAll() {
		return notifyAll(notifiableFactory.getAllPortfolios(calculator::reachedMaximumLossBy), PORTFOLIO_MAX_LOSS);
	}

	@Transactional
	public List<NotifyMessageItem> notifyTargetPrice(Long memberId) {
		return notifyAll(notifiableFactory.getAllTargetPriceNotificationsBy(memberId), STOCK_TARGET_PRICE);
	}

	@Transactional
	public List<NotifyMessageItem> notifyTargetPriceBy(List<String> tickerSymbols) {
		return notifyAll(notifiableFactory.getAllTargetPriceNotificationsBy(tickerSymbols), STOCK_TARGET_PRICE);
	}

	private List<NotifyMessageItem> notifyAll(List<Notifiable> notifiableList, NotificationType type) {
		return switch (type) {
			case PORTFOLIO_TARGET_GAIN -> notifyMessages(notifiableList, targetGainNotificationStrategy);
			case PORTFOLIO_MAX_LOSS -> notifyMessages(notifiableList, maximumLossNotificationStrategy);
			case STOCK_TARGET_PRICE -> notifyMessages(notifiableList, targetPriceNotificationStrategy);
		};
	}

	private List<NotifyMessageItem> notifyMessages(
		List<Notifiable> data,
		NotificationStrategy strategy) {
		// 알림 조건을 만족하는 데이터를 생성
		List<NotifyMessage> notifyMessages = notifyMessageFactory.generate(data, strategy.getPolicy());

		// 만족하는 포트폴리오를 대상으로 알림 데이터 생성 & 알림 전송
		List<NotifyMessage> sentNotifyMessages = notificationSender.send(notifyMessages);

		// 알림 전송에 실패한 전송 메시지에 대해서 FCM 토큰 삭제
		notificationSender.deleteTokensForFailedMessagesIn(sentNotifyMessages);

		// 알림 저장
		List<Notification> notifications = saveNotifications(sentNotifyMessages);

		// 전송 내역 저장
		notifications.forEach(strategy.getSendHistory());

		// 결과 객체 생성
		return notifications.stream()
			.map(strategy.getMapper())
			.sorted()
			.toList();
	}

	@NotNull
	private List<Notification> saveNotifications(List<NotifyMessage> notifyMessages) {
		List<Notification> notifications = notifyMessages.stream()
			.map(notifyMessage -> {
				Member member = memberRepository.findById(notifyMessage.getMemberId())
					.orElseThrow(() -> notFoundMember(notifyMessage));
				return notifyMessage.toEntity(member);
			})
			.toList();
		return notificationRepository.saveAll(notifications);
	}

	@NotNull
	private static IllegalArgumentException notFoundMember(NotifyMessage notifyMessage) {
		return new IllegalArgumentException("not found member, memberId=" + notifyMessage.getMemberId());
	}
}
