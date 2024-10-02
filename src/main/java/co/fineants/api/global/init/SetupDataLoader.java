package co.fineants.api.global.init;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.exchangerate.domain.entity.ExchangeRate;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.domain.exchangerate.service.ExchangeRateUpdateService;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.entity.MemberRole;
import co.fineants.api.domain.member.domain.entity.Role;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.member.repository.RoleRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.RoleErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.init.properties.AdminProperties;
import co.fineants.api.global.init.properties.ManagerProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import co.fineants.api.global.init.properties.UserProperties;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.infra.s3.service.AmazonS3DividendService;
import co.fineants.api.infra.s3.service.AmazonS3StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader {
	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final PasswordEncoder passwordEncoder;
	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateUpdateService exchangeRateUpdateService;
	private final AdminProperties adminProperties;
	private final ManagerProperties managerProperties;
	private final UserProperties userProperties;
	private final RoleProperties roleProperties;
	private final AmazonS3StockService amazonS3StockService;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final AmazonS3DividendService amazonS3DividendService;

	@Transactional
	public void setupResources() {
		setupSecurityResources();
		setupMemberResources();
		setAdminAuthentication();
		setupExchangeRateResources();
		setupStockResources();
		setupStockDividendResources();
	}

	private void setupSecurityResources() {
		roleProperties.getRolePropertyList().forEach(this::saveRoleIfNotFound);
	}

	private void saveRoleIfNotFound(RoleProperties.RoleProperty roleProperty) {
		roleRepository.save(findOrCreateRole(roleProperty));
	}

	@NotNull
	private Role findOrCreateRole(RoleProperties.RoleProperty roleProperty) {
		return roleRepository.findRoleByRoleName(roleProperty.getRoleName())
			.orElseGet(roleProperty::toRoleEntity);
	}

	private void setupMemberResources() {
		Role userRole = roleRepository.findRoleByRoleName("ROLE_USER")
			.orElseThrow(supplierNotFoundRoleException());
		Role managerRole = roleRepository.findRoleByRoleName("ROLE_MANAGER")
			.orElseThrow(supplierNotFoundRoleException());
		Role adminRole = roleRepository.findRoleByRoleName("ROLE_ADMIN")
			.orElseThrow(supplierNotFoundRoleException());

		createMemberIfNotFound(
			userProperties.getEmail(),
			userProperties.getNickname(),
			userProperties.getPassword(),
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
			NotificationPreference newPreference = NotificationPreference.allActive();
			member.setNotificationPreference(newPreference);
			notificationPreferenceRepository.save(newPreference);
		}
	}

	@NotNull
	private Supplier<Member> supplierNewMember(String email, String nickname, String password, Set<Role> roleSet) {
		return () -> {
			MemberProfile profile = MemberProfile.localMemberProfile(email, nickname, passwordEncoder.encode(password),
				null);
			Member newMember = Member.localMember(profile);
			MemberRole[] memberRoles = roleSet.stream()
				.map(r -> MemberRole.of(newMember, r))
				.toArray(MemberRole[]::new);
			newMember.addMemberRole(memberRoles);
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
		exchangeRateUpdateService.updateExchangeRates();
	}

	private ExchangeRate saveExchangeRateIfNotFound(ExchangeRate exchangeRate) {
		return exchangeRateRepository.save(exchangeRate);
	}

	private void setupStockResources() {
		List<Stock> stocks = stockRepository.saveAll(amazonS3StockService.fetchStocks());
		log.info("setupStock count is {}", stocks.size());
	}

	private void setupStockDividendResources() {
		List<StockDividend> dividends = stockDividendRepository.saveAll(amazonS3DividendService.fetchDividends());
		log.info("setupStockDividend count is {}", dividends.size());
	}
}
