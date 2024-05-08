package codesquad.fineants.domain.portfolio_gain_history.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
import codesquad.fineants.domain.portfolio.repository.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.domain.entity.PortfolioGainHistory;
import codesquad.fineants.AbstractContainerBaseTest;

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
			.totalGain(Money.zero())
			.dailyGain(Money.zero())
			.currentValuation(Money.zero())
			.cash(Money.zero())
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
			.totalGain(Money.zero())
			.dailyGain(Money.zero())
			.currentValuation(Money.zero())
			.cash(Money.zero())
			.portfolio(portfolio)
			.build());
		PortfolioGainHistory saveHistory = portfolioGainHistoryRepository.save(PortfolioGainHistory.builder()
			.totalGain(Money.zero())
			.dailyGain(Money.zero())
			.currentValuation(Money.zero())
			.cash(Money.zero())
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
			.budget(Money.won(1000000L))
			.targetGain(Money.won(1500000L))
			.maximumLoss(Money.won(900000L))
			.member(member)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.build();
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
		portfolioGainHistoryRepository.save(portfolioGainHistory1);
		portfolioGainHistoryRepository.save(portfolioGainHistory2);

		// when
		PortfolioGainHistory result = portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
			portfolio.getId(), LocalDateTime.now()).orElseThrow();

		// then
		assertThat(result.getCurrentValuation()).isEqualByComparingTo(Money.won(120000L));

	}
}
