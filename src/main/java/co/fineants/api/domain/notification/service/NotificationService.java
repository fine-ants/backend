package co.fineants.api.domain.notification.service;

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
import co.fineants.api.domain.notification.repository.NotificationRepository;
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

	@Transactional
	public List<NotifyMessageItem> notifyTargetGain(Long portfolioId) {
		return notifyTargetGainAll(List.of(notifiableFactory.getPortfolio(portfolioId)));
	}

	@Transactional
	public List<NotifyMessageItem> notifyTargetGainAll() {
		return notifyTargetGainAll(notifiableFactory.getAllPortfolios());
	}

	private List<NotifyMessageItem> notifyTargetGainAll(List<Notifiable> notifiableList) {
		return notifyMessages(notifiableList, targetGainNotificationStrategy);
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

	/**
	 * 특정 포트폴리오의 최대 손실율 달성 알림 푸시
	 *
	 * @param portfolioId 포트폴리오 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyMaxLoss(Long portfolioId) {
		return notifyMaxLossAll(List.of(notifiableFactory.getPortfolio(portfolioId)));
	}

	/**
	 * 모든 포트폴리오를 대상으로 최대 손실율에 도달하는 모든 포트폴리오에 대해서 최대 손실율 도달 알림 푸시
	 *
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyMaxLossAll() {
		return notifyMaxLossAll(notifiableFactory.getAllPortfolios());
	}

	private List<NotifyMessageItem> notifyMaxLossAll(List<Notifiable> notifiableList) {
		return notifyMessages(notifiableList, maximumLossNotificationStrategy);
	}

	/**
	 * 특정 회원을 대상으로 종목 지정가 알림 발송
	 *
	 * @param memberId 회원의 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetPrice(Long memberId) {
		return notifyTargetPriceAll(notifiableFactory.getAllTargetPriceNotificationsBy(memberId));
	}

	/**
	 * 모든 회원을 대상으로 특정 종목들에 대한 종목 지정가 알림 발송
	 *
	 * @param tickerSymbols 종목의 티커 심볼 리스트
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetPriceBy(List<String> tickerSymbols) {
		return notifyTargetPriceAll(notifiableFactory.getAllTargetPriceNotificationsBy(tickerSymbols));
	}

	private List<NotifyMessageItem> notifyTargetPriceAll(List<Notifiable> notifiableList) {
		return notifyMessages(notifiableList, targetPriceNotificationStrategy);
	}
}
