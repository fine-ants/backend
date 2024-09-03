package co.fineants.api.global.security.ajax.provider;

import java.util.Collection;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import co.fineants.api.domain.member.domain.entity.Member;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MemberContext extends User {

	private final Member member;

	public MemberContext(Member member, Collection<? extends GrantedAuthority> authorities) {
		super(member.getEmail(), member.getPassword(), authorities);
		this.member = member;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null || getClass() != object.getClass())
			return false;
		if (!super.equals(object))
			return false;
		MemberContext that = (MemberContext)object;
		return Objects.equals(member, that.member);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), member);
	}
}
