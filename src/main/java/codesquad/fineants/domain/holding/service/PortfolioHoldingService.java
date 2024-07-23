package codesquad.fineants.domain.holding.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.holding.domain.chart.DividendChart;
import codesquad.fineants.domain.holding.domain.chart.PieChart;
import codesquad.fineants.domain.holding.domain.chart.SectorChart;
import codesquad.fineants.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioChartResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDetailResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDetails;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioPieChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioStockCreateResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioStockDeleteResponse;
import codesquad.fineants.domain.holding.domain.dto.response.PortfolioStockDeletesResponse;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.domain.factory.PortfolioDetailFactory;
import codesquad.fineants.domain.holding.domain.factory.PortfolioHoldingDetailFactory;
import codesquad.fineants.domain.holding.event.publisher.PortfolioHoldingEventPublisher;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.purchasehistory.domain.entity.PurchaseHistory;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.global.common.authorized.Authorized;
import codesquad.fineants.global.common.authorized.service.PortfolioAuthorizedService;
import codesquad.fineants.global.common.authorized.service.PortfolioHoldingAuthorizedService;
import codesquad.fineants.global.common.resource.ResourceId;
import codesquad.fineants.global.common.resource.ResourceIds;
import codesquad.fineants.global.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.global.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.global.errors.errorcode.PurchaseHistoryErrorCode;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
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
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public PortfolioStockCreateResponse createPortfolioHolding(@ResourceId Long portfolioId,
		PortfolioHoldingCreateRequest request) {
		log.info("포트폴리오 종목 추가 서비스 요청 : portfolioId={}, request={}", portfolioId, request);

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
	@Authorized(serviceClass = PortfolioHoldingAuthorizedService.class)
	public PortfolioStockDeleteResponse deletePortfolioStock(@ResourceId Long portfolioHoldingId) {
		log.info("포트폴리오 종목 삭제 서비스 : portfolioHoldingId={}", portfolioHoldingId);
		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(List.of(portfolioHoldingId));

		int deleted = portfolioHoldingRepository.deleteAllByIdIn(List.of(portfolioHoldingId));
		if (deleted == 0) {
			throw new FineAntsException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
		}
		return new PortfolioStockDeleteResponse(portfolioHoldingId);
	}

	@Transactional
	@Authorized(serviceClass = PortfolioHoldingAuthorizedService.class)
	public PortfolioStockDeletesResponse deletePortfolioHoldings(Long portfolioId, Long memberId,
		@ResourceIds List<Long> portfolioHoldingIds) {
		log.info("포트폴리오 종목 다수 삭제 서비스 : portfolioId={}, memberId={}, portfolioHoldingIds={}", portfolioId, memberId,
			portfolioHoldingIds);
		validateExistPortfolioHolding(portfolioHoldingIds);

		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(portfolioHoldingIds);
		try {
			portfolioHoldingRepository.deleteAllByIdIn(portfolioHoldingIds);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
		}
		return new PortfolioStockDeletesResponse(portfolioHoldingIds);
	}

	private Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
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

	@Transactional(readOnly = true)
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public PortfolioHoldingsResponse readPortfolioHoldings(@ResourceId Long portfolioId) {
		Portfolio portfolio = findPortfolio(portfolioId);
		PortfolioDetailResponse portfolioDetail = portfolioDetailFactory.createPortfolioDetailItem(portfolio);
		List<PortfolioHoldingItem> portfolioHoldingItems = portfolioHoldingDetailFactory.createPortfolioHoldingItems(
			portfolio);
		return PortfolioHoldingsResponse.of(portfolioDetail, portfolioHoldingItems);
	}

	@Transactional(readOnly = true)
	public PortfolioHoldingsRealTimeResponse readMyPortfolioStocksInRealTime(Long portfolioId) {
		Portfolio portfolio = findPortfolioUsingFetchJoin(portfolioId);
		PortfolioDetailRealTimeItem portfolioDetail = portfolioDetailFactory.createPortfolioDetailRealTimeItem(
			portfolio);
		List<PortfolioHoldingRealTimeItem> portfolioHoldingDetails =
			portfolioHoldingDetailFactory.createPortfolioHoldingRealTimeItems(portfolio);
		return PortfolioHoldingsRealTimeResponse.of(portfolioDetail, portfolioHoldingDetails);
	}

	private Portfolio findPortfolioUsingFetchJoin(Long portfolioId) {
		return portfolioRepository.findByPortfolioIdWithAll(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	@Transactional(readOnly = true)
	@Authorized(serviceClass = PortfolioAuthorizedService.class)
	public PortfolioChartResponse readPortfolioCharts(@ResourceId Long portfolioId, LocalDate currentLocalDate) {
		Portfolio portfolio = findPortfolio(portfolioId);
		PortfolioDetails portfolioDetails = PortfolioDetails.from(portfolio);
		List<PortfolioPieChartItem> pieChartItems = pieChart.createBy(portfolio);
		List<PortfolioDividendChartItem> dividendChartItems = dividendChart.createBy(portfolio, currentLocalDate);
		List<PortfolioSectorChartItem> sectorChartItems = sectorChart.createBy(portfolio);
		return PortfolioChartResponse.create(portfolioDetails, pieChartItems, dividendChartItems, sectorChartItems);
	}
}
