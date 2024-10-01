package co.fineants.api.domain.member.domain.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
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
@EqualsAndHashCode(of = {"profile"}, callSuper = false)
public class Member extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private MemberProfile profile;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "member", orphanRemoval = true, cascade = CascadeType.ALL)
	private NotificationPreference notificationPreference;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "member", orphanRemoval = true, cascade = CascadeType.ALL)
	private final Set<MemberRole> roles = new HashSet<>();

	private Member(MemberProfile profile) {
		this(null, profile);
	}

	private Member(Long id, MemberProfile profile) {
		this.id = id;
		this.profile = profile;
	}

	public static Member oauthMember(MemberProfile profile) {
		return new Member(null, profile);
	}

	public static Member localMember(MemberProfile profile) {
		return new Member(profile);
	}

	public static Member localMember(Long id, MemberProfile profile) {
		return new Member(id, profile);
	}

	//** 연관 관계 엔티티 메서드 시작 **//
	public void addMemberRole(MemberRole... memberRole) {
		for (MemberRole role : memberRole) {
			if (this.roles.contains(role)) {
				continue;
			}
			this.roles.add(role);
			role.setMember(this);
		}
	}

	public void removeMemberRole(MemberRole memberRole) {
		this.roles.remove(memberRole);
		memberRole.setMember(null);
	}

	public void changeNotificationPreference(NotificationPreference notificationPreference) {
		if (this.notificationPreference != null) {
			this.notificationPreference.changeMember(null);
		}

		this.notificationPreference = notificationPreference;
		if (notificationPreference != null && notificationPreference.getMember() != this) {
			notificationPreference.changeMember(this);
		}
	}

	//** 연관 관계 엔티티 메서드 종료 **//

	public boolean hasAuthorization(Long memberId) {
		return id.equals(memberId);
	}

	public void changeProfileUrl(String profileUrl) {
		profile.changeProfileUrl(profileUrl);
	}

	public void changeNickname(String nickname) {
		this.profile.changeNickname(nickname);
	}

	public Collection<GrantedAuthority> getSimpleGrantedAuthorities() {
		return roles.stream()
			.map(MemberRole::toSimpleGrantedAuthority)
			.collect(Collectors.toSet());
	}

	public Map<String, Object> toMemberAttributeMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("id", id);
		result.putAll(profile.toMap());
		result.put("roleSet", roles.stream()
			.map(MemberRole::getRoleName)
			.collect(Collectors.toSet()));
		return result;
	}

	public Set<MemberRole> getRoles() {
		return Collections.unmodifiableSet(roles);
	}

	public Optional<String> getPassword() {
		return profile.getPassword();
	}

	public String getProvider() {
		return profile.getProvider();
	}

	public String getNickname() {
		return profile.getNickname();
	}

	public String getEmail() {
		return profile.getEmail();
	}

	public Optional<String> getProfileUrl() {
		return profile.getProfileUrl();
	}
}
