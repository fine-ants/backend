package codesquad.fineants.domain.member.domain.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
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

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "member")
	private NotificationPreference notificationPreference;

	@Builder
	public Member(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, String email,
		String nickname, String provider, String password, String profileUrl,
		NotificationPreference notificationPreference) {
		super(createAt, modifiedAt);
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.provider = provider;
		this.password = password;
		this.profileUrl = profileUrl;
		this.notificationPreference = notificationPreference;
	}

	public Map<String, Object> createClaims() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("memberId", id);
		claims.put("email", email);
		return claims;
	}

	public boolean hasAuthorization(Long memberId) {
		return id.equals(memberId);
	}

	public void updateProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public void updatePassword(String newEncodedPassword) {
		this.password = newEncodedPassword;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}
}
