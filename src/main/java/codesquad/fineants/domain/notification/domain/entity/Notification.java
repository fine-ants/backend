package codesquad.fineants.domain.notification.domain.entity;

import java.time.LocalDateTime;

import codesquad.fineants.domain.BaseEntity;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.notification.domain.entity.type.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
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

	private String link;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;
	
	Notification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, String title,
		Boolean isRead, NotificationType type, String referenceId, String link, Member member) {
		super(createAt, modifiedAt);
		this.id = id;
		this.title = title;
		this.isRead = isRead;
		this.type = type;
		this.referenceId = referenceId;
		this.link = link;
		this.member = member;
	}

	public static Notification portfolio(String portfolioName, String title, NotificationType type,
		String referenceId, String link, Member member) {
		if (type == NotificationType.PORTFOLIO_TARGET_GAIN
			|| type == NotificationType.PORTFOLIO_MAX_LOSS) {
			return PortfolioNotification.newNotification(title, type, referenceId, link, portfolioName, member);
		}
		throw new IllegalArgumentException("잘못된 타입입니다. type=" + type);
	}

	public static Notification stock(String stockName, Money targetPrice, String title,
		String referenceId, String link, Long targetPriceNotificationId, Member member) {
		return StockTargetPriceNotification.newNotification(stockName, targetPrice, title, referenceId,
			link, targetPriceNotificationId, member);
	}

	// 알림을 읽음 처리
	public void read() {
		this.isRead = true;
	}

	public abstract NotificationBody getBody();

	public abstract String getContent();

	public abstract String getName();
}
