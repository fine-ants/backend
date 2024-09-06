package co.fineants.api.domain.gainhistory.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;

class PortfolioGainHistoryRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@DisplayName("포트폴리오 등록번호를 가진 손익내역들을 조회한다")
	@Test
	void findAllByPortfolioId() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		portfolioGainHistoryRepository.save(PortfolioGainHistory.empty(portfolio));

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
		portfolioGainHistoryRepository.save(PortfolioGainHistory.empty(portfolio));
		PortfolioGainHistory saveHistory = portfolioGainHistoryRepository.save(PortfolioGainHistory.empty(portfolio));

		// when
		PortfolioGainHistory history =
			portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(),
					LocalDateTime.now())
				.stream()
				.findFirst()
				.orElseThrow();

		// then
		assertThat(history.getId()).isEqualTo(saveHistory.getId());
	}

	@DisplayName("주어진 날짜보다 같거나 작은 데이터들중 가장 최근의 데이터를 한개 조회한다")
	@Test
	void findFirstByCreateAtIsLessThanEqualOrderByCreateAtDesc() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		PortfolioGainHistory portfolioGainHistory1 = PortfolioGainHistory.create(
			Money.won(10000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(110000L),
			portfolio
		);

		PortfolioGainHistory portfolioGainHistory2 = PortfolioGainHistory.create(
			Money.won(20000L),
			Money.won(10000L),
			Money.won(1000000L),
			Money.won(120000L),
			portfolio
		);
		portfolioGainHistoryRepository.save(portfolioGainHistory1);
		portfolioGainHistoryRepository.save(portfolioGainHistory2);

		// when
		PortfolioGainHistory result =
			portfolioGainHistoryRepository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
					portfolio.getId(), LocalDateTime.now())
				.stream()
				.findFirst()
				.orElseThrow();

		// then
		assertThat(result.getCurrentValuation()).isEqualByComparingTo(Money.won(120000L));

	}
}
