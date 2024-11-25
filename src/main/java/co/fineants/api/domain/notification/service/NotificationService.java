package co.fineants.api.domain.notification.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.notification.Notifiable;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notification.domain.dto.request.NotificationSaveRequest;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageResponse;
import co.fineants.api.domain.notification.domain.dto.response.PortfolioNotifyMessagesResponse;
import co.fineants.api.domain.notification.domain.dto.response.SentNotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.save.NotificationSaveResponse;
import co.fineants.api.domain.notification.domain.entity.policy.MaxLossNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.NotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetGainNotificationPolicy;
import co.fineants.api.domain.notification.domain.entity.policy.TargetPriceNotificationPolicy;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.notification.repository.NotificationSentRepository;
import co.fineants.api.domain.notification.service.disptacher.NotificationDispatcher;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotifyMessageResponse;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
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
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final NotificationSentRepository sentManager;
	private final StockTargetPriceRepository stockTargetPriceRepository;
	private final TargetGainNotificationPolicy targetGainNotificationPolicy;
	private final MaxLossNotificationPolicy maximumLossNotificationPolicy;
	private final TargetPriceNotificationPolicy targetPriceNotificationPolicy;
	private final NotificationDispatcher notificationDispatcher;

	@NotNull
	@Transactional
	public List<NotificationSaveResponse> saveNotification(List<SentNotifyMessage> messages) {
		List<NotificationSaveResponse> result = new ArrayList<>();
		messages.stream()
			.distinct()
			.forEach(message -> {
				NotificationSaveResponse response = this.saveNotification(message.toNotificationSaveRequest());
				result.add(response);
			});
		return result;
	}

	/**
	 * 포트폴리오 알림 저장
	 *
	 * @param request 알림 데이터
	 * @return 알림 저장 결과
	 */
	@Transactional
	public NotificationSaveResponse saveNotification(NotificationSaveRequest request) {
		Member member = findMember(request.getMemberId());
		return notificationRepository.save(request.toEntity(member)).toSaveResponse();
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	/**
	 * 모든 회원을 대상으로 목표 수익률을 만족하는 포트폴리오에 대해서 목표 수익률 달성 알림 푸시
	 *
	 * @return 알림 전송 결과
	 */
	@Transactional
	public NotifyMessageResponse notifyTargetGainAll() {
		// 모든 회원의 포트폴리오 중에서 입력으로 받은 종목들을 가진 포트폴리오들을 조회
		List<Notifiable> portfolios = new ArrayList<>(portfolioRepository.findAllWithAll());
		List<NotifyMessageItem> items = notifyMessage(
			portfolios,
			targetGainNotificationPolicy,
			sentManager::addTargetGainSendHistory);
		return PortfolioNotifyMessagesResponse.create(items);
	}

	/**
	 * 특정 포트폴리오의 목표 수익률 달성 알림 푸시
	 *
	 * @param portfolioId 포트폴리오 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public NotifyMessageResponse notifyTargetGain(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.findFirst()
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		Consumer<Long> sentFunction = sentManager::addTargetGainSendHistory;
		return PortfolioNotifyMessagesResponse.create(
			notifyMessage(List.of(portfolio), targetGainNotificationPolicy, sentFunction)
		);
	}

	@NotNull
	private List<NotifyMessageItem> notifyMessage(List<Notifiable> targets,
		NotificationPolicy<Notifiable> policy, Consumer<Long> sentFunction) {
		// 알림 전송
		List<SentNotifyMessage> messages = notificationDispatcher.dispatch(targets, policy);
		log.debug("알림 전송 결과: {}", messages);

		// 알림 저장
		List<NotificationSaveResponse> saveResponses = this.saveNotification(messages);
		log.debug("db 알림 저장 결과 : {}", saveResponses);

		// 전송 내역 저장
		saveResponses.stream()
			.map(NotificationSaveResponse::getIdToSentHistory)
			.map(id -> id.split(":")[1])
			.map(Long::valueOf)
			.forEach(sentFunction);

		// Response 생성
		return createNotifyMessageItems(messages, saveResponses);
	}

	@NotNull
	private List<NotifyMessageItem> createNotifyMessageItems(
		List<SentNotifyMessage> sentNotifyMessages,
		List<NotificationSaveResponse> notificationSaveResponses) {
		Map<String, String> messageIdMap = getMessageIdMap(sentNotifyMessages);

		return notificationSaveResponses.stream()
			.map(response -> {
				String messageId = messageIdMap.getOrDefault(response.getIdToSentHistory(), Strings.EMPTY);
				return response.toNotifyMessageItemWith(messageId);
			})
			.toList();
	}

	private Map<String, String> getMessageIdMap(List<SentNotifyMessage> sentNotifyMessages) {
		Map<String, String> result = new HashMap<>();
		for (SentNotifyMessage target : sentNotifyMessages) {
			result.put(target.getNotifyMessage().getIdToSentHistory(), target.getMessageId());
		}
		return result;
	}

	/**
	 * 모든 포트폴리오를 대상으로 최대 손실율에 도달하는 모든 포트폴리오에 대해서 최대 손실율 도달 알림 푸시
	 *
	 * @return 알림 전송 결과
	 */
	@Transactional
	public NotifyMessageResponse notifyMaxLossAll() {
		List<Notifiable> portfolios = new ArrayList<>(portfolioRepository.findAllWithAll());
		Consumer<Long> sentFunction = sentManager::addMaxLossSendHistory;
		return PortfolioNotifyMessagesResponse.create(
			notifyMessage(portfolios, maximumLossNotificationPolicy, sentFunction)
		);
	}

	/**
	 * 특정 포트폴리오의 최대 손실율 달성 알림 푸시
	 *
	 * @param portfolioId 포트폴리오 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public NotifyMessageResponse notifyMaxLoss(Long portfolioId) {
		Portfolio portfolio = portfolioRepository.findByPortfolioIdWithAll(portfolioId).stream()
			.findAny()
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
		Consumer<Long> sentFunction = sentManager::addMaxLossSendHistory;
		return PortfolioNotifyMessagesResponse.create(
			notifyMessage(List.of(portfolio), maximumLossNotificationPolicy, sentFunction)
		);
	}

	/**
	 * 모든 회원을 대상으로 특정 종목들에 대한 종목 지정가 알림 발송
	 *
	 * @param tickerSymbols 종목의 티커 심볼 리스트
	 * @return 알림 전송 결과
	 */
	@Transactional
	public NotifyMessageResponse notifyTargetPriceToAllMember(List<String> tickerSymbols) {
		log.debug("tickerSymbols : {}", tickerSymbols);

		List<Notifiable> targetPrices = stockTargetPriceRepository.findAllByTickerSymbols(tickerSymbols)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
		log.debug("targetPrices : {}", targetPrices);

		Consumer<Long> sentFunction = sentManager::addTargetPriceSendHistory;
		return TargetPriceNotifyMessageResponse.create(
			notifyMessage(targetPrices, targetPriceNotificationPolicy, sentFunction)
		);
	}

	/**
	 * 특정 회원을 대상으로 종목 지정가 알림 발송
	 *
	 * @param memberId 회원의 등록번호
	 * @return 알림 전송 결과
	 */
	@Transactional
	public NotifyMessageResponse notifyTargetPrice(Long memberId) {
		List<Notifiable> targetPrices = stockTargetPriceRepository.findAllByMemberId(memberId)
			.stream()
			.map(StockTargetPrice::getTargetPriceNotifications)
			.flatMap(Collection::stream)
			.map(Notifiable.class::cast)
			.toList();
		Consumer<Long> sentFunction = sentManager::addTargetPriceSendHistory;
		return TargetPriceNotifyMessageResponse.create(
			notifyMessage(targetPrices, targetPriceNotificationPolicy, sentFunction)
		);
	}
}
