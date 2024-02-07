package codesquad.fineants.domain.notification_preference;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class NotificationPreference extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private boolean browserNotify;

	private boolean targetGainNotify;

	private boolean maxLossNotify;

	private boolean targetPriceNotify;

	@Builder
	public NotificationPreference(Member member, boolean browserNotify, boolean targetGainNotify, boolean maxLossNotify,
		boolean targetPriceNotify) {
		this.member = member;
		this.browserNotify = browserNotify;
		this.targetGainNotify = targetGainNotify;
		this.maxLossNotify = maxLossNotify;
		this.targetPriceNotify = targetPriceNotify;
	}

	public void changePreference(NotificationPreference notificationPreference) {
		this.browserNotify = notificationPreference.browserNotify;
		this.targetGainNotify = notificationPreference.targetGainNotify;
		this.maxLossNotify = notificationPreference.maxLossNotify;
		this.targetPriceNotify = notificationPreference.targetPriceNotify;
	}
}
