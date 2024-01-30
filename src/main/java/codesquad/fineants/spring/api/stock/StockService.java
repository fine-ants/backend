package codesquad.fineants.spring.api.stock;

import static org.springframework.http.HttpHeaders.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.member.service.WebClientWrapper;
import codesquad.fineants.spring.api.stock.request.StockSearchRequest;
import codesquad.fineants.spring.api.stock.response.StockResponse;
import codesquad.fineants.spring.api.stock.response.StockDataResponse;
import codesquad.fineants.spring.api.stock.response.StockSearchItem;
import codesquad.fineants.spring.util.ObjectMapperUtil;
import codesquad.fineants.spring.util.StockFileUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final PortfolioHoldingRepository portfolioHoldingRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final StockDividendRepository stockDividendRepository;
	private final WebClientWrapper webClient;
	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;

	public List<StockSearchItem> search(StockSearchRequest request) {
		return stockRepository.search(request.getSearchTerm())
			.stream()
			.map(StockSearchItem::from)
			.collect(Collectors.toList());
	}

	@Scheduled(cron = "0 0 * * * ?")
	@Transactional
	public void refreshStockFile() {
		String requestUri = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
		String responseText = webClient.post(requestUri, createHeader(), createBody(), String.class);
		Set<StockDataResponse.StockInfo> response = ObjectMapperUtil.deserialize(responseText, StockDataResponse.class)
			.getStockInfos();
		Set<StockDataResponse.StockInfo> initialStockInfos = StockFileUtil.readStockFile();
		initialStockInfos.removeAll(response);
		for (StockDataResponse.StockInfo stockInfo : initialStockInfos) {
			Optional<PortfolioHolding> portfolioHolding = portfolioHoldingRepository.findByTickerSymbol(
				stockInfo.getTickerSymbol());
			if (portfolioHolding.isPresent()) {
				purchaseHistoryRepository.deleteByPortfolioHoldingId(portfolioHolding.get().getId());
				portfolioHoldingRepository.deleteById(portfolioHolding.get().getId());
			}
			stockDividendRepository.deleteByTickerSymbol(stockInfo.getTickerSymbol());
			stockRepository.deleteByTickerSymbol(stockInfo.getTickerSymbol());
		}
		StockFileUtil.convertToTsvFile(response);

		System.out.println(0);
	}

	@Transactional(readOnly = true)
	public StockResponse getStock(String tickerSymbol) {
		Stock stock = stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
		return StockResponse.of(stock, currentPriceManager, lastDayClosingPriceManager);
	}

	private MultiValueMap<String, String> createBody() {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("bld", "dbms/MDC/STAT/standard/MDCSTAT01901");
		formData.add("locale", "ko_KR");
		formData.add("mktId", "ALL");
		formData.add("share", "1");
		formData.add("csvxls_isNo", "false");
		return formData;
	}

	private MultiValueMap<String, String> createHeader() {
		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
		result.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		result.add(ACCEPT, MediaType.TEXT_HTML_VALUE);
		result.add(ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
		return result;
	}
}
