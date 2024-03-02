package codesquad.fineants.spring.api.purchase_history;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.spring.api.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.notification.service.NotificationService;
import codesquad.fineants.spring.api.purchase_history.event.NotificationEventPublisher;
import codesquad.fineants.spring.api.purchase_history.event.PublishEvent;
import codesquad.fineants.spring.api.purchase_history.event.PushNotificationEvent;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryModifyRequest;
import codesquad.fineants.spring.api.purchase_history.response.PurchaseHistoryCreateResponse;
import codesquad.fineants.spring.api.purchase_history.response.PurchaseHistoryDeleteResponse;
import codesquad.fineants.spring.api.purchase_history.response.PurchaseHistoryModifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PurchaseHistoryService {
	private final PurchaseHistoryRepository repository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final NotificationEventPublisher publisher;
	private final NotificationService notificationService;

	@Transactional
	@PublishEvent(eventType = PushNotificationEvent.class, params = "#{T(codesquad.fineants.spring.api.purchase_history.event.SendableParameter).create(portfolioId, memberId)}")
	public PurchaseHistoryCreateResponse addPurchaseHistory(
		PurchaseHistoryCreateRequest request,
		Long portfolioId,
		Long portfolioHoldingId,
		Long memberId) {
		log.info("매입이력 추가 서비스 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);

		PortfolioHolding portfolioHolding = findPortfolioHolding(portfolioHoldingId);
		Portfolio portfolio = portfolioHolding.getPortfolio();
		if (!portfolio.getId().equals(portfolioId)) {
			throw new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
		}

		Double purchasedPrice = request.getNumShares() * request.getPurchasePricePerShare();
		if (portfolio.calculateTotalInvestmentAmount() + purchasedPrice > portfolio.getBudget()) {
			throw new FineAntsException(PortfolioErrorCode.TOTAL_INVESTMENT_PRICE_EXCEEDS_BUDGET);
		}

		PurchaseHistory newPurchaseHistory = repository.save(request.toEntity(portfolioHolding));
		log.info("매입이력 저장 결과 : newPurchaseHistory={}", newPurchaseHistory);

		// publisher.publishEvent(portfolioId, memberId);
		// NotifyPortfolioMessagesResponse response = notificationService.notifyPortfolioTargetGainMessages(portfolioId,
		// 	memberId);
		// log.info("알림 저장 결과 : {}", response);
		return PurchaseHistoryCreateResponse.from(newPurchaseHistory, portfolioId, memberId);
	}

	@Transactional
	@PublishEvent(eventType = PushNotificationEvent.class, params = "#{T(codesquad.fineants.spring.api.purchase_history.event.SendableParameter).create(portfolioId, memberId)}")
	public PurchaseHistoryModifyResponse modifyPurchaseHistory(PurchaseHistoryModifyRequest request,
		Long portfolioHoldingId, Long purchaseHistoryId, Long portfolioId, Long memberId) {
		log.info("매입 내역 수정 서비스 요청 : request={}, portfolioHoldingId={}, purchaseHistoryId={}", request,
			portfolioHoldingId, purchaseHistoryId);
		PortfolioHolding portfolioHolding = findPortfolioHolding(portfolioHoldingId);
		PurchaseHistory originalPurchaseHistory = findPurchaseHistory(purchaseHistoryId);
		PurchaseHistory changePurchaseHistory = request.toEntity(portfolioHolding);

		PurchaseHistory changedPurchaseHistory = originalPurchaseHistory.change(changePurchaseHistory);
		PurchaseHistoryModifyResponse response = PurchaseHistoryModifyResponse.from(
			changedPurchaseHistory,
			portfolioId,
			memberId
		);
		log.info("매입 내역 수정 결과 : response={}", response);
		return response;
	}

	@Transactional
	@PublishEvent(eventType = PushNotificationEvent.class, params = "#{T(codesquad.fineants.spring.api.purchase_history.event.SendableParameter).create(portfolioId, memberId)}")
	public PurchaseHistoryDeleteResponse deletePurchaseHistory(Long portfolioHoldingId, Long purchaseHistoryId,
		Long portfolioId, Long memberId) {
		log.info("매입 내역 삭제 서비스 요청 : portfolioHoldingId={}, purchaseHistoryId={}", portfolioHoldingId,
			purchaseHistoryId);
		PurchaseHistory deletePurchaseHistory = findPurchaseHistory(purchaseHistoryId);
		repository.deleteById(purchaseHistoryId);
		return PurchaseHistoryDeleteResponse.from(deletePurchaseHistory, portfolioId, memberId);
	}

	private PortfolioHolding findPortfolioHolding(Long portfolioHoldingId) {
		return portfolioHoldingRepository.findById(portfolioHoldingId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING));
	}

	private PurchaseHistory findPurchaseHistory(Long purchaseHistoryId) {
		return repository.findById(purchaseHistoryId)
			.orElseThrow(() -> new NotFoundResourceException(PurchaseHistoryErrorCode.NOT_FOUND_PURCHASE_HISTORY));
	}
}
