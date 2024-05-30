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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	@Builder
	public FcmToken(Long id, Member member, String token, LocalDateTime latestActivationTime) {
		this.id = id;
		this.member = member;
		this.token = token;
		this.latestActivationTime = latestActivationTime;
	}

	public void refreshLatestActivationTime() {
		this.latestActivationTime = LocalDateTime.now();
	}
}
