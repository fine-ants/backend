package codesquad.fineants.spring.api.purchase_history.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.purchase_history.event.PurchaseHistoryEventPublisher;
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
	private final PurchaseHistoryEventPublisher purchaseHistoryEventPublisher;
	private final PortfolioRepository portfolioRepository;

	@Transactional
	public PurchaseHistoryCreateResponse addPurchaseHistory(
		PurchaseHistoryCreateRequest request,
		Long portfolioId,
		Long portfolioHoldingId,
		Long memberId) {
		log.info("매입이력 추가 서비스 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);

		Portfolio portfolio = findPortfolio(portfolioId);
		PortfolioHolding findHolding = portfolio.getPortfolioHoldings().stream()
			.filter(holding -> holding.getId().equals(portfolioHoldingId))
			.findAny()
			.orElseThrow(() -> new FineAntsException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING));
		PurchaseHistory history = request.toEntity(findHolding);

		verifyCashSufficientForPurchase(portfolio, history.calculateInvestmentAmount());

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

	private void verifyCashSufficientForPurchase(Portfolio portfolio, long investmentAmount) {
		if (portfolio.isCashSufficientForPurchase(investmentAmount)) {
			throw new FineAntsException(PortfolioErrorCode.TOTAL_INVESTMENT_PRICE_EXCEEDS_BUDGET);
		}
	}

	@Transactional
	public PurchaseHistoryModifyResponse modifyPurchaseHistory(PurchaseHistoryModifyRequest request,
		Long portfolioHoldingId, Long purchaseHistoryId, Long portfolioId, Long memberId) {
		log.info("매입 내역 수정 서비스 요청 : request={}, portfolioHoldingId={}, purchaseHistoryId={}", request,
			portfolioHoldingId, purchaseHistoryId);
		PortfolioHolding portfolioHolding = findPortfolioHolding(portfolioHoldingId, portfolioId);
		PurchaseHistory originalPurchaseHistory = findPurchaseHistory(purchaseHistoryId);
		PurchaseHistory changePurchaseHistory = request.toEntity(portfolioHolding);

		PurchaseHistory changedPurchaseHistory = originalPurchaseHistory.change(changePurchaseHistory);
		PurchaseHistoryModifyResponse response = PurchaseHistoryModifyResponse.from(
			changedPurchaseHistory,
			portfolioId,
			memberId
		);
		purchaseHistoryEventPublisher.publishPushNotificationEvent(portfolioId, memberId);
		log.info("매입 내역 수정 결과 : response={}", response);
		return response;
	}

	@Transactional
	public PurchaseHistoryDeleteResponse deletePurchaseHistory(Long portfolioHoldingId, Long purchaseHistoryId,
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
