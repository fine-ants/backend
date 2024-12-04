package co.fineants.api.domain.notification.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.Message;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.common.notification.PortfolioMaximumLossNotifiable;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.common.notification.StockTargetPriceNotifiable;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.fcm.service.FirebaseMessagingService;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.policy.MaxLossNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetGainNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetPriceNotificationPolicy;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {
	private final PortfolioRepository portfolioRepository;
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final NotificationSentRepository sentManager;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetGainNotificationPolicy targetGainNotificationPolicy;
	private final MaxLossNotificationPolicy maximumLossNotificationPolicy;
	private final TargetPriceNotificationPolicy targetPriceNotificationPolicy;
	private final FcmService fcmService;
	private final FirebaseMessagingService firebaseMessagingService;
	private final PortfolioCalculator portfolioCalculator;

	/**
	 * 특정 포트폴리오의 목표 수익률 달성 알림 푸시
	 *
	 * @param portfolioId 포트폴리오 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetGain(Long portfolioId) {
		Notifiable notifiable = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.findAny()
			.map(p -> {
				boolean isReachedTargetGain = portfolioCalculator.reachedTargetGainBy(p);
				return PortfolioTargetGainNotifiable.from(p, isReachedTargetGain);
			})
			.map(Notifiable.class::cast)
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		return notifyTargetGainAll(List.of(notifiable));
	}

	@Transactional
	public List<NotifyMessageItem> notifyTargetGainAll() {
		List<Notifiable> notifiableList = portfolioRepository.findAllWithAll().stream()
			.map(portfolio -> {
				boolean isReachedTargetGain = portfolioCalculator.reachedTargetGainBy(portfolio);
				return PortfolioTargetGainNotifiable.from(portfolio, isReachedTargetGain);
			})
			.map(Notifiable.class::cast)
			.toList();
		return notifyTargetGainAll(notifiableList);
	}

	private List<NotifyMessageItem> notifyTargetGainAll(List<Notifiable> notifiableList) {
		List<NotifyMessage> notifyMessages = notifiableList.stream()
			.filter(targetGainNotificationPolicy::isSatisfied)
			.map(notifiable -> fcmService.findTokens(notifiable.fetchMemberId()).stream()
				.map(token -> targetGainNotificationPolicy.apply(notifiable, token))
				.flatMap(Optional::stream)
				.toList())
			.flatMap(Collection::stream)
			.toList();

		// 만족하는 포트폴리오를 대상으로 알림 데이터 생성 & 알림 전송
		Map<NotifyMessage, List<String>> messageIdsMap = new HashMap<>();
		for (NotifyMessage notifyMessage : notifyMessages) {
			Message message = notifyMessage.toMessage();
			String messageId = firebaseMessagingService.send(message).orElse(Strings.EMPTY);
			messageIdsMap.computeIfAbsent(notifyMessage, key -> new ArrayList<>())
				.add(messageId);
		}
		List<NotifyMessage> sentNotifyMessages = messageIdsMap.entrySet().stream()
			.map(entry -> entry.getKey().withMessageId(entry.getValue()))
			.toList();

		// 알림 전송에 실패한 전송 메시지에 대해서 FCM 토큰 삭제
		messageIdsMap.forEach((notifyMessage, messageIds) -> messageIds.stream()
			.filter(Strings::isBlank)
			.forEach(blankMessageId -> notifyMessage.deleteTokenBy(fcmService)));

		// 알림 저장
		List<Notification> notifications = sentNotifyMessages.stream()
			.map(notifyMessage -> {
				Member member = memberRepository.findById(notifyMessage.getMemberId())
					.orElseThrow(() -> notFoundMember(notifyMessage));
				return notifyMessage.toEntity(member);
			})
			.map(notificationRepository::save)
			.toList();

		// 전송 내역 저장
		notifications.stream()
			.map(Notification::getId)
			.forEach(sentManager::addTargetGainSendHistory);

		// 결과 객체 생성
		return notifications.stream()
			.map(response -> {
				String idToSentHistory = response.getIdToSentHistory();
				List<String> messageIds = sentNotifyMessages.stream()
					.filter(notifyMessage -> notifyMessage.getIdToSentHistory().equals(idToSentHistory))
					.map(NotifyMessage::getMessageIds)
					.findAny()
					.orElse(Collections.emptyList());
				return response.toNotifyMessageItemWith(messageIds);
			})
			.toList();
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
		Notifiable notifiable = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.findAny()
			.map(portfolio -> {
				boolean isReached = portfolioCalculator.reachedMaximumLossBy(portfolio);
				return PortfolioMaximumLossNotifiable.from(portfolio, isReached);
			})
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		return notifyMaxLossAll(List.of(notifiable));
	}

	/**
	 * 모든 포트폴리오를 대상으로 최대 손실율에 도달하는 모든 포트폴리오에 대해서 최대 손실율 도달 알림 푸시
	 *
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyMaxLossAll() {
		List<Notifiable> notifiableList = portfolioRepository.findAllWithAll().stream()
			.map(portfolio -> {
				boolean isReached = portfolioCalculator.reachedMaximumLossBy(portfolio);
				return PortfolioMaximumLossNotifiable.from(portfolio, isReached);
			})
			.map(Notifiable.class::cast)
			.toList();
		return notifyMaxLossAll(notifiableList);
	}

	private List<NotifyMessageItem> notifyMaxLossAll(List<Notifiable> notifiableList) {
		List<NotifyMessage> notifyMessages = notifiableList.stream()
			.filter(maximumLossNotificationPolicy::isSatisfied)
			.map(notifiable -> fcmService.findTokens(notifiable.fetchMemberId()).stream()
				.map(token -> maximumLossNotificationPolicy.apply(notifiable, token))
				.flatMap(Optional::stream)
				.toList())
			.flatMap(Collection::stream)
			.toList();

		// 만족하는 포트폴리오를 대상으로 알림 데이터 생성 & 알림 전송
		Map<NotifyMessage, List<String>> messageIdsMap = new HashMap<>();
		for (NotifyMessage notifyMessage : notifyMessages) {
			Message message = notifyMessage.toMessage();
			String messageId = firebaseMessagingService.send(message).orElse(Strings.EMPTY);
			messageIdsMap.computeIfAbsent(notifyMessage, key -> new ArrayList<>())
				.add(messageId);
		}
		List<NotifyMessage> sentNotifyMessages = messageIdsMap.entrySet().stream()
			.map(entry -> entry.getKey().withMessageId(entry.getValue()))
			.toList();

		// 알림 전송에 실패한 전송 메시지에 대해서 FCM 토큰 삭제
		messageIdsMap.forEach((notifyMessage, messageIds) -> messageIds.stream()
			.filter(Strings::isBlank)
			.forEach(blankMessageId -> notifyMessage.deleteTokenBy(fcmService)));

		// 알림 저장
		List<Notification> notifications = sentNotifyMessages.stream()
			.map(notifyMessage -> {
				Member member = memberRepository.findById(notifyMessage.getMemberId())
					.orElseThrow(() -> notFoundMember(notifyMessage));
				return notifyMessage.toEntity(member);
			})
			.map(notificationRepository::save)
			.toList();

		// 전송 내역 저장
		notifications.stream()
			.map(Notification::getId)
			.forEach(sentManager::addMaxLossSendHistory);

		// 결과 객체 생성
		return notifications.stream()
			.map(response -> {
				String idToSentHistory = response.getIdToSentHistory();
				List<String> messageIds = sentNotifyMessages.stream()
					.filter(notifyMessage -> notifyMessage.getIdToSentHistory().equals(idToSentHistory))
					.map(NotifyMessage::getMessageIds)
					.findAny()
					.orElse(Collections.emptyList());
				return response.toNotifyMessageItemWith(messageIds);
			})
			.toList();
	}

	/**
	 * 특정 회원을 대상으로 종목 지정가 알림 발송
	 *
	 * @param memberId 회원의 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetPrice(Long memberId) {
		List<Notifiable> notifiableList = stockTargetPriceRepository.findAllByMemberId(memberId)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.sorted(Comparator.comparingLong(TargetPriceNotification::getId))
			.map(targetPriceNotification -> StockTargetPriceNotifiable.from(targetPriceNotification))
			.map(Notifiable.class::cast)
			.toList();
		return notifyTargetPriceAll(notifiableList);
	}

	/**
	 * 모든 회원을 대상으로 특정 종목들에 대한 종목 지정가 알림 발송
	 *
	 * @param tickerSymbols 종목의 티커 심볼 리스트
	 * @return 알림 전송 결과
	 */
	@Transactional
	public List<NotifyMessageItem> notifyTargetPriceBy(List<String> tickerSymbols) {
		List<Notifiable> notifiableList = stockTargetPriceRepository.findAllByTickerSymbols(
				tickerSymbols)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.map(targetPriceNotification -> StockTargetPriceNotifiable.from(targetPriceNotification))
			.map(Notifiable.class::cast)
			.toList();
		log.debug("notifiableList : {}", notifiableList);
		return notifyTargetPriceAll(notifiableList);
	}

	private List<NotifyMessageItem> notifyTargetPriceAll(List<Notifiable> notifiableList) {
		List<NotifyMessage> notifyMessages = notifiableList.stream()
			.filter(targetPriceNotificationPolicy::isSatisfied)
			.map(notifiable -> fcmService.findTokens(notifiable.fetchMemberId()).stream()
				.map(token -> targetPriceNotificationPolicy.apply(notifiable, token))
				.flatMap(Optional::stream)
				.toList())
			.flatMap(Collection::stream)
			.toList();

		// 만족하는 지정가 알림을 대상으로 알림 데이터 생성 & 알림 전송
		Map<NotifyMessage, List<String>> messageIdsMap = new HashMap<>();
		for (NotifyMessage notifyMessage : notifyMessages) {
			Message message = notifyMessage.toMessage();
			String messageId = firebaseMessagingService.send(message).orElse(Strings.EMPTY);
			messageIdsMap.computeIfAbsent(notifyMessage, key -> new ArrayList<>())
				.add(messageId);
		}
		List<NotifyMessage> sentNotifyMessages = messageIdsMap.entrySet().stream()
			.map(entry -> entry.getKey().withMessageId(entry.getValue()))
			.sorted()
			.toList();

		// 알림 전송에 실패한 전송 메시지에 대해서 FCM 토큰 삭제
		messageIdsMap.forEach((notifyMessage, messageIds) -> messageIds.stream()
			.filter(Strings::isBlank)
			.forEach(blankMessageId -> notifyMessage.deleteTokenBy(fcmService)));

		// 알림 저장
		List<Notification> notifications = sentNotifyMessages.stream()
			.map(notifyMessage -> {
				Member member = memberRepository.findById(notifyMessage.getMemberId())
					.orElseThrow(() -> notFoundMember(notifyMessage));
				return notifyMessage.toEntity(member);
			})
			.map(notificationRepository::save)
			.toList();

		// 전송 내역 저장
		notifications.stream()
			.map(Notification::getId)
			.forEach(sentManager::addTargetPriceSendHistory);

		// 결과 객체 생성
		return notifications.stream()
			.map(response -> {
				String idToSentHistory = response.getIdToSentHistory();
				List<String> messageIds = sentNotifyMessages.stream()
					.filter(notifyMessage -> notifyMessage.getIdToSentHistory().equals(idToSentHistory))
					.map(NotifyMessage::getMessageIds)
					.findAny()
					.orElse(Collections.emptyList());
				return response.toNotifyMessageItemWith(messageIds);
			})
			.sorted()
			.toList();
	}
}
