package co.fineants.api.global.security.oauth.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;

class MemberAuthenticationTest extends AbstractContainerBaseTest {

	@DisplayName("Member를 MemberAuthentication으로 변환한다")
	@Test
	void from() {
		// given
		Member member = createMember();
		// when
		MemberAuthentication authentication = MemberAuthentication.from(member);
		// then
		Assertions.assertThat(authentication.toString())
			.hasToString("MemberAuthentication(id=null, nickname=nemo1234, "
				+ "email=dragonbead95@naver.com, roles=[ROLE_USER])");
	}

}
