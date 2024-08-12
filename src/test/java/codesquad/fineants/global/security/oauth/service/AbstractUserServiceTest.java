package codesquad.fineants.global.security.oauth.service;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.member.service.NicknameGenerator;
import codesquad.fineants.domain.notificationpreference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.security.oauth.dto.OAuthAttribute;

class AbstractUserServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationPreferenceRepository notificationPreferenceRepository;

	@Autowired
	private NicknameGenerator nicknameGenerator;

	@Autowired
	private RoleRepository roleRepository;

	@Transactional
	@DisplayName("OAuth google 계정이 다른 프로필로 변경한 상태에서 회원 정보 저장시 프로필 사진을 유지한다")
	@Test
	void saveOrUpdate_givenOtherProfileUrl_whenSaveOrUpdate_thenMaintainProfileUrl() {
		// given
		memberRepository.save(createOauthMember());
		AbstractUserService userService = new CustomOidcUserService(memberRepository, notificationPreferenceRepository,
			nicknameGenerator, roleRepository);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("email", "fineants1234@gmail.com");
		attributes.put("profile", "profileUrl0");
		attributes.put("sub", "123445");
		OAuthAttribute googleOAuth = OAuthAttribute.of("google", attributes, "sub");
		// when
		Member member = userService.saveOrUpdate(googleOAuth);
		// then
		assertThat(member.getProfileUrl()).isEqualTo("profileUrl1");
	}

}
