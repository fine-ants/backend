package codesquad.fineants.global.init;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.kis.service.KisService;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.domain.entity.Role;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.member.repository.RoleRepository;
import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.notification_preference.repository.NotificationPreferenceRepository;
import codesquad.fineants.domain.stock.service.StockService;
import codesquad.fineants.domain.stock_dividend.service.StockDividendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile(value = {"local", "dev"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private boolean alreadySetup = false;
	private final KisService kisService;
	private final StockService stockService;
	private final StockDividendService stockDividendService;
	private final RoleRepository roleRepository;
	private final MemberRepository memberRepository;
	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
		if (alreadySetup) {
			return;
		}
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 시작");
		kisService.scheduleRefreshingAllStockCurrentPrice();
		kisService.scheduleRefreshingAllLastDayClosingPrice();
		log.info("애플리케이션 시작시 종목 현재가 및 종가 초기화 종료");
		stockService.refreshStocks();
		stockDividendService.initializeStockDividend();

		setupSecurityResources();
		alreadySetup = true;
	}

	@Transactional
	public void setupSecurityResources() {
		Role adminRole = createRoleIfNotFound("ROLE_ADMIN", "관리자");
		Role managerRole = createRoleIfNotFound("ROLE_MANAGER", "매니저");
		Role userRole = createRoleIfNotFound("ROLE_USER", "회원");
		log.info("create the adminRole : {}", adminRole);
		log.info("create the managerRole : {}", managerRole);
		log.info("create the userRole : {}", userRole);

		Member userMember = createMemberIfNotFound("dragonbead95@naver.com", "일개미1111", "nemo1234@", Set.of(userRole));
		Member oauthMember = createOauthMemberIfNotFound("dragonbead95@naver.com", "일개미1112", "naver",
			Set.of(userRole));
		log.info("userMember : {}", userMember);
		log.info("oauthMember : {}", oauthMember);
	}

	@Transactional
	public Role createRoleIfNotFound(String roleName, String roleDesc) {
		Role role = roleRepository.findRoleByRoleName(roleName)
			.orElseGet(() -> Role.create(roleName, roleDesc));
		return roleRepository.save(role);
	}

	@Transactional
	public Member createMemberIfNotFound(String email, String nickname, String password,
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
		notificationPreferenceRepository.save(NotificationPreference.allActive(saveMember));
		return saveMember;
	}

	@Transactional
	public Member createOauthMemberIfNotFound(String email, String nickname, String provider, Set<Role> roleSet) {
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
		notificationPreferenceRepository.save(NotificationPreference.allActive(saveMember));
		return saveMember;
	}
}
