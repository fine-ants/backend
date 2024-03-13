package codesquad.fineants.domain.notification;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
@Entity
public abstract class Notification extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private Boolean isRead;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "type")
	private NotificationType type;

	private String referenceId;

	private String messageId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public static Notification portfolioNotification(String portfolioName, String title, NotificationType type,
		String referenceId, String messageId, Member member) {
		if (type == NotificationType.PORTFOLIO_TARGET_GAIN
			|| type == NotificationType.PORTFOLIO_MAX_LOSS) {
			return PortfolioNotification.builder()
				.portfolioName(portfolioName)
				.title(title)
				.isRead(false)
				.type(type)
				.referenceId(referenceId)
				.messageId(messageId)
				.member(member)
				.build();
		}
		throw new IllegalStateException("잘못된 타입입니다. type=" + type);
	}

	public static Notification stockTargetPriceNotification(String stockName, Long targetPrice, String title,
		String referenceId, String messageId, Member member) {
		return StockTargetPriceNotification.builder()
			.stockName(stockName)
			.targetPrice(targetPrice)
			.title(title)
			.isRead(false)
			.type(NotificationType.STOCK_TARGET_PRICE)
			.referenceId(referenceId)
			.messageId(messageId)
			.member(member)
			.build();
	}

	// 알림을 읽음 처리
	public void readNotification() {
		this.isRead = true;
	}

	public abstract NotificationBody createNotificationBody();

	public abstract String createNotificationContent();
}
