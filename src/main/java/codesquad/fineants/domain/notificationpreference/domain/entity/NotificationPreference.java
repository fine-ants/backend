package codesquad.fineants.domain.notificationpreference.domain.entity;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.domain.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

	private boolean browserNotify;

	private boolean targetGainNotify;

	private boolean maxLossNotify;

	private boolean targetPriceNotify;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	public NotificationPreference(Member member, boolean browserNotify, boolean targetGainNotify, boolean maxLossNotify,
		boolean targetPriceNotify) {
		this.member = member;
		this.browserNotify = browserNotify;
		this.targetGainNotify = targetGainNotify;
		this.maxLossNotify = maxLossNotify;
		this.targetPriceNotify = targetPriceNotify;
	}

	public static NotificationPreference allActive(Member member) {
		return NotificationPreference.builder()
			.browserNotify(true)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.member(member)
			.build();
	}

	public static NotificationPreference defaultSetting(Member member) {
		return NotificationPreference.builder()
			.browserNotify(false)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.member(member)
			.build();
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public void changePreference(NotificationPreference notificationPreference) {
		this.browserNotify = notificationPreference.browserNotify;
		this.targetGainNotify = notificationPreference.targetGainNotify;
		this.maxLossNotify = notificationPreference.maxLossNotify;
		this.targetPriceNotify = notificationPreference.targetPriceNotify;
	}

	public boolean isAllInActive() {
		return !this.browserNotify
			&& !this.targetGainNotify
			&& !this.maxLossNotify
			&& !this.targetPriceNotify;
	}

	public boolean isPossibleTargetGainNotification() {
		return this.browserNotify && this.targetGainNotify;
	}

	public boolean isPossibleMaxLossNotification() {
		return this.browserNotify && this.maxLossNotify;
	}

	public boolean isPossibleStockTargetPriceNotification() {
		return this.browserNotify && this.targetPriceNotify;
	}
}
