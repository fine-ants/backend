package co.fineants.api.domain.gainhistory.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.api.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.dto.response.PortfolioGainHistoryCreateResponse;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import reactor.core.publisher.Mono;

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

	@MockBean
	private KisClient kisClient;

	@DisplayName("모든 포트폴리오의 손익 내역을 추가한다")
	@Test
	void addPortfolioGainHistory() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio savePortfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portFolioHoldingRepository.save(
			PortfolioHolding.of(savePortfolio, stock, Money.won(60000L)));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePricePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePricePerShare, memo, portfolioHolding));
		given(kisClient.fetchCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L)));

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
