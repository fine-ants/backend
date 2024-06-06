package codesquad.fineants.domain.fcm.domain.entity;

import java.time.LocalDateTime;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.domain.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = "member")
@Table(name = "fcm_token", uniqueConstraints = {
	@UniqueConstraint(name = "token_member_id_unique", columnNames = {"token", "member_id"})
})
@Entity
public class FcmToken extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private String token;

	private LocalDateTime latestActivationTime;

	public static FcmToken create(Member member, String token) {
		return create(null, member, token);
	}

	public static FcmToken create(Long id, Member member, String token) {
		return new FcmToken(id, member, token, LocalDateTime.now());
	}

	public void refreshLatestActivationTime() {
		this.latestActivationTime = LocalDateTime.now();
	}
}
