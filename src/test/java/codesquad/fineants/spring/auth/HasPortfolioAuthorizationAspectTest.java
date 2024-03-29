package codesquad.fineants.spring.auth;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.portfolio.aop.HasPortfolioAuthorizationAspect;

class HasPortfolioAuthorizationAspectTest extends AbstractContainerBaseTest {

	@Autowired
	private HasPortfolioAuthorizationAspect hasPortfolioAuthorizationAspect;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@AfterEach
	void tearDown() {
		portfolioRepository.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@DisplayName("사용자는 포트폴리오에 대한 권한을 가지고 있어서 에러를 발생시키지 않고 통과한다")
	@Test
	void hasAuthorization() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		// when & then
		assertThatCode(
			() -> hasPortfolioAuthorizationAspect.hasAuthorization(null, portfolio.getId(), AuthMember.from(member)))
			.doesNotThrowAnyException();
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
