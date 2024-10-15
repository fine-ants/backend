package co.fineants.api.domain.holding.domain.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.stock.domain.entity.Stock;

class PortfolioHoldingTest extends AbstractContainerBaseTest {

	@DisplayName("포트폴리오 종목에 포트폴리오를 설정한다")
	@Test
	void setPortfolio() {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();
		PortfolioHolding holding = createPortfolioHolding(portfolio, stock);

		Portfolio other = createPortfolio(member, "other");
		// when
		holding.setPortfolio(other);
		// then
		Assertions.assertThat(portfolio.getPortfolioHoldings()).isEmpty();
		Assertions.assertThat(other.getPortfolioHoldings())
			.hasSize(1)
			.containsExactlyInAnyOrder(createPortfolioHolding(other, stock));
		Assertions.assertThat(holding.getPortfolio()).isEqualTo(other);
	}
}
