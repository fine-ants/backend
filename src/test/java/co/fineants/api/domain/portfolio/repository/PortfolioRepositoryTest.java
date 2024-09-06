package co.fineants.api.domain.portfolio.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;

class PortfolioRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@DisplayName("회원이 가지고 있는 포트폴리오들을 조회한다")
	@Test
	void findAllByMemberId() {
		// given
		Member member = memberRepository.save(createMember());
		portfolioRepository.save(createPortfolio(member));
		// when
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberId(member.getId());
		// then
		assertThat(portfolios)
			.hasSize(1);
	}

	@DisplayName("포트폴리오 이름과 회원이 매칭되는 포트폴리오가 존재하는지 조회한다")
	@Test
	void existsByNameAndMember() {
		// given
		Member member = memberRepository.save(createMember());
		portfolioRepository.save(createPortfolio(member));

		// when
		boolean actual = portfolioRepository.existsByNameAndMember("내꿈은 워렌버핏", member);

		// then
		assertThat(actual).isTrue();
	}

	@DisplayName("회원 등록번호에 따른 포트폴리오들을 등록번호를 기준으로 내림차순으로 조회한다")
	@Test
	void findAllByMemberIdOrderByIdDesc() {
		// given
		Member member = memberRepository.save(createMember());
		portfolioRepository.save(createPortfolio(member));

		// when
		List<Portfolio> portfolios = portfolioRepository.findAllByMemberIdOrderByIdDesc(member.getId());

		// then
		assertThat(portfolios)
			.hasSize(1);
	}

	@DisplayName("특정한 한 포트폴리오를 조회합니다")
	@Test
	void findById() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		// when
		Portfolio findPortfolio = portfolioRepository.findById(portfolio.getId())
			.orElseThrow();

		// then
		Assertions.assertThat(portfolio.getId()).isEqualTo(findPortfolio.getId());
	}
}
