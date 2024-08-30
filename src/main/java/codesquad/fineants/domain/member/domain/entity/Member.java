package codesquad.fineants.domain.member.domain.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@ToString(exclude = {"notificationPreference", "roles"})
@EqualsAndHashCode(of = {"email", "nickname", "provider"}, callSuper = false)
public class Member extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	@Column(name = "nickname", unique = true, nullable = false)
	private String nickname;
	private String provider;
	private String password;
	private String profileUrl;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "member", orphanRemoval = true, cascade = CascadeType.ALL)
	private NotificationPreference notificationPreference;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "member", orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<MemberRole> roles;

	private Member(Long id, String email, String nickname, String provider, String password, String profileUrl,
		NotificationPreference notificationPreference, Set<MemberRole> roles) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.password = password;
		this.profileUrl = profileUrl;
		this.notificationPreference = notificationPreference;
		this.roles = roles;
	}

	public static Member oauthMember(String email, String nickname, String provider,
		String profileUrl) {
		return new Member(null, email, nickname, provider, null, profileUrl, null, new HashSet<>());
	}

	public static Member localMember(String email, String nickname, String password) {
		return localMember(email, nickname, password, null);
	}

	public static Member localMember(String email, String nickname, String password, String profileUrl) {
		return localMember(null, email, nickname, password, profileUrl);
	}

	public static Member localMember(Long id, String email, String nickname, String password, String profileUrl) {
		return new Member(id, email, nickname, "local", password, profileUrl, null, new HashSet<>());
	}

	public void addMemberRole(MemberRole memberRole) {
		if (!this.roles.contains(memberRole)) {
			roles.add(memberRole);
			memberRole.setMember(this);
		}
	}

	public void addMemberRole(Set<MemberRole> memberRoleSet) {
		for (MemberRole memberRole : memberRoleSet) {
			addMemberRole(memberRole);
		}
	}

	public void setMemberRoleSet(Set<MemberRole> memberRoleSet) {
		this.roles = memberRoleSet;
	}

	public void setNotificationPreference(NotificationPreference newPreference) {
		this.notificationPreference = newPreference;
		newPreference.setMember(this);
	}

	public boolean hasAuthorization(Long memberId) {
		return id.equals(memberId);
	}

	public Member updateProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
		return this;
	}

	public void updatePassword(String newEncodedPassword) {
		this.password = newEncodedPassword;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public Collection<GrantedAuthority> getSimpleGrantedAuthorities() {
		return roles.stream()
			.map(MemberRole::toSimpleGrantedAuthority)
			.collect(Collectors.toSet());
	}

	public Map<String, Object> toMemberAttributeMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("id", id);
		result.put("email", email);
		result.put("nickname", nickname);
		result.put("provider", provider);
		result.put("profileUrl", profileUrl);
		result.put("roleSet", roles.stream()
			.map(MemberRole::getRoleName)
			.collect(Collectors.toSet()));
		return result;
	}

	public Set<MemberRole> getRoles() {
		return Collections.unmodifiableSet(roles);
	}
}
