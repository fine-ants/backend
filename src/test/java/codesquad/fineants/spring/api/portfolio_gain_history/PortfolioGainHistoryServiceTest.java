package codesquad.fineants.spring.api.portfolio_gain_history;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.portfolio_holding.PortfolioHolding;
import codesquad.fineants.domain.portfolio_holding.PortfolioHoldingRepository;
import codesquad.fineants.domain.purchase_history.PurchaseHistory;
import codesquad.fineants.domain.purchase_history.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.Market;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_dividend.StockDividendRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.portfolio_gain_history.response.PortfolioGainHistoryCreateResponse;
import codesquad.fineants.spring.api.portfolio_gain_history.service.PortfolioGainHistoryService;

class PortfolioGainHistoryServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioGainHistoryService service;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository portFolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@MockBean
	private CurrentPriceManager currentPriceManager;

	private Member member;

	private Stock stock;

	private Portfolio portfolio;

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
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.build();
		this.portfolio = portfolio;

		Stock stock = Stock.builder()
			.companyName("삼성전자보통주")
			.tickerSymbol("005930")
			.companyNameEng("SamsungElectronics")
			.stockCode("KR7005930003")
			.market(Market.KOSPI)
			.build();
		this.stock = stockRepository.save(stock);
	}

	@AfterEach
	void tearDown() {
		portfolioGainHistoryRepository.deleteAllInBatch();
		purchaseHistoryRepository.deleteAllInBatch();
		portFolioHoldingRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
		stockDividendRepository.deleteAllInBatch();
		stockRepository.deleteAllInBatch();
	}

	@DisplayName("모든 포트폴리오의 손익 내역을 추가한다")
	@Test
	void addPortfolioGainHistory() {
		// given
		Portfolio savePortfolio = portfolioRepository.save(portfolio);
		PortfolioHolding portfolioHolding = PortfolioHolding.of(savePortfolio, stock, Money.won(60000L));
		PurchaseHistory purchaseHistory = PurchaseHistory.builder()
			.purchaseDate(LocalDateTime.now())
			.numShares(Count.from(3L))
			.purchasePricePerShare(Money.won(50000.0))
			.memo("첫구매")
			.portfolioHolding(portfolioHolding)
			.build();
		portfolioHolding.addPurchaseHistory(purchaseHistory);
		portFolioHoldingRepository.save(portfolioHolding);
		purchaseHistoryRepository.save(purchaseHistory);

		given(currentPriceManager.hasCurrentPrice("005930")).willReturn(true);
		given(currentPriceManager.getCurrentPrice("005930")).willReturn(Optional.of(Money.won(60000L)));

		// when
		PortfolioGainHistoryCreateResponse response = service.addPortfolioGainHistory();

		// then
		assertAll(
			() -> assertThat(response).extracting("ids")
				.asList()
				.hasSize(1),
			() -> assertThat(portfolioGainHistoryRepository.findAll())
				.extracting(PortfolioGainHistory::getTotalGain, PortfolioGainHistory::getDailyGain,
					PortfolioGainHistory::getCurrentValuation)
				.usingComparatorForType(Money::compareTo, Money.class)
				.containsExactlyInAnyOrder(Tuple.tuple(Money.won(30000L), Money.won(30000L), Money.won(180000L)))
		);
	}
}
