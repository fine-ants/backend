package codesquad.fineants.spring.api.portfolio_stock.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.FineAntsException;
import codesquad.fineants.spring.api.common.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.portfolio_stock.chart.DividendChart;
import codesquad.fineants.spring.api.portfolio_stock.chart.PieChart;
import codesquad.fineants.spring.api.portfolio_stock.chart.SectorChart;
import codesquad.fineants.spring.api.portfolio_stock.event.publisher.PortfolioHoldingEventPublisher;
import codesquad.fineants.spring.api.portfolio_stock.factory.PortfolioDetailFactory;
import codesquad.fineants.spring.api.portfolio_stock.factory.PortfolioHoldingDetailFactory;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioHoldingCreateRequest;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStocksDeleteRequest;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioChartResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDetailRealTimeItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDetailResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDividendChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingRealTimeItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioSectorChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockCreateResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockDeleteResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockDeletesResponse;
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
	public PortfolioStockCreateResponse createPortfolioHolding(Long portfolioId, PortfolioHoldingCreateRequest request,
		AuthMember authMember) {
		log.info("포트폴리오 종목 추가 서비스 요청 : portfolioId={}, request={}, authMember={}", request, portfolioId, authMember);

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
	public PortfolioStockDeleteResponse deletePortfolioStock(Long portfolioHoldingId, AuthMember authMember) {
		log.info("포트폴리오 종목 삭제 서비스 : portfolioHoldingId={}, authMember={}", portfolioHoldingId, authMember);

		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(List.of(portfolioHoldingId));
		try {
			portfolioHoldingRepository.deleteById(portfolioHoldingId);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
		}
		return new PortfolioStockDeleteResponse(portfolioHoldingId);
	}

	@Transactional
	public PortfolioStockDeletesResponse deletePortfolioHoldings(Long portfolioId, AuthMember authMember,
		PortfolioStocksDeleteRequest request) {
		log.info("포트폴리오 종목 다수 삭제 서비스 : portfolioId={}, authMember={}, request={}", portfolioId, authMember, request);

		List<Long> portfolioHoldingIds = request.getPortfolioHoldingIds();
		validateExistPortfolioHolding(portfolioHoldingIds);
		validateHasAuthorization(portfolioHoldingIds, authMember.getMemberId());

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
		Double purchasedAmount =
			request.getPurchaseHistory().getNumShares() * request.getPurchaseHistory()
				.getPurchasePricePerShare();
		if (portfolio.calculateTotalInvestmentAmount() + purchasedAmount > portfolio.getBudget()
			.getAmount()
			.longValue()) {
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
