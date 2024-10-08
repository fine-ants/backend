package co.fineants.api.domain.portfolio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioNotificationPreference {
	@Column(name = "target_gain_is_active", nullable = false)
	private Boolean targetGainIsActive;
	@Column(name = "maximum_loss_is_active", nullable = false)
	private Boolean maximumLossIsActive;

	private PortfolioNotificationPreference(boolean targetGainIsActive, boolean maximumLossIsActive) {
		this.targetGainIsActive = targetGainIsActive;
		this.maximumLossIsActive = maximumLossIsActive;
	}

	/**
	 * 알림 설정이 전부 활성화된 포트폴리오 알림 개인설정 객체를 반환한다.
	 *
	 * @return 포트폴리오 알림 개인 설정 객체
	 */
	public static PortfolioNotificationPreference allActive() {
		return new PortfolioNotificationPreference(true, true);
	}

	/**
	 * 알림 설정이 전부 비 활성화된 포트폴리오 알림 개인설정 객체를 반환한다.
	 *
	 * @return 포트폴리오 알림 개인 설정 객체
	 */
	public static PortfolioNotificationPreference allInactive() {
		return new PortfolioNotificationPreference(false, false);
	}

	/**
	 * 포트폴리오 목표수익금액 알림 활성화 여부를 설정한다.
	 *
	 * @param isActive 포트폴리오 목표수익금액 활성화 여부, true: 활성화, false: 비활성화
	 */
	public void changeTargetGain(Boolean isActive) {
		this.targetGainIsActive = isActive;
	}

	/**
	 * 포트폴리오 최대손실금액 알림 활성화 여부를 설정한다.
	 *
	 * @param isActive 포트폴리오 최대손실금액 활성화 여부, true: 활성화, false: 비활성화
	 */
	public void changeMaximumLoss(Boolean isActive) {
		this.maximumLossIsActive = isActive;
	}

	/**
	 * 포트폴리오 목표수익금액 알림 활성화 여부가 같은지 비교한다.
	 *
	 * @param active 포트폴리오 목표수익금액 알림 활성화 비교 값
	 * @return true: 일치, false: 비일치
	 */
	public boolean isSameTargetGain(boolean active) {
		return this.targetGainIsActive == active;
	}

	/**
	 * 포트폴리오 최대손실금액 알림 활성화 여부가 같은지 비교한다.
	 *
	 * @param active 포트폴리오 최대손실금액 알림 활성화 비교 값
	 * @return true: 일치, false: 비일치
	 */
	public boolean isSameMaxLoss(boolean active) {
		return this.maximumLossIsActive == active;
	}

	public Boolean targetGainIsActive() {
		return this.targetGainIsActive;
	}

	public Boolean maximumLossIsActive() {
		return this.maximumLossIsActive;
	}
}
