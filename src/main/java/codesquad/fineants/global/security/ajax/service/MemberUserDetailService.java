package codesquad.fineants.global.security.ajax.service;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.domain.entity.MemberRole;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.global.security.ajax.provider.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("memberUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class MemberUserDetailService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Member member = memberRepository.findMemberByEmailAndProvider(email, "local")
			.orElseThrow(() -> new BadCredentialsException("invalid email"));
		log.debug("findMember : {}", member);
		List<GrantedAuthority> roles = member.getRoles().stream()
			.map(MemberRole::toSimpleGrantedAuthority)
			.toList();
		log.debug("fineMember's roles : {}", roles);
		return new MemberContext(member, roles);
	}
}
