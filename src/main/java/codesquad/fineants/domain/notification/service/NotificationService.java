package codesquad.fineants.domain.notification.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.fcm_token.service.FcmService;
import codesquad.fineants.domain.fcm_token.service.FirebaseMessagingService;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notification.domain.dto.request.PortfolioNotificationRequest;
import codesquad.fineants.domain.notification.domain.dto.response.NotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotificationResponse;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessageItem;
import codesquad.fineants.domain.notification.domain.dto.response.PortfolioNotifyMessagesResponse;
import codesquad.fineants.domain.notification.domain.dto.response.SentNotifyMessage;
import codesquad.fineants.domain.notification.domain.dto.response.TargetPriceNotificationResponse;
import codesquad.fineants.domain.notification.domain.entity.Notification;
import codesquad.fineants.domain.notification.domain.entity.policy.NotificationPolicy;
import codesquad.fineants.domain.notification.domain.entity.policy.max_loss.MaxLossNotificationPolicy;
import codesquad.fineants.domain.notification.domain.entity.policy.target_gain.TargetGainNotificationPolicy;
import codesquad.fineants.domain.notification.domain.entity.policy.target_price.TargetPriceNotificationPolicy;
import codesquad.fineants.domain.notification.repository.NotificationRepository;
import codesquad.fineants.domain.notification.repository.NotificationSentRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.stock_target_price.domain.dto.request.StockNotificationRequest;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotifyMessageItem;
import codesquad.fineants.domain.stock_target_price.domain.dto.response.TargetPriceNotifyMessageResponse;
import codesquad.fineants.domain.stock_target_price.domain.entity.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.domain.entity.TargetPriceNotification;
import codesquad.fineants.domain.stock_target_price.repository.StockTargetPriceRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {
	private final PortfolioRepository portfolioRepository;
	private final FcmService fcmService;
	private final FirebaseMessagingService firebaseMessagingService;
	private final NotificationRepository notificationRepository;
	private final MemberRepository memberRepository;
	private final CurrentPriceRepository currentPriceRepository;
	private final NotificationSentRepository sentManager;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetGainNotificationPolicy targetGainNotificationPolicy;
	private final MaxLossNotificationPolicy maximumLossNotificationPolicy;
	private final TargetPriceNotificationPolicy targetPriceNotificationPolicy;

	// 알림 저장
	@Transactional
	public PortfolioNotificationResponse saveNotification(PortfolioNotificationRequest request) {
		Member member = findMember(request.getMemberId());
		Notification notification = notificationRepository.save(
			request.toEntity(member)
		);
		return PortfolioNotificationResponse.from(notification);
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	// 알림 저장
	@Transactional
	public TargetPriceNotificationResponse saveNotification(StockNotificationRequest request) {
		Member member = findMember(request.getMemberId());
		Notification notification = notificationRepository.save(
			request.toEntity(member)
		);
		return TargetPriceNotificationResponse.from(notification);
	}

	// 모든 회원을 대상으로 목표 수익률 달성 알림 푸시
	@Transactional
	public PortfolioNotifyMessagesResponse notifyTargetGain() {
		// 모든 회원의 포트폴리오 중에서 입력으로 받은 종목들을 가진 포트폴리오들을 조회
		List<Portfolio> portfolios = portfolioRepository.findAllWithAll().stream()
			.peek(portfolio -> portfolio.applyCurrentPriceAllHoldingsBy(currentPriceRepository))
			.collect(Collectors.toList());
		Function<PortfolioNotificationResponse, PortfolioNotificationResponse> sentFunction = item -> {
			sentManager.addTargetGainSendHistory(Long.valueOf(item.getReferenceId()));
			return item;
		};
		return notifyMessage(portfolios, targetGainNotificationPolicy, sentFunction);
	}

	// 특정 포트폴리오의 목표 수익률 달성 알림 푸시
	@Transactional
	@Secured("ROLE_USER")
	public PortfolioNotifyMessagesResponse notifyTargetGainBy(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.peek(p -> p.applyCurrentPriceAllHoldingsBy(currentPriceRepository))
			.findFirst()
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		Function<PortfolioNotificationResponse, PortfolioNotificationResponse> sentFunction = item -> {
			sentManager.addTargetGainSendHistory(Long.valueOf(item.getReferenceId()));
			return item;
		};
		return notifyMessage(List.of(portfolio), targetGainNotificationPolicy, sentFunction);
	}

	// 모든 트폴리오의 최대 손실율 달성 알림 푸시
	@Transactional
	public PortfolioNotifyMessagesResponse notifyMaxLoss() {
		List<Portfolio> portfolios = portfolioRepository.findAllWithAll().stream()
			.peek(portfolio -> portfolio.applyCurrentPriceAllHoldingsBy(currentPriceRepository))
			.collect(Collectors.toList());
		Function<PortfolioNotificationResponse, PortfolioNotificationResponse> sentFunction = item -> {
			sentManager.addMaxLossSendHistory(Long.valueOf(item.getReferenceId()));
			return item;
		};
		return notifyMessage(portfolios, maximumLossNotificationPolicy, sentFunction);
	}

	// 특정 포트폴리오의 최대 손실율 달성 알림 푸시
	@Transactional
	@Secured("ROLE_USER")
	public PortfolioNotifyMessagesResponse notifyMaxLoss(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.stream().peek(p -> p.applyCurrentPriceAllHoldingsBy(currentPriceRepository))
			.findAny()
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		Function<PortfolioNotificationResponse, PortfolioNotificationResponse> sentFunction = item -> {
			sentManager.addMaxLossSendHistory(Long.valueOf(item.getReferenceId()));
			return item;
		};
		return notifyMessage(List.of(portfolio), maximumLossNotificationPolicy, sentFunction);
	}

	private PortfolioNotifyMessagesResponse notifyMessage(
		List<Portfolio> portfolios,
		NotificationPolicy<Portfolio> policy,
		Function<PortfolioNotificationResponse, PortfolioNotificationResponse> sentFunction) {
		// 포트폴리오별 FCM 토큰 리스트맵 생성
		Map<Portfolio, List<String>> fcmTokenMap = portfolios.stream()
			.collect(Collectors.toMap(
				portfolio -> portfolio,
				portfolio -> fcmService.findTokens(portfolio.getMember().getId())
			));
		// 각 토큰에 전송할 NotifyMessage 객체 생성
		List<NotifyMessage> notifyMessages = new ArrayList<>();
		for (Map.Entry<Portfolio, List<String>> entry : fcmTokenMap.entrySet()) {
			Portfolio p = entry.getKey();
			for (String token : entry.getValue()) {
				policy.apply(p, p.getMember().getNotificationPreference(), token)
					.ifPresent(notifyMessages::add);
			}
		}

		// 알림 저장
		List<PortfolioNotificationResponse> portfolioNotificationResponses = notifyMessages.stream()
			.distinct()
			.map(message -> this.saveNotification(PortfolioNotificationRequest.from(message)))
			// 발송 이력 저장
			.map(sentFunction)
			.collect(Collectors.toList());
		log.debug("portfolioNotificationResponses : {}", portfolioNotificationResponses);

		// 알림 전송
		Map<String, String> messageIdMap = new HashMap<>();
		List<SentNotifyMessage> sentNotifyMessages = new ArrayList<>();
		for (NotifyMessage notifyMessage : notifyMessages) {
			firebaseMessagingService.send(notifyMessage.toMessage())
				.ifPresentOrElse(
					messageId -> {
						messageIdMap.put(notifyMessage.getReferenceId(), messageId);
						sentNotifyMessages.add(SentNotifyMessage.create(notifyMessage, messageId));
					},
					() -> fcmService.deleteToken(notifyMessage.getToken()));
		}
		log.info("포트폴리오 알림 전송 결과, sentNotifyMessage={}", sentNotifyMessages);

		List<PortfolioNotifyMessageItem> items = portfolioNotificationResponses.stream()
			.map(response -> {
				String messageId = messageIdMap.getOrDefault(response.getReferenceId(), Strings.EMPTY);
				return PortfolioNotifyMessageItem.from(response, messageId);
			})
			.collect(Collectors.toList());
		return PortfolioNotifyMessagesResponse.create(items);
	}

	// 모든 회원을 대상으로 특정 티커 심볼들에 대한 종목 지정가 알림 발송
	@Transactional
	public TargetPriceNotifyMessageResponse notifyTargetPriceBy(List<String> tickerSymbols) {
		List<TargetPriceNotification> targetPrices = stockTargetPriceRepository.findAllByTickerSymbols(tickerSymbols)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		Function<TargetPriceNotificationResponse, TargetPriceNotificationResponse> sentFunction = item -> {
			sentManager.addTargetPriceSendHistory(item.getTargetPriceNotificationId());
			return item;
		};
		return notifyTargetPrice(
			targetPrices,
			targetPriceNotificationPolicy,
			sentFunction
		);
	}

	// 회원에 대한 종목 지정가 알림 발송
	@Transactional
	public TargetPriceNotifyMessageResponse notifyTargetPriceBy(Long memberId) {
		List<TargetPriceNotification> targetPrices = stockTargetPriceRepository.findAllByMemberId(memberId)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		Function<TargetPriceNotificationResponse, TargetPriceNotificationResponse> sentFunction = item -> {
			sentManager.addTargetPriceSendHistory(item.getTargetPriceNotificationId());
			return item;
		};
		return notifyTargetPrice(
			targetPrices,
			targetPriceNotificationPolicy,
			sentFunction
		);
	}

	private TargetPriceNotifyMessageResponse notifyTargetPrice(
		List<TargetPriceNotification> targetPrices,
		NotificationPolicy<TargetPriceNotification> policy,
		Function<TargetPriceNotificationResponse, TargetPriceNotificationResponse> sentFunction) {
		log.debug("종목 지정가 알림 발송 서비스 시작, targetPrices={}", targetPrices);

		// 지정가 알림별 FCM 토큰 리스트맵 생성
		Map<TargetPriceNotification, List<String>> fcmTokenMap = targetPrices.stream()
			.collect(Collectors.toMap(
				t -> t,
				t -> fcmService.findTokens(t.getStockTargetPrice().getMember().getId())
			));
		// 각 토큰에 전송할 NotifyMessage 객체 생성
		List<NotifyMessage> notifyMessages = new ArrayList<>();
		for (Map.Entry<TargetPriceNotification, List<String>> entry : fcmTokenMap.entrySet()) {
			TargetPriceNotification t = entry.getKey();
			for (String token : entry.getValue()) {
				policy.apply(t, t.getStockTargetPrice().getMember().getNotificationPreference(), token)
					.ifPresent(notifyMessages::add);
			}
		}
		log.debug("notifyMessage : {}", notifyMessages);

		// 알림 저장
		List<TargetPriceNotificationResponse> notificationSaveResponse = notifyMessages.stream()
			.distinct()
			// 알림 저장
			.map(message -> this.saveNotification(StockNotificationRequest.from(message)))
			// 발송 이력 저장
			.map(sentFunction)
			.sorted(Comparator.comparing(TargetPriceNotificationResponse::getReferenceId))
			.collect(Collectors.toList());
		log.debug("종목 지정가 알림 발송 서비스 결과, notificationSaveResponse={}", notificationSaveResponse);

		// 알림 전송
		Map<String, String> messageIdMap = new HashMap<>();
		List<SentNotifyMessage> sentNotifyMessages = new ArrayList<>();
		for (NotifyMessage notifyMessage : notifyMessages) {
			firebaseMessagingService.send(notifyMessage.toMessage())
				.ifPresentOrElse(
					messageId -> {
						messageIdMap.put(notifyMessage.getReferenceId(), messageId);
						sentNotifyMessages.add(SentNotifyMessage.create(notifyMessage, messageId));
					},
					() -> fcmService.deleteToken(notifyMessage.getToken()));
		}
		log.info("종목 지정가 FCM 알림 발송 결과, sentNotifyMessage={}", sentNotifyMessages);

		// 응답 리스폰스 생성
		List<TargetPriceNotifyMessageItem> items = notificationSaveResponse.stream()
			.map(response -> {
				String messageId = messageIdMap.getOrDefault(response.getReferenceId(), Strings.EMPTY);
				return TargetPriceNotifyMessageItem.from(response, messageId);
			})
			.collect(Collectors.toList());

		return TargetPriceNotifyMessageResponse.from(items);
	}

}
