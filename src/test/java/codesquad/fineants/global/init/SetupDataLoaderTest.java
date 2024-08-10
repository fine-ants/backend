package codesquad.fineants.global.init;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.exchangerate.service.ExchangeRateService;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;

class SetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private SetupDataLoader setupDataLoader;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AdminProperties adminProperties;

	@Autowired
	private ManagerProperties managerProperties;

	@MockBean
	private ExchangeRateService exchangeRateService;

	@DisplayName("서버는 권한 및 역할 등의 리소스들을 생성한다")
	@Test
	void setupResources() {
		// given
		doNothing().when(exchangeRateService).updateExchangeRates();
		// when
		setupDataLoader.setupResources();
		// then
		assertThat(roleRepository.findAll())
			.hasSize(3)
			.containsExactlyInAnyOrder(
				Role.create("ROLE_ADMIN", "관리자"),
				Role.create("ROLE_MANAGER", "매니저"),
				Role.create("ROLE_USER", "회원")
			);
		assertThat(memberRepository.findAll())
			.hasSize(3)
			.containsExactlyInAnyOrder(
				Member.localMember(adminProperties.getEmail(), adminProperties.getNickname(),
					adminProperties.getPassword()),
				Member.localMember(managerProperties.getEmail(), managerProperties.getNickname(),
					managerProperties.getPassword()),
				Member.localMember("dragonbead95@naver.com", "일개미1111", "nemo1234@")
			);
	}
}
