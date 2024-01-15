package codesquad.fineants.spring.api.portfolio_stock.event.publisher;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.kis.response.CurrentPriceResponse;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterKey;
import codesquad.fineants.spring.api.portfolio_stock.manager.SseEmitterManager;
import codesquad.fineants.spring.api.portfolio_stock.service.PortfolioStockService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
class PortfolioEventPublisherTest {

	@Autowired
	private PortfolioEventPublisher publisher;

	@Autowired
	private SseEmitterManager manager;

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

	@MockBean
	private PortfolioStockService portfolioStockService;

	@AfterEach
	void tearDown() {
		purchaseHistoryRepository.deleteAllInBatch();
		portfolioHoldingRepository.deleteAllInBatch();
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("포트폴리오 이벤트를 전송한다")
	@Test
	void sendEventToPortfolio() throws IOException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));
		currentPriceManager.addCurrentPrice(new CurrentPriceResponse("005930", 60000L));
		lastDayClosingPriceManager.addPrice("005930", 50000);

		SseEmitter emitter = mock(SseEmitter.class);
		SseEmitterKey key = SseEmitterKey.create(portfolio.getId());
		emitter.onTimeout(() -> manager.remove(key));
		emitter.onCompletion(() -> manager.remove(key));
		manager.add(key, emitter);

		// when
		publisher.sendEventToPortfolio(LocalDateTime.of(2023, 12, 22, 10, 0, 0));
		// then
		verify(emitter, times(1)).send(any(SseEventBuilder.class));
	}

	@DisplayName("포트폴리오 이벤트 전송전에 예외가 발생하여 emitter가 제거된다")
	@Test
	void sendEventToPortfolioWithException() throws IOException {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createStock());
		stockDividendRepository.saveAll(createStockDividendWith(stock));
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		purchaseHistoryRepository.save(createPurchaseHistory(portfolioHolding));
		currentPriceManager.addCurrentPrice(new CurrentPriceResponse("005930", 60000L));
		lastDayClosingPriceManager.addPrice("005930", 50000);

		given(portfolioStockService.readMyPortfolioStocksInRealTime(anyLong()))
			.willThrow(new IllegalArgumentException("005930 종목에 대한 가격을 찾을 수 없습니다."));

		SseEmitter emitter = mock(SseEmitter.class);
		SseEmitterKey key = SseEmitterKey.create(portfolio.getId());
		emitter.onTimeout(() -> manager.remove(key));
		emitter.onCompletion(() -> manager.remove(key));
		manager.add(key, emitter);

		// when
		publisher.sendEventToPortfolio(LocalDateTime.of(2023, 12, 22, 10, 0, 0));
		// then
		Assertions.assertThat(manager.size()).isZero();
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
