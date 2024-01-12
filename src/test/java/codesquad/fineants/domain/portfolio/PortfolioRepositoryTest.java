package codesquad.fineants.domain.portfolio;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;

@ActiveProfiles("test")
@SpringBootTest
class PortfolioRepositoryTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@AfterEach
	void tearDown() {
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

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
