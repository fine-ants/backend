package codesquad.fineants.domain.member.domain.entity;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "member_role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@ToString
public class MemberRole {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_role_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	private Role role;

	public static MemberRole create(Member member, Role role) {
		return new MemberRole(null, member, role);
	}

	//** 연관 관계 메서드 시작 **//
	public void setMember(Member member) {
		this.member = member;
	}
	//** 연관 관계 메서드 종료 **//

	public SimpleGrantedAuthority toSimpleGrantedAuthority() {
		return role.toSimpleGrantedAuthority();
	}

	public String getRoleName() {
		return role.getRoleName();
	}
}
