package codesquad.fineants.spring.api.portfolio_stock.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividend;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.portfolio_stock.event.publisher.PortfolioHoldingEventPublisher;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStockCreateRequest;
import codesquad.fineants.spring.api.portfolio_stock.request.PortfolioStocksDeleteRequest;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioChartResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockCreateResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockDeleteResponse;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioStockDeletesResponse;
import codesquad.fineants.spring.util.ObjectMapperUtil;
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
	private PortfolioHoldingRepository portFolioHoldingRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private CurrentPriceManager currentPriceManager;

	@Autowired
	private LastDayClosingPriceManager lastDayClosingPriceManager;

	@MockBean
	private PortfolioHoldingEventPublisher publisher;

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
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		currentPriceManager.addCurrentPrice(new CurrentPriceResponse("005930", 60000L));
		lastDayClosingPriceManager.addPrice("005930", 50000);
		// when
		PortfolioHoldingsResponse response = service.readMyPortfolioStocks(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioDetails")
				.extracting("securitiesFirm", "name", "budget", "targetGain", "targetReturnRate",
					"maximumLoss", "maximumLossRate", "investedAmount", "totalGain", "totalGainRate", "dailyGain",
					"dailyGainRate", "balance", "annualDividend", "annualDividendYield",
					"provisionalLossBalance",
					"targetGainNotification", "maxLossNotification")
				.containsExactlyInAnyOrder("토스", "내꿈은 워렌버핏", 1000000L, 1500000L, 50, 900000L, 10, 150000L, 30000L,
					20, 30000L, 20, 850000L, 3249L, 1, 0L, false, false),
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
					portfolioHolding.getId(), 180000L, 60000L, 50000.0, 3L, 10000L, 20, 30000L, 20, 3249L)),
			() -> assertThat(response).extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.flatExtracting("purchaseHistory")
				.extracting("purchaseDate", "numShares", "purchasePricePerShare", "memo")
				.containsExactlyInAnyOrder(Tuple.tuple(LocalDateTime.of(2023, 9, 26, 9, 30, 0), 3L, 50000.0, "첫구매"))
		);
	}

	@DisplayName("사용자는 포트폴리오의 차트 정보를 조회한다")
	@Test
	void readMyPortfolioCharts() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		List<StockDividend> stockDividends = createStockDividendWith(stock);
		stockDividends.forEach(stock::addStockDividend);
		stockDividendRepository.saveAll(stockDividends);
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		currentPriceManager.addCurrentPrice(new CurrentPriceResponse("005930", 60000L));
		lastDayClosingPriceManager.addPrice("005930", 50000);

		// when
		PortfolioChartResponse response = service.readMyPortfolioCharts(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("pieChart")
				.asList()
				.hasSize(2)
				.extracting("name", "valuation", "weight", "totalGain", "totalGainRate")
				.containsExactlyInAnyOrder(
					Tuple.tuple("삼성전자보통주", 180000L, 17.475728155339805, 30000L, 20.00),
					Tuple.tuple("현금", 850000L, 82.52427184466019, 0L, 0.00)
				),
			() -> assertThat(response)
				.extracting("dividendChart")
				.asList()
				.hasSize(12)
				.extracting("month", "amount")
				.containsExactlyInAnyOrder(
					Tuple.tuple(1, 0L),
					Tuple.tuple(2, 0L),
					Tuple.tuple(3, 0L),
					Tuple.tuple(4, 0L),
					Tuple.tuple(5, 1083L),
					Tuple.tuple(6, 0L),
					Tuple.tuple(7, 0L),
					Tuple.tuple(8, 1083L),
					Tuple.tuple(9, 0L),
					Tuple.tuple(10, 0L),
					Tuple.tuple(11, 2166L),
					Tuple.tuple(12, 0L)
				),
			() -> assertThat(response)
				.extracting("sectorChart")
				.asList()
				.hasSize(2)
				.extracting("sector", "sectorWeight")
				.containsExactlyInAnyOrder(
					Tuple.tuple("전기전자", 17.475728155339805),
					Tuple.tuple("현금", 82.52427184466019)
				)
		);
	}

	@DisplayName("사용자는 포트폴리오에 실시간 상세 데이터를 조회한다")
	@Test
	void readMyPortfolioStocksInRealTime() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		currentPriceManager.addCurrentPrice(new CurrentPriceResponse("005930", 60000L));
		lastDayClosingPriceManager.addPrice("005930", 50000);

		// when
		PortfolioHoldingsRealTimeResponse response = service.readMyPortfolioStocksInRealTime(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioDetails")
				.extracting("currentValuation", "totalGain", "totalGainRate", "dailyGain", "dailyGainRate",
					"provisionalLossBalance")
				.containsExactlyInAnyOrder(180000L, 30000L, 20, 30000L, 20, 0L),

			() -> assertThat(response).extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.extracting("currentValuation", "currentPrice", "dailyChange", "dailyChangeRate", "totalGain",
					"totalReturnRate")
				.containsExactlyInAnyOrder(Tuple.tuple(180000L, 60000L, 10000L, 20, 30000L, 20))
		);
	}

	@DisplayName("사용자는 포트폴리오에 종목을 추가한다")
	@Test
	void addPortfolioStock() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", stock.getTickerSymbol());
		PortfolioStockCreateRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(requestBodyMap),
			PortfolioStockCreateRequest.class);

		// when
		PortfolioStockCreateResponse response = service.addPortfolioStock(portfolio.getId(), request,
			AuthMember.from(member));

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("portfolioStockId")
				.isNotNull(),
			() -> assertThat(portFolioHoldingRepository.findAll()).hasSize(1)
		);
	}

	@DisplayName("사용자는 포트폴리오에 존재하지 않는 종목을 추가할 수 없다")
	@Test
	void addPortfolioStockWithNotExistStock() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "999999");
		PortfolioStockCreateRequest request = ObjectMapperUtil.deserialize(ObjectMapperUtil.serialize(requestBodyMap),
			PortfolioStockCreateRequest.class);

		// when
		Throwable throwable = catchThrowable(() -> service.addPortfolioStock(portfolio.getId(),
			request, AuthMember.from(member)));

		// then
		assertThat(throwable).isInstanceOf(NotFoundResourceException.class)
			.extracting("message")
			.isEqualTo("종목을 찾을 수 없습니다");
	}

	@DisplayName("사용자는 포트폴리오의 종목을 삭제한다")
	@Test
	void deletePortfolioStock() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());

		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(
			PortfolioHolding.empty(portfolio, stock)
		);

		purchaseHistoryRepository.save(
			PurchaseHistory.builder()
				.purchaseDate(LocalDateTime.now())
				.purchasePricePerShare(10000.0)
				.numShares(1L)
				.memo("첫구매")
				.build()
		);

		Long portfolioHoldingId = portfolioHolding.getId();
		// when
		PortfolioStockDeleteResponse response = service.deletePortfolioStock(
			portfolioHoldingId, portfolio.getId(), AuthMember.from(member));

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioHoldingId").isNotNull(),
			() -> assertThat(portFolioHoldingRepository.findById(portfolioHoldingId).isEmpty()).isTrue(),
			() -> assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(portfolioHoldingId)).isEmpty()
		);
	}

	@DisplayName("사용자는 존재하지 않은 포트폴리오의 종목을 삭제할 수 없다")
	@Test
	void deletePortfolioStockWithNotExistPortfolioStockId() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Long portfolioStockId = 9999L;

		// when
		Throwable throwable = catchThrowable(() -> service.deletePortfolioStock(
			portfolioStockId, portfolio.getId(), AuthMember.from(member)));

		// then
		assertThat(throwable).isInstanceOf(NotFoundResourceException.class).extracting("message")
			.isEqualTo("포트폴리오 종목이 존재하지 않습니다");
	}

	@DisplayName("사용자는 다수의 포트폴리오 종목을 삭제할 수 있다")
	@Test
	void deletePortfolioStocks() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock1 = stockRepository.save(createStock());
		Stock stock2 = stockRepository.save(
			createStock("동화약품보통주", "000020", "DongwhaPharm", "KR7000020008", "의약품", Market.KOSPI));
		PortfolioHolding portfolioHolding1 = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock1));
		PortfolioHolding portfolioHolding2 = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock2));

		PurchaseHistory purchaseHistory1 = purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding1));
		PurchaseHistory purchaseHistory2 = purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding2));

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("portfolioHoldingIds", List.of(portfolioHolding1.getId(), portfolioHolding2.getId()));
		PortfolioStocksDeleteRequest request = ObjectMapperUtil.deserialize(
			ObjectMapperUtil.serialize(requestBodyMap), PortfolioStocksDeleteRequest.class);

		// when
		PortfolioStockDeletesResponse response = service.deletePortfolioStocks(portfolio.getId(),
			AuthMember.from(member), request);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("portfolioHoldingIds")
				.asList()
				.hasSize(2)
				.containsExactlyInAnyOrder(portfolioHolding1.getId(), portfolioHolding2.getId()),
			() -> assertThat(purchaseHistoryRepository.existsById(purchaseHistory1.getId())).isFalse(),
			() -> assertThat(purchaseHistoryRepository.existsById(purchaseHistory2.getId())).isFalse(),
			() -> assertThat(portFolioHoldingRepository.existsById(portfolioHolding1.getId())).isFalse(),
			() -> assertThat(portFolioHoldingRepository.existsById(portfolioHolding2.getId())).isFalse()
		);
	}

	@DisplayName("사용자는 다수의 포트폴리오 삭제시 존재하지 않는 일부 포트폴리오 종목이 존재한다면 전부 삭제할 수 없다")
	@Test
	void deletePortfolioStocks_whenNotExistPortfolioHolding_thenError404() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock1 = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock1));
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("portfolioHoldingIds", List.of(portfolioHolding.getId(), 9999L));
		PortfolioStocksDeleteRequest request = ObjectMapperUtil.deserialize(
			ObjectMapperUtil.serialize(requestBodyMap), PortfolioStocksDeleteRequest.class);

		// when
		Throwable throwable = catchThrowable(
			() -> service.deletePortfolioStocks(portfolio.getId(), AuthMember.from(member), request));

		// then
		assertThat(throwable)
			.isInstanceOf(NotFoundResourceException.class)
			.hasMessage("포트폴리오 종목이 존재하지 않습니다");
		assertThat(portFolioHoldingRepository.findById(portfolioHolding.getId()).isPresent()).isTrue();
		assertThat(purchaseHistoryRepository.findById(purchaseHistory.getId()).isPresent()).isTrue();
	}

	@DisplayName("사용자는 다수의 포트폴리오 삭제시 다른 회원의 포트폴리오 종목이 존재한다면 전부 삭제할 수 없다")
	@Test
	void deletePortfolioStocks_whenNotExistPortfolioHolding_thenError403() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock1 = stockRepository.save(createStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(createPortfolioHolding(portfolio, stock1));
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));

		Member member2 = memberRepository.save(createMember());
		Portfolio portfolio2 = portfolioRepository.save(createPortfolio(member2));
		PortfolioHolding portfolioHolding2 = portFolioHoldingRepository.save(
			createPortfolioHolding(portfolio2, stock1));

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("portfolioHoldingIds", List.of(portfolioHolding.getId(), portfolioHolding2.getId()));
		PortfolioStocksDeleteRequest request = ObjectMapperUtil.deserialize(
			ObjectMapperUtil.serialize(requestBodyMap), PortfolioStocksDeleteRequest.class);

		// when
		Throwable throwable = catchThrowable(
			() -> service.deletePortfolioStocks(portfolio.getId(), AuthMember.from(member), request));

		// then
		assertThat(throwable)
			.isInstanceOf(ForBiddenException.class)
			.hasMessage("해당 포트폴리오 종목들에 대한 권한이 없습니다");
		assertThat(portFolioHoldingRepository.findById(portfolioHolding.getId()).isPresent()).isTrue();
		assertThat(portFolioHoldingRepository.findById(portfolioHolding2.getId()).isPresent()).isTrue();
		assertThat(purchaseHistoryRepository.findById(purchaseHistory.getId()).isPresent()).isTrue();
	}

	private Member createMember() {
		return createMember("일개미1234", "kim1234@gmail.com");
	}

	private Member createMember(String nickname, String email) {
		return Member.builder()
			.nickname(nickname)
			.email(email)
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.targetGainIsActive(false)
			.maximumIsActive(false)
			.build();
	}

	private Stock createStock() {
		return Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.sector("전기전자")
			.market(Market.KOSPI)
			.build();
	}

	private Stock createStock(String companyName, String tickerSymbol, String companyNameEng, String stockCode,
		String sector, Market market) {
		return Stock.builder()
			.companyName(companyName)
			.tickerSymbol(tickerSymbol)
			.companyNameEng(companyNameEng)
			.stockCode(stockCode)
			.sector(sector)
			.market(market)
			.build();
	}

	private StockDividend createStockDividend(LocalDate exDividendDate, LocalDate recordDate, LocalDate paymentDate,
		Stock stock) {
		return StockDividend.builder()
			.dividend(361L)
			.exDividendDate(exDividendDate)
			.recordDate(recordDate)
			.paymentDate(paymentDate)
			.stock(stock)
			.build();
	}

	private PortfolioHolding createPortfolioHolding(Portfolio portfolio, Stock stock) {
		return PortfolioHolding.builder()
			.portfolio(portfolio)
			.stock(stock)
			.build();
	}

	private PurchaseHistory createPurchaseHistory(PortfolioHolding portfolioHolding) {
		return PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.of(2023, 9, 26, 9, 30, 0))
			.numShares(3L)
			.purchasePricePerShare(50000.0)
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
	}

	private List<StockDividend> createStockDividendWith(Stock stock) {
		return List.of(
			createStockDividend(
				LocalDate.of(2022, 12, 30),
				LocalDate.of(2022, 12, 31),
				LocalDate.of(2023, 4, 14),
				stock),
			createStockDividend(
				LocalDate.of(2023, 3, 30),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2023, 6, 29),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2023, 9, 27),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20),
				stock),
			createStockDividend(
				LocalDate.of(2024, 3, 29),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17),
				stock),
			createStockDividend(
				LocalDate.of(2024, 6, 28),
				LocalDate.of(2024, 6, 30),
				LocalDate.of(2024, 8, 16),
				stock),
			createStockDividend(
				LocalDate.of(2024, 9, 27),
				LocalDate.of(2024, 9, 30),
				LocalDate.of(2024, 11, 20),
				stock)
		);
	}
}
