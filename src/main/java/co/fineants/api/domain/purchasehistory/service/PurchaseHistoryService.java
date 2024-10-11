package co.fineants.api.domain.purchasehistory.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryUpdateRequest;
import co.fineants.api.domain.purchasehistory.domain.dto.response.PurchaseHistoryCreateResponse;
import co.fineants.api.domain.purchasehistory.domain.dto.response.PurchaseHistoryDeleteResponse;
import co.fineants.api.domain.purchasehistory.domain.dto.response.PurchaseHistoryUpdateResponse;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.event.publisher.PurchaseHistoryEventPublisher;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.PortfolioHoldingAuthorizedService;
import co.fineants.api.global.common.authorized.service.PurchaseHistoryAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.errorcode.PortfolioHoldingErrorCode;
import co.fineants.api.global.errors.errorcode.PurchaseHistoryErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PurchaseHistoryService {
	private final PurchaseHistoryRepository repository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryEventPublisher purchaseHistoryEventPublisher;
	private final PortfolioRepository portfolioRepository;
	private final PortfolioCalculator calculator;

	@Transactional
	@Authorized(serviceClass = PortfolioHoldingAuthorizedService.class)
	@Secured("ROLE_USER")
	public PurchaseHistoryCreateResponse createPurchaseHistory(
		PurchaseHistoryCreateRequest request,
		Long portfolioId,
		@ResourceId Long portfolioHoldingId,
		Long memberId) {
		log.info("매입이력 추가 서비스 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);

		Portfolio portfolio = findPortfolio(portfolioId);
		PortfolioHolding findHolding = portfolio.getPortfolioHoldings().stream()
			.filter(holding -> holding.getId().equals(portfolioHoldingId))
			.findAny()
			.orElseThrow(() -> new FineAntsException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING));
		PurchaseHistory history = request.toEntity(findHolding);

		verifyCashSufficientForPurchase(portfolio, (Money)history.calInvestmentAmount());

		PurchaseHistory newPurchaseHistory = repository.save(history);
		// 매입 이력 알림 이벤트를 위한 매입 이력 데이터 추가
		findHolding.addPurchaseHistory(newPurchaseHistory);

		purchaseHistoryEventPublisher.publishPushNotificationEvent(portfolioId, memberId);
		log.info("매입이력 저장 결과 : newPurchaseHistory={}", newPurchaseHistory);
		return PurchaseHistoryCreateResponse.from(newPurchaseHistory, portfolioId, memberId);
	}

	private Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.orElseThrow(() -> new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	private void verifyCashSufficientForPurchase(Portfolio portfolio, Money investmentAmount) {
		if (!portfolio.isCashSufficientForPurchase(investmentAmount, calculator)) {
			throw new FineAntsException(PortfolioErrorCode.TOTAL_INVESTMENT_PRICE_EXCEEDS_BUDGET);
		}
	}

	@Transactional
	@Authorized(serviceClass = PurchaseHistoryAuthorizedService.class)
	@Secured("ROLE_USER")
	public PurchaseHistoryUpdateResponse updatePurchaseHistory(PurchaseHistoryUpdateRequest request,
		Long portfolioHoldingId, @ResourceId Long purchaseHistoryId, Long portfolioId, Long memberId) {
		log.info("매입 내역 수정 서비스 요청 : request={}, portfolioHoldingId={}, purchaseHistoryId={}", request,
			portfolioHoldingId, purchaseHistoryId);
		PortfolioHolding portfolioHolding = findPortfolioHolding(portfolioHoldingId, portfolioId);
		PurchaseHistory originalPurchaseHistory = findPurchaseHistory(purchaseHistoryId);
		PurchaseHistory changePurchaseHistory = request.toEntity(portfolioHolding);

		PurchaseHistory changedPurchaseHistory = originalPurchaseHistory.change(changePurchaseHistory);
		PurchaseHistoryUpdateResponse response = PurchaseHistoryUpdateResponse.from(
			changedPurchaseHistory,
			portfolioId,
			memberId
		);
		purchaseHistoryEventPublisher.publishPushNotificationEvent(portfolioId, memberId);
		log.info("매입 내역 수정 결과 : response={}", response);
		return response;
	}

	@Transactional
	@Authorized(serviceClass = PurchaseHistoryAuthorizedService.class)
	@Secured("ROLE_USER")
	public PurchaseHistoryDeleteResponse deletePurchaseHistory(Long portfolioHoldingId,
		@ResourceId Long purchaseHistoryId,
		Long portfolioId, Long memberId) {
		log.info("매입 내역 삭제 서비스 요청 : portfolioHoldingId={}, purchaseHistoryId={}", portfolioHoldingId,
			purchaseHistoryId);
		PurchaseHistory deletePurchaseHistory = findPurchaseHistory(purchaseHistoryId);
		repository.deleteById(purchaseHistoryId);

		purchaseHistoryEventPublisher.publishPushNotificationEvent(portfolioId, memberId);
		return PurchaseHistoryDeleteResponse.from(deletePurchaseHistory, portfolioId, memberId);
	}

	private PortfolioHolding findPortfolioHolding(Long portfolioHoldingId, Long portfolioId) {
		return portfolioHoldingRepository.findByPortfolioHoldingIdAndPortfolioIdWithPortfolio(portfolioHoldingId,
				portfolioId)
			.orElseThrow(() -> new FineAntsException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING));
	}

	private PurchaseHistory findPurchaseHistory(Long purchaseHistoryId) {
		return repository.findById(purchaseHistoryId)
			.orElseThrow(() -> new FineAntsException(PurchaseHistoryErrorCode.NOT_FOUND_PURCHASE_HISTORY));
	}
}
