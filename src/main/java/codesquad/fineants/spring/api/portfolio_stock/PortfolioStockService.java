package codesquad.fineants.spring.api.portfolio_stock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.PortfolioHoldingErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStockCreateRequest;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioChartResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioDividendChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioPieChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioSectorChartItem;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockCreateResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockDeleteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PortfolioStockService {
	private final PortfolioRepository portfolioRepository;
	private final StockRepository stockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final PortfolioGainHistoryRepository portfolioGainHistoryRepository;
	private final CurrentPriceManager currentPriceManager;
	private final PieChart pieChart;
	private final DividendChart dividendChart;
	private final SectorChart sectorChart;

	@Transactional
	public PortfolioStockCreateResponse addPortfolioStock(Long portfolioId, PortfolioStockCreateRequest request,
		AuthMember authMember) {
		log.info("포트폴리오 종목 추가 서비스 요청 : portfolioId={}, request={}, authMember={}", request, portfolioId, authMember);

		Portfolio portfolio = findPortfolio(portfolioId);
		validatePortfolioAuthorization(portfolio, authMember.getMemberId());

		Stock stock = stockRepository.findByTickerSymbol(request.getTickerSymbol())
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));

		PortfolioHolding portFolioHolding = portfolioHoldingRepository.save(PortfolioHolding.empty(portfolio, stock));

		log.info("포트폴리오 종목 추가 결과 : {}", portFolioHolding);
		return PortfolioStockCreateResponse.from(portFolioHolding);
	}

	public Portfolio findPortfolio(Long portfolioId) {
		return portfolioRepository.findById(portfolioId)
			.orElseThrow(() -> new NotFoundResourceException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO));
	}

	private void validatePortfolioAuthorization(Portfolio portfolio, Long memberId) {
		if (!portfolio.hasAuthorization(memberId)) {
			throw new ForBiddenException(PortfolioErrorCode.NOT_HAVE_AUTHORIZATION);
		}
	}

	@Transactional
	public PortfolioStockDeleteResponse deletePortfolioStock(Long portfolioHoldingId, Long portfolioId,
		AuthMember authMember) {
		log.info("포트폴리오 종목 삭제 서비스 : portfolioHoldingId={}, authMember={}", portfolioHoldingId, authMember);

		Portfolio portfolio = findPortfolio(portfolioId);
		validatePortfolioAuthorization(portfolio, authMember.getMemberId());

		purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(List.of(portfolioHoldingId));
		try {
			portfolioHoldingRepository.deleteById(portfolioHoldingId);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundResourceException(PortfolioHoldingErrorCode.NOT_FOUND_PORTFOLIO_HOLDING);
		}
		return new PortfolioStockDeleteResponse(portfolioHoldingId);
	}

	public PortfolioHoldingsResponse readMyPortfolioStocks(Long portfolioId, LastDayClosingPriceManager manager) {
		Portfolio portfolio = findPortfolio(portfolioId);

		List<PortfolioHolding> portfolioHoldings = portfolio.changeCurrentPriceFromHoldings(currentPriceManager);
		log.info("portfolioHoldings : {}", portfolioHoldings);

		PortfolioGainHistory latestHistory = portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
				portfolio.getId(), LocalDateTime.now())
			.orElseGet(PortfolioGainHistory::empty);

		Map<String, Long> lastDayClosingPriceMap = portfolioHoldings.parallelStream()
			.map(PortfolioHolding::getStock)
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toMap(key -> key, manager::getPrice));
		return PortfolioHoldingsResponse.of(portfolio, latestHistory, portfolioHoldings, lastDayClosingPriceMap);
	}

	public PortfolioChartResponse readMyPortfolioCharts(Long portfolioId) {
		Portfolio portfolio = findPortfolio(portfolioId);
		List<PortfolioPieChartItem> pieChartItems = pieChart.createBy(portfolio);
		List<PortfolioDividendChartItem> dividendChartItems = dividendChart.createBy(portfolio);
		List<PortfolioSectorChartItem> sectorChartItems = sectorChart.createBy(portfolio);
		return new PortfolioChartResponse(pieChartItems, dividendChartItems, sectorChartItems);
	}
}
