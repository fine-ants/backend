package codesquad.fineants.domain.notification_preference;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private boolean browserNotify;

	private boolean targetGainNotify;

	private boolean maxLossNotify;

	private boolean targetPriceNotify;

	@Builder
	public NotificationPreference(Member member, boolean browser, boolean portfolioTargetGain, boolean portfolioMaxLoss,
		boolean stockTargetPrice) {
		this.member = member;
		this.browser = browser;
		this.portfolioTargetGain = portfolioTargetGain;
		this.portfolioMaxLoss = portfolioMaxLoss;
		this.stockTargetPrice = stockTargetPrice;
	}
}
