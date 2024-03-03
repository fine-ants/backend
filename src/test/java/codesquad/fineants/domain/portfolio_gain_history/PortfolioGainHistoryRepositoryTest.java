package codesquad.fineants.domain.portfolio_gain_history;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;

class PortfolioGainHistoryRepositoryTest extends AbstractContainerBaseTest {

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
		assertThat(histories).hasSize(1);
	}

	@DisplayName("사용자는 제일 최근의 포트폴리오 손익 내역을 조회합니다")
	@Test
	void findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc() {
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
		PortfolioGainHistory saveHistory = portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(0L)
			.dailyGain(0L)
			.currentValuation(0L)
			.cash(0L)
			.portfolio(portfolio)
			.build());

		// when
		Optional<PortfolioGainHistory> optional = portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
			portfolio.getId(),
			LocalDateTime.now());
		PortfolioGainHistory history = optional.orElseThrow();

		// then
		assertThat(history.getId()).isEqualTo(saveHistory.getId());
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
			.maximumLossIsActive(false)
			.build();
	}

}
