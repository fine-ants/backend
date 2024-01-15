package codesquad.fineants.domain.portfolio_gain_history;

import java.util.List;

import org.assertj.core.api.Assertions;
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
class PortfolioGainHistoryRepositoryTest {

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@AfterEach
	void tearDown() {
		portfolioGainHistoryRepository.deleteAllInBatch();
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("포트폴리오 등록번호를 가진 손익내역들을 조회한다")
	@Test
	void findAllByPortfolioId() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(0L)
			.dailyGain(0L)
			.currentValuation(0L)
			.cash(0L)
			.portfolio(portfolio)
			.build());

		// when
		List<PortfolioGainHistory> histories = portfolioGainHistoryRepository.findAllByPortfolioId(
			portfolio.getId());

		// then
		Assertions.assertThat(histories).hasSize(1);
	}

	private Member createMember() {
		return Member.builder()
			.nickname("kim1234")
			.email("kim1234@naver.com")
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

}
