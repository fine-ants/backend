package codesquad.fineants.domain.fcm_token;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
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
