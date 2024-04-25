package codesquad.fineants.domain.portfolio_gain_history;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;

class PortfolioGainHistoryServiceRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioGainHistoryRepository repository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@AfterEach
	void tearDown() {
		repository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("주어진 날짜보다 같거나 작은 데이터들중 가장 최근의 데이터를 한개 조회한다")
	@Test
	void findFirstByCreateAtIsLessThanEqualOrderByCreateAtDesc() {
		// given
		Member member = memberRepository.save(Member.builder()
			.nickname("일개미1234")
			.email("kim1234@gmail.com")
			.password("kim1234@")
			.provider("local")
			.build());

		Portfolio portfolio = portfolioRepository.save(Portfolio.builder()
			.name("내꿈은 워렌버핏")
			.securitiesFirm("토스")
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.build());

		PortfolioGainHistory portfolioGainHistory1 = PortfolioGainHistory.builder()
			.totalGain(Money.won(10000L))
			.dailyGain(Money.won(10000L))
			.cash(Money.won(1000000L))
			.currentValuation(Money.won(110000L))
			.portfolio(portfolio)
			.build();
		PortfolioGainHistory portfolioGainHistory2 = PortfolioGainHistory.builder()
			.totalGain(Money.won(20000L))
			.dailyGain(Money.won(10000L))
			.cash(Money.won(1000000L))
			.currentValuation(Money.won(120000L))
			.portfolio(portfolio)
			.build();
		repository.save(portfolioGainHistory1);
		repository.save(portfolioGainHistory2);

		// when
		PortfolioGainHistory result = repository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
			portfolio.getId(), LocalDateTime.now()).orElseThrow();

		// then
		assertThat(result.getCurrentValuation()).isEqualByComparingTo(Money.won(120000L));

	}

}
