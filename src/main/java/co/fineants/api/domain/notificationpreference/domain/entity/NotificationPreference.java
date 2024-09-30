package co.fineants.api.domain.notificationpreference.domain.entity;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.member.domain.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
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

	private NotificationPreference(boolean browserNotify, boolean targetGainNotify, boolean maxLossNotify,
		boolean targetPriceNotify) {
		this(null, browserNotify, targetGainNotify, maxLossNotify, targetPriceNotify);
	}

	private NotificationPreference(Long id, boolean browserNotify, boolean targetGainNotify, boolean maxLossNotify,
		boolean targetPriceNotify) {
		this.id = id;
		this.browserNotify = browserNotify;
		this.targetGainNotify = targetGainNotify;
		this.maxLossNotify = maxLossNotify;
		this.targetPriceNotify = targetPriceNotify;
	}

	public static NotificationPreference allActive() {
		return new NotificationPreference(true, true, true, true);
	}

	public static NotificationPreference defaultSetting() {
		return new NotificationPreference(false, false, false, false);
	}

	public static NotificationPreference create(boolean browserNotify, boolean targetGainNotify, boolean maxLossNotify,
		boolean targetPriceNotify) {
		return new NotificationPreference(browserNotify, targetGainNotify, maxLossNotify, targetPriceNotify);
	}

	public void changeMember(Member member) {
		this.member = member;
		if (member != null && member.getNotificationPreference() != this) {
			member.changeNotificationPreference(this);
		}
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

	public boolean hasAuthorization(Long memberId) {
		return member.hasAuthorization(memberId);
	}
}
