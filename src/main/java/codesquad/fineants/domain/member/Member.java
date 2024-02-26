package codesquad.fineants.domain.member;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.notification_preference.NotificationPreference;
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
