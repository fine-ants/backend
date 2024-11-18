package co.fineants.api.domain.holding.repository;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;

class PortfolioHoldingRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository holdingRepository;

	@DisplayName("포트폴리오 종목들의 모든 티커 심보들을 조회한다")
	@Test
	void findAllTickerSymbol() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock samsung = stockRepository.save(createSamsungStock());
		Stock kakao = stockRepository.save(createKakaoStock());
		holdingRepository.save(createPortfolioHolding(portfolio, samsung));
		holdingRepository.save(createPortfolioHolding(portfolio, kakao));

		member = memberRepository.save(createMember("ant1234"));
		portfolio = portfolioRepository.save(createPortfolio(member));
		Stock dongwha = stockRepository.save(createDongwhaPharmStock());
		holdingRepository.save(createPortfolioHolding(portfolio, samsung));
		holdingRepository.save(createPortfolioHolding(portfolio, kakao));
		holdingRepository.save(createPortfolioHolding(portfolio, dongwha));

		// when
		List<String> actual = holdingRepository.findAllTickerSymbol();
		// then
		Assertions.assertThat(actual)
			.hasSize(3)
			.containsExactly("000020", "005930", "035720");
	}
}
