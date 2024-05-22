package codesquad.fineants.domain.portfolio_holding.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.domain.chart.DividendChart;
import codesquad.fineants.domain.portfolio_holding.domain.chart.PieChart;
import codesquad.fineants.domain.portfolio_holding.domain.chart.SectorChart;
import codesquad.fineants.domain.portfolio_holding.domain.dto.request.PortfolioHoldingCreateRequest;
import codesquad.fineants.domain.portfolio_holding.domain.dto.request.PortfolioStocksDeleteRequest;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioChartResponse;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioDetailRealTimeItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioDetailResponse;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioDividendChartItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioHoldingItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioHoldingsResponse;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioSectorChartItem;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioStockCreateResponse;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioStockDeleteResponse;
import codesquad.fineants.domain.portfolio_holding.domain.dto.response.PortfolioStockDeletesResponse;
import codesquad.fineants.domain.portfolio_holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.domain.factory.PortfolioDetailFactory;
import codesquad.fineants.domain.portfolio_holding.domain.factory.PortfolioHoldingDetailFactory;
import codesquad.fineants.domain.portfolio_holding.event.publisher.PortfolioHoldingEventPublisher;
import codesquad.fineants.domain.portfolio_holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.global.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.ForBiddenException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PortfolioHoldingService {
	private final PortfolioRepository portfolioRepository;
	private final StockRepository stockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final PieChart pieChart;
	private final DividendChart dividendChart;
	private final SectorChart sectorChart;
	private final PortfolioDetailFactory portfolioDetailFactory;
	private final PortfolioHoldingDetailFactory portfolioHoldingDetailFactory;
	private final PortfolioHoldingEventPublisher publisher;

	@Transactional
	public PortfolioStockCreateResponse createPortfolioHolding(Long portfolioId,
		PortfolioHoldingCreateRequest request) {
		log.info("포트폴리오 종목 추가 서비스 요청 : portfolioId={}, request={}, authMember={}", request, portfolioId);

		Portfolio portfolio = findPortfolio(portfolioId);

		Stock stock = stockRepository.findByTickerSymbol(request.getTickerSymbol())
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));

		PortfolioHolding holding = portfolioHoldingRepository.findByPortfolioIdAndTickerSymbol(portfolioId,
				request.getTickerSymbol())
			.orElseGet(() -> PortfolioHolding.empty(portfolio, stock));
		PortfolioHolding saveHolding = portfolioHoldingRepository.save(holding);

		if (request.isPurchaseHistoryComplete()) {
			validateInvestAmountNotExceedsBudget(request, portfolio);
			purchaseHistoryRepository.save(PurchaseHistory.of(saveHolding, request.getPurchaseHistory()));
		} else if (!request.isPurchaseHistoryAllNull()) {
			throw new FineAntsException(PurchaseHistoryErrorCode.BAD_INPUT);
		}

		publisher.publishPortfolioHolding(stock.getTickerSymbol());
		log.info("포트폴리오 종목 추가 결과 : {}", saveHolding);
		return PortfolioStockCreateResponse.from(saveHolding);
	}

	@Transactional
	public PortfolioStockDeleteResponse deletePortfolioStock(Long portfolioHoldingId) {
		log.info("포트폴리오 종목 삭제 서비스 : portfolioHoldingId={}", portfolioHoldingId);

		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(List.of(portfolioHoldingId));

		int deleted = portfolioHoldingRepository.deleteAllByIdIn(List.of(portfolioHoldingId));
		if (deleted == 0) {
			throw new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
		}
		return new PortfolioStockDeleteResponse(portfolioHoldingId);
	}

	@Transactional
	public PortfolioStockDeletesResponse deletePortfolioHoldings(Long portfolioId, Long memberId,
		PortfolioStocksDeleteRequest request) {
		log.info("포트폴리오 종목 다수 삭제 서비스 : portfolioId={}, memberId={}, request={}", portfolioId, memberId, request);

		List<Long> portfolioHoldingIds = request.getPortfolioHoldingIds();
		validateExistPortfolioHolding(portfolioHoldingIds);
		validateHasAuthorization(portfolioHoldingIds, memberId);

		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioHoldingIds);
		try {
			portfolioHoldingRepository.deleteAllByIdIn(portfolioHoldingIds);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
		}
		return new PortfolioStockDeletesResponse(portfolioHoldingIds);
	}

	public Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	public Portfolio findPortfolioUsingFetchJoin(Long portfolioId) {
		return portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	private void validateInvestAmountNotExceedsBudget(PortfolioHoldingCreateRequest request, Portfolio portfolio) {
		Expression purchasedAmount = request.getPurchaseHistory().getNumShares()
			.multiply(request.getPurchaseHistory().getPurchasePricePerShare());
		if (portfolio.isExceedBudgetByPurchasedAmount(purchasedAmount)) {
			throw new FineAntsException(PortfolioErrorCode.TOTAL_INVESTMENT_PRICE_EXCEEDS_BUDGET);
		}
	}

	private void validateExistPortfolioHolding(List<Long> portfolioHoldingIds) {
		portfolioHoldingIds.stream()
			.filter(portfolioHoldingId -> !portfolioHoldingRepository.existsById(portfolioHoldingId))
			.forEach(portfolioHoldingId -> {
				throw new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
			});
	}

	private void validateHasAuthorization(List<Long> portfolioHoldingIds, Long memberId) {
		for (Long portfolioHoldingId : portfolioHoldingIds) {
			if (!portfolioHoldingRepository.existsByIdAndMemberId(portfolioHoldingId, memberId)) {
				throw new ForBiddenException(PortfolioHoldingErrorCode.FORBIDDEN_PORTFOLIO_HOLDING);
			}
		}
	}

	public PortfolioHoldingsResponse readPortfolioHoldings(Long portfolioId) {
		Portfolio portfolio = findPortfolio(portfolioId);
		PortfolioDetailResponse portfolioDetail = portfolioDetailFactory.createPortfolioDetailItem(portfolio);
		List<PortfolioHoldingItem> portfolioHoldingItems = portfolioHoldingDetailFactory.createPortfolioHoldingItems(
			portfolio);
		return PortfolioHoldingsResponse.of(portfolioDetail, portfolioHoldingItems);
	}

	public PortfolioHoldingsRealTimeResponse readMyPortfolioStocksInRealTime(Long portfolioId) {
		Portfolio portfolio = findPortfolioUsingFetchJoin(portfolioId);
		PortfolioDetailRealTimeItem portfolioDetail = portfolioDetailFactory.createPortfolioDetailRealTimeItem(
			portfolio);
		List<PortfolioHoldingRealTimeItem> portfolioHoldingDetails = portfolioHoldingDetailFactory.createPortfolioHoldingRealTimeItems(
			portfolio);
		return PortfolioHoldingsRealTimeResponse.of(portfolioDetail, portfolioHoldingDetails);
	}

	public PortfolioChartResponse readPortfolioCharts(Long portfolioId, LocalDate currentLocalDate) {
		Portfolio portfolio = findPortfolio(portfolioId);
		List<PortfolioPieChartItem> pieChartItems = pieChart.createBy(portfolio);
		List<PortfolioDividendChartItem> dividendChartItems = dividendChart.createBy(portfolio, currentLocalDate);
		List<PortfolioSectorChartItem> sectorChartItems = sectorChart.createBy(portfolio);
		return PortfolioChartResponse.create(pieChartItems, dividendChartItems, sectorChartItems);
	}
}
