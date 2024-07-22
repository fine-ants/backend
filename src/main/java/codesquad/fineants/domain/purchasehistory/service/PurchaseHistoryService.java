package codesquad.fineants.domain.purchasehistory.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.holding.service.PortfolioHoldingAuthorizedService;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.domain.purchasehistory.domain.dto.request.PurchaseHistoryUpdateRequest;
import codesquad.fineants.domain.purchasehistory.domain.dto.response.PurchaseHistoryCreateResponse;
import codesquad.fineants.domain.purchasehistory.domain.dto.response.PurchaseHistoryDeleteResponse;
import codesquad.fineants.domain.purchasehistory.domain.dto.response.PurchaseHistoryUpdateResponse;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.event.publisher.PurchaseHistoryEventPublisher;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.global.common.authorized.Authorized;
import codesquad.fineants.global.common.resource.ResourceId;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.global.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
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

		verifyCashSufficientForPurchase(portfolio, (Money)history.calculateInvestmentAmount());

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
		if (portfolio.isCashSufficientForPurchase(investmentAmount)) {
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

		// 매입 이력 알람 이벤트를 위한 매입 이력 데이터 삭제
		findPortfolio(portfolioId).getPortfolioHoldings().stream()
			.filter(holding -> holding.getId().equals(portfolioHoldingId))
			.findAny()
			.orElseThrow(() -> new FineAntsException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING))
			.getPurchaseHistory().remove(deletePurchaseHistory);

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
