package codesquad.fineants.spring.api.purchase_history;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio_holding.PortFolioHoldingRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
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
	private final PortFolioHoldingRepository portFolioHoldingRepository;

	@Transactional
	public PurchaseHistoryCreateResponse addPurchaseHistory(PurchaseHistoryCreateRequest request,
		Long portfolioHoldingId,
		AuthMember authMember) {
		log.info("매입이력 추가 서비스 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);
		PortfolioHolding portfolioHolding = findPortfolioHolding(portfolioHoldingId);
		validatePortfolioAuthorization(portfolioHolding.getPortfolio(), authMember.getMemberId());
		PurchaseHistory newPurchaseHistory = repository.save(request.toEntity(portfolioHolding));
		log.info("매입이력 저장 결과 : newPurchaseHistory={}", newPurchaseHistory);
		return PurchaseHistoryCreateResponse.from(newPurchaseHistory);
	}

	private PortfolioHolding findPortfolioHolding(Long portfolioHoldingId) {
		return portFolioHoldingRepository.findById(portfolioHoldingId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING));
	}

	private void validatePortfolioAuthorization(Portfolio portfolio, Long memberId) {
		if (!portfolio.hasAuthorization(memberId)) {
			throw new ForBiddenException(PortfolioErrorCode.NOT_HAVE_AUTHORIZATION);
		}
	}

	@Transactional
	public PurchaseHistoryModifyResponse modifyPurchaseHistory(PurchaseHistoryModifyRequest request,
		Long portfolioHoldingId, Long purchaseHistoryId, AuthMember authMember) {
		log.info("매입 내역 수정 서비스 요청 : request={}, portfolioHoldingId={}, purchaseHistoryId={}", request,
			portfolioHoldingId, purchaseHistoryId);
		Portfolio portfolio = findPortfolioHolding(portfolioHoldingId).getPortfolio();
		validatePortfolioAuthorization(portfolio, authMember.getMemberId());

		PortfolioHolding portfolioHolding = findPortfolioHolding(portfolioHoldingId);
		PurchaseHistory originalPurchaseHistory = findPurchaseHistory(purchaseHistoryId);
		PurchaseHistory changePurchaseHistory = request.toEntity(portfolioHolding);

		log.info("매입 내역 수정 결과 : changePurchaseHistory={}", changePurchaseHistory);
		return PurchaseHistoryModifyResponse.from(originalPurchaseHistory.change(changePurchaseHistory));
	}

	private PurchaseHistory findPurchaseHistory(Long purchaseHistoryId) {
		return repository.findById(purchaseHistoryId)
			.orElseThrow(() -> new NotFoundResourceException(PurchaseHistoryErrorCode.NOT_FOUND_PURCHASE_HISTORY));
	}

	@Transactional
	public PurchaseHistoryDeleteResponse deletePurchaseHistory(Long portfolioHoldingId, Long purchaseHistoryId,
		AuthMember authMember) {
		log.info("매입 내역 삭제 서비스 요청 : portfolioHoldingId={}, purchaseHistoryId={}", portfolioHoldingId,
			purchaseHistoryId);
		Portfolio portfolio = findPortfolioHolding(portfolioHoldingId).getPortfolio();
		validatePortfolioAuthorization(portfolio, authMember.getMemberId());
		PurchaseHistory deletePurchaseHistory = findPurchaseHistory(purchaseHistoryId);
		repository.deleteById(purchaseHistoryId);
		return PurchaseHistoryDeleteResponse.from(deletePurchaseHistory);
	}
}
