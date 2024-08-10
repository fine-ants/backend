package codesquad.fineants.global.init;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.exchangerate.domain.entity.ExchangeRate;
import codesquad.fineants.domain.exchangerate.repository.ExchangeRateRepository;
import codesquad.fineants.domain.exchangerate.service.ExchangeRateService;
import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.notificationpreference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.RoleErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"local", "dev", "release", "production"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private boolean alreadySetup = false;
	private final KisService kisService;
	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final PasswordEncoder passwordEncoder;
	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateService exchangeRateService;
	@Value("${member.admin.password}")
	private String password;

	@Override
	@Transactional
	public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
		if (alreadySetup) {
			return;
		}
		setupSecurityResources();
		setupMemberResources();
		setAdminAuthentication();
		setupExchangeRateResources();

		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 시작");
		kisService.refreshCurrentPrice();
		kisService.refreshClosingPrice();
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 종료");
		alreadySetup = true;
	}

	private void setAdminAuthentication() {
		Member admin = memberRepository.findMemberByEmailAndProvider("admin@admin.com", "local")
			.orElseThrow(() -> new FineAntsException(MemberErrorCode.NOT_FOUND_MEMBER));
		MemberAuthentication memberAuthentication = MemberAuthentication.from(admin);
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			memberAuthentication,
			Strings.EMPTY,
			memberAuthentication.getSimpleGrantedAuthority()
		);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}

	private void setupSecurityResources() {
		createRoleIfNotFound("ROLE_ADMIN", "관리자");
		createRoleIfNotFound("ROLE_MANAGER", "매니저");
		createRoleIfNotFound("ROLE_USER", "회원");
	}

	private void setupMemberResources() {
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(() -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE));
		Role managerRole = roleRepository.findRoleByRoleName("ROLE_MANAGER")
			.orElseThrow(() -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE));
		Role adminRole = roleRepository.findRoleByRoleName("ROLE_ADMIN")
			.orElseThrow(() -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE));

		createMemberIfNotFound("dragonbead95@naver.com", "일개미1111", "nemo1234@",
			Set.of(userRole));
		createOauthMemberIfNotFound("dragonbead95@naver.com", "일개미1112", "naver",
			Set.of(userRole));
		createMemberIfNotFound("admin@admin.com", "admin", password, Set.of(adminRole));
		createMemberIfNotFound("manager@manager.com", "manager", password, Set.of(managerRole));
	}

	private void setupExchangeRateResources() {
		List<ExchangeRate> rates = List.of(
			createExchangeRateIfNotFound(ExchangeRate.base("KRW")),
			createExchangeRateIfNotFound(ExchangeRate.zero("USD", false))
		);
		exchangeRateService.updateExchangeRates();
		log.info("환율 생성 : {}", rates);
	}

	private ExchangeRate createExchangeRateIfNotFound(ExchangeRate exchangeRate) {
		return exchangeRateRepository.save(exchangeRate);
	}

	private void createRoleIfNotFound(String roleName, String roleDesc) {
		Role role = roleRepository.findRoleByRoleName(roleName)
			.orElseGet(() -> Role.create(roleName, roleDesc));
		roleRepository.save(role);
	}

	private void createMemberIfNotFound(String email, String nickname, String password,
		Set<Role> roleSet) {
		Member member = memberRepository.findMemberByEmailAndProvider(email, "local")
			.orElse(null);

		if (member == null) {
			member = Member.localMember(email, nickname, passwordEncoder.encode(password));
			Set<MemberRole> memberRoleSet = new HashSet<>();
			for (Role r : roleSet) {
				MemberRole memberRole = MemberRole.create(member, r);
				memberRoleSet.add(memberRole);
			}
			member.setMemberRoleSet(memberRoleSet);
		}
		Member saveMember = memberRepository.save(member);
		NotificationPreference preference = member.getNotificationPreference();
		if (preference == null) {
			NotificationPreference newPreference = NotificationPreference.allActive(saveMember);
			saveMember.setNotificationPreference(newPreference);
			notificationPreferenceRepository.save(newPreference);
		}
	}

	private void createOauthMemberIfNotFound(String email, String nickname, String provider, Set<Role> roleSet) {
		Member member = memberRepository.findMemberByEmailAndProvider(email, provider)
			.orElse(null);

		if (member == null) {
			member = Member.oauthMember(email, nickname, provider, null);
			Set<MemberRole> memberRoleSet = new HashSet<>();
			for (Role r : roleSet) {
				MemberRole memberRole = MemberRole.create(member, r);
				memberRoleSet.add(memberRole);
			}
			member.setMemberRoleSet(memberRoleSet);
		}
		Member saveMember = memberRepository.save(member);
		NotificationPreference preference = member.getNotificationPreference();
		if (preference == null) {
			NotificationPreference newPreference = NotificationPreference.allActive(saveMember);
			saveMember.setNotificationPreference(newPreference);
			notificationPreferenceRepository.save(newPreference);
		}
	}
}
