package codesquad.fineants.global.init;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
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
public class SetupDataLoader {
	private final KisService kisService;
	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final PasswordEncoder passwordEncoder;
	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateService exchangeRateService;
	private final AdminProperties adminProperties;
	private final ManagerProperties managerProperties;

	@Transactional
	public void setupResources() {
		setupSecurityResources();
		setupMemberResources();
		setAdminAuthentication();
		setupExchangeRateResources();

		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 시작");
		kisService.refreshCurrentPrice();
		kisService.refreshClosingPrice();
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 종료");
	}

	private void setupSecurityResources() {
		saveRoleIfNotFound("ROLE_ADMIN", "관리자");
		saveRoleIfNotFound("ROLE_MANAGER", "매니저");
		saveRoleIfNotFound("ROLE_USER", "회원");
	}

	private void saveRoleIfNotFound(String roleName, String roleDesc) {
		roleRepository.save(findOrCreateRole(roleName, roleDesc));
	}

	@NotNull
	private Role findOrCreateRole(String roleName, String roleDesc) {
		return roleRepository.findRoleByRoleName(roleName)
			.orElseGet(() -> Role.create(roleName, roleDesc));
	}

	private void setupMemberResources() {
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(supplierNotFoundRoleException());
		Role managerRole = roleRepository.findRoleByRoleName("ROLE_MANAGER")
			.orElseThrow(supplierNotFoundRoleException());
		Role adminRole = roleRepository.findRoleByRoleName("ROLE_ADMIN")
			.orElseThrow(supplierNotFoundRoleException());

		createMemberIfNotFound(
			"dragonbead95@naver.com",
			"일개미1111",
			"nemo1234@",
			Set.of(userRole));
		createMemberIfNotFound(
			adminProperties.getEmail(),
			adminProperties.getNickname(),
			adminProperties.getPassword(),
			Set.of(adminRole));
		createMemberIfNotFound(
			managerProperties.getEmail(),
			managerProperties.getNickname(),
			managerProperties.getPassword(),
			Set.of(managerRole));
	}

	@NotNull
	private static Supplier<FineAntsException> supplierNotFoundRoleException() {
		return () -> new FineAntsException(RoleErrorCode.NOT_EXIST_ROLE);
	}

	private void createMemberIfNotFound(String email, String nickname, String password,
		Set<Role> roleSet) {
		Member member = memberRepository.save(findOrCreateNewMember(email, nickname, password, roleSet));
		initializeNotificationPreferenceIfNotExists(member);
	}

	private Member findOrCreateNewMember(String email, String nickname, String password, Set<Role> roleSet) {
		return memberRepository.findMemberByEmailAndProvider(email, "local")
			.orElseGet(supplierNewMember(email, nickname, password, roleSet));
	}

	private void initializeNotificationPreferenceIfNotExists(Member member) {
		if (member.getNotificationPreference() == null) {
			NotificationPreference newPreference = NotificationPreference.allActive(member);
			member.setNotificationPreference(newPreference);
			notificationPreferenceRepository.save(newPreference);
		}
	}

	@NotNull
	private Supplier<Member> supplierNewMember(String email, String nickname, String password, Set<Role> roleSet) {
		return () -> {
			Member newMember = Member.localMember(email, nickname, passwordEncoder.encode(password));
			Set<MemberRole> memberRoles = roleSet.stream()
				.map(r -> MemberRole.create(newMember, r))
				.collect(Collectors.toUnmodifiableSet());
			newMember.setMemberRoleSet(memberRoles);
			return newMember;
		};
	}

	private void setAdminAuthentication() {
		Member admin = memberRepository.findMemberByEmailAndProvider(adminProperties.getEmail(), "local")
			.orElseThrow(() -> new FineAntsException(MemberErrorCode.NOT_FOUND_MEMBER));
		MemberAuthentication memberAuthentication = MemberAuthentication.from(admin);
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			memberAuthentication,
			Strings.EMPTY,
			memberAuthentication.getSimpleGrantedAuthority()
		);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}

	private void setupExchangeRateResources() {
		List<ExchangeRate> rates = Stream.of(ExchangeRate.base("KRW"), ExchangeRate.zero("USD", false))
			.map(this::saveExchangeRateIfNotFound)
			.toList();
		log.info("create the exchange rates : {}", rates);
		exchangeRateService.updateExchangeRates();
	}

	private ExchangeRate saveExchangeRateIfNotFound(ExchangeRate exchangeRate) {
		return exchangeRateRepository.save(exchangeRate);
	}
}
