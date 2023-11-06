package codesquad.fineants.domain.portfolio_gain_history;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;

@ActiveProfiles("test")
@SpringBootTest
class PortfolioGainHistoryServiceRepositoryTest {

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
			.budget(1000000L)
			.targetGain(1500000L)
			.maximumLoss(900000L)
			.member(member)
			.build());

		PortfolioGainHistory portfolioGainHistory1 = PortfolioGainHistory.builder()
			.totalGain(10000L)
			.dailyGain(10000L)
			.currentValuation(110000L)
			.portfolio(portfolio)
			.build();
		PortfolioGainHistory portfolioGainHistory2 = PortfolioGainHistory.builder()
			.totalGain(20000L)
			.dailyGain(10000L)
			.currentValuation(120000L)
			.portfolio(portfolio)
			.build();
		repository.save(portfolioGainHistory1);
		repository.save(portfolioGainHistory2);

		// when
		PortfolioGainHistory result = repository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
			portfolio.getId(), LocalDateTime.now()).orElseThrow();

		// then
		assertThat(result).extracting("currentValuation").isEqualTo(120000L);

	}

}
