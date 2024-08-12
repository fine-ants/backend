package codesquad.fineants.domain.gainhistory.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.gainhistory.domain.dto.response.PortfolioGainHistoryCreateResponse;
import codesquad.fineants.domain.gainhistory.domain.entity.PortfolioGainHistory;
import codesquad.fineants.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
import codesquad.fineants.domain.holding.repository.PortfolioHoldingRepository;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;
import codesquad.fineants.domain.kis.repository.CurrentPriceRedisRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.purchasehistory.repository.PurchaseHistoryRepository;
import codesquad.fineants.domain.stock.domain.entity.Stock;
import codesquad.fineants.domain.stock.repository.StockRepository;

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
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	// TODO: Kis 접근 토큰 모킹 처리
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

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create(stock.getTickerSymbol(), 60000L));

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
