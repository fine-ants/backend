package codesquad.fineants.spring.api.portfolio_stock.event.listener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
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
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.kis.client.KisCurrentPrice;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.portfolio_stock.event.PortfolioEvent;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterKey;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.response.PortfolioHoldingsRealTimeResponse;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioHoldingService;

class HoldingSseEventListenerTest extends AbstractContainerBaseTest {

	@Autowired
	private HoldingSseEventListener holdingSseEventListener;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private CurrentPriceManager currentPriceManager;

	@Autowired
	private LastDayClosingPriceManager lastDayClosingPriceManager;

	@Autowired
	private PortfolioHoldingService portfolioHoldingService;

	@Autowired
	private SseEmitterManager manager;

	@AfterEach
	void tearDown() {
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
		manager.clear();
	}

	@DisplayName("장시간에 포트폴리오 리스너는 포트폴리오 이벤트를 받고 이벤트 스트림으로 데이터를 전송한다")
	@Test
	void handleMessage() throws IOException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));
		currentPriceManager.addCurrentPrice(KisCurrentPrice.create("005930", 60000L));
		lastDayClosingPriceManager.addPrice("005930", 50000);

		SseEmitter emitter = mock(SseEmitter.class);
		SseEmitterKey key = SseEmitterKey.create(portfolio.getId());
		emitter.onTimeout(() -> manager.remove(key));
		emitter.onCompletion(() -> manager.remove(key));
		manager.add(key, emitter);

		PortfolioHoldingsRealTimeResponse response = portfolioHoldingService.readMyPortfolioStocksInRealTime(
			portfolio.getId());
		LocalDateTime eventDateTime = LocalDateTime.of(2024, 1, 16, 9, 30);

		PortfolioEvent event = new PortfolioEvent(key, response, eventDateTime);

		// when
		holdingSseEventListener.handleMessage(event);

		// then
		BDDMockito.verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
	}

	@DisplayName("장시간에 포트폴리오 리스너는 포트폴리오 이벤트를 받고 데이터를 전송하려고 했으나 Emitter가 이미 완료되어 해당 emitter를 제거합니다")
	@Test
	void handleMessage_whenEmitterIsAlreadyCompleted_thenRemoveEmitter() throws IOException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));
		currentPriceManager.addCurrentPrice(KisCurrentPrice.create("005930", 60000L));
		lastDayClosingPriceManager.addPrice("005930", 50000);

		SseEmitter emitter = mock(SseEmitter.class);
		SseEmitterKey key = SseEmitterKey.create(portfolio.getId());
		emitter.onTimeout(() -> manager.remove(key));
		emitter.onCompletion(() -> manager.remove(key));
		BDDMockito.willThrow(new IllegalArgumentException("ResponseBodyEmitter has already completed"))
			.given(emitter)
			.send(any(SseEmitter.SseEventBuilder.class));
		manager.add(key, emitter);

		PortfolioHoldingsRealTimeResponse response = portfolioHoldingService.readMyPortfolioStocksInRealTime(
			portfolio.getId());
		LocalDateTime eventDateTime = LocalDateTime.of(2024, 1, 16, 9, 30);

		PortfolioEvent event = new PortfolioEvent(key, response, eventDateTime);

		// when
		holdingSseEventListener.handleMessage(event);

		// then
		assertThat(manager.size()).isZero();
	}

	private Member createMember() {
		return Member.builder()
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build();
	}

	private Portfolio createPortfolio(Member member) {
		return createPortfolio(member, "내꿈은 워렌버핏");
	}

	private Portfolio createPortfolio(Member member, String name) {
		return Portfolio.builder()
			.name(name)
			.securitiesFirm("토스")
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
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
				stock)
		);
	}
}
