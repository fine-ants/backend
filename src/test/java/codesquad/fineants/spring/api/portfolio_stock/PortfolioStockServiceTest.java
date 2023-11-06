package codesquad.fineants.spring.api.portfolio_stock;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortFolioHoldingRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class PortfolioStockServiceTest {

	@Autowired
	private PortfolioStockService service;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortFolioHoldingRepository portFolioHoldingRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private CurrentPriceManager currentPriceManager;

	private Member member;
	private Portfolio portfolio;
	private Stock stock;
	private PortfolioHolding portfolioHolding;
	private PurchaseHistory purchaseHistory;

	@BeforeEach
	void init() {
		Member member = Member.builder()
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
		this.member = memberRepository.save(member);

		Portfolio portfolio = Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.targetGainIsActive(false)
			.maximumIsActive(false)
			.build();
		this.portfolio = portfolioRepository.save(portfolio);

		Stock stock = Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build();
		this.stock = stockRepository.save(stock);

		StockDividend stockDividend1Q = StockDividend.builder()
			.dividend(361L)
			.dividendMonth(LocalDate.of(2023, 4, 1).atStartOfDay())
			.stock(this.stock)
			.build();
		StockDividend stockDividend2Q = StockDividend.builder()
			.dividend(361L)
			.dividendMonth(LocalDate.of(2023, 5, 1).atStartOfDay())
			.stock(this.stock)
			.build();
		StockDividend stockDividend3Q = StockDividend.builder()
			.dividend(361L)
			.dividendMonth(LocalDate.of(2023, 8, 1).atStartOfDay())
			.stock(this.stock)
			.build();
		StockDividend stockDividend4Q = StockDividend.builder()
			.dividend(361L)
			.dividendMonth(LocalDate.of(2023, 11, 1).atStartOfDay())
			.stock(this.stock)
			.build();

		stockDividendRepository.saveAll(List.of(stockDividend1Q, stockDividend2Q, stockDividend3Q, stockDividend4Q));

		PortfolioHolding portfolioHolding = PortfolioHolding.empty(portfolio, stock);
		this.portfolioHolding = portFolioHoldingRepository.save(portfolioHolding);

		this.purchaseHistory = purchaseHistoryRepository.save(PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 11, 1, 9, 30, 0))
			.numShares(3L)
			.purchasePricePerShare(50000.0)
			.memo("첫구매")
			.portFolioHolding(portfolioHolding)
			.build());
	}

	@AfterEach
	void tearDown() {
		purchaseHistoryRepository.deleteAllInBatch();
		portFolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("포트폴리오 종목들의 상세 정보를 조회한다")
	@Test
	void readMyPortfolioStocks() {
		// given
		Long portfolioId = portfolio.getId();
		currentPriceManager.addCurrentPrice(new CurrentPriceResponse("005930", 60000L));

		// when
		PortfolioHoldingsResponse response = service.readMyPortfolioStocks(portfolioId);

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioDetails")
				.extracting("securitiesFirm", "name", "budget", "targetGain", "targetReturnRate",
					"maximumLoss", "maximumLossRate", "investedAmount", "totalGain", "totalGainRate", "dailyGain",
					"dailyGainRate", "balance", "totalAnnualDividend", "totalAnnualDividendYield",
					"provisionalLossBalance",
					"targetGainNotification", "maxLossNotification")
				.containsExactlyInAnyOrder("토스", "내꿈은 워렌버핏", 1000000L, 1500000L, 50, 900000L, 10, 150000L, 30000L,
					20, 30000L, 20, 850000L, 4332L, 2, 0L, false, false),
			() -> assertThat(response).extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.extracting("stock")
				.extracting("companyName", "tickerSymbol")
				.containsExactlyInAnyOrder(Tuple.tuple("삼성전자보통주", "005930")),

			() -> assertThat(response).extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.extracting("portfolioHolding")
				.extracting("portfolioHoldingId", "currentValuation", "currentPrice", "averageCostPerShare",
					"numShares", "dailyChange", "dailyChangeRate", "totalGain", "totalReturnRate", "annualDividend")
				.containsExactlyInAnyOrder(Tuple.tuple(
					portfolioHolding.getId(), 180000L, 60000L, 50000.0, 3L, 30000L, 20, 30000L, 20, 4332L)),

			() -> assertThat(response).extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.flatExtracting("dividends")
				.extracting("dividendMonth", "dividendAmount")
				.containsExactlyInAnyOrder(
					Tuple.tuple(LocalDate.of(2023, 4, 1).atStartOfDay(), 361L),
					Tuple.tuple(LocalDate.of(2023, 5, 1).atStartOfDay(), 361L),
					Tuple.tuple(LocalDate.of(2023, 8, 1).atStartOfDay(), 361L),
					Tuple.tuple(LocalDate.of(2023, 11, 1).atStartOfDay(), 361L)
				),

			() -> assertThat(response).extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.flatExtracting("purchaseHistory")
				.extracting("purchaseDate", "numShares", "purchasePricePerShare", "memo")
				.containsExactlyInAnyOrder(Tuple.tuple(LocalDateTime.of(2023, 11, 1, 9, 30, 0), 3L, 50000.0, "첫구매"))
		);
	}
}
