package codesquad.fineants.global.security.ajax.provider;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import codesquad.fineants.domain.member.domain.entity.Member;
import lombok.Getter;

@Getter
public class MemberContext extends User {

	private final Member member;

	public MemberContext(Member member, Collection<? extends GrantedAuthority> authorities) {
		super(member.getEmail(), member.getPassword(), authorities);
		this.member = member;
	}
}
