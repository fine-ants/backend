package co.fineants.api.domain.notification.domain.entity;

import java.time.LocalDateTime;
import java.util.List;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.domain.entity.type.NotificationType;
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
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@Entity
@SuperBuilder(toBuilder = true)
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

	@Transient
	private List<String> messageIds;

	Notification(Long id, String title, Boolean isRead, NotificationType type, String referenceId, String link,
		Member member, List<String> messageIds) {
		super(LocalDateTime.now(), LocalDateTime.now());
		this.id = id;
		this.title = title;
		this.isRead = isRead;
		this.type = type;
		this.referenceId = referenceId;
		this.link = link;
		this.member = member;
		this.messageIds = messageIds;
	}

	public static Notification portfolio(String portfolioName, String title, NotificationType type,
		String referenceId, String link, Long portfolioId, Member member, List<String> messageIds) {
		if (type == NotificationType.PORTFOLIO_TARGET_GAIN
			|| type == NotificationType.PORTFOLIO_MAX_LOSS) {
			return PortfolioNotification.newNotification(title, type, referenceId, link, member, messageIds,
				portfolioName, portfolioId
			);
		}
		throw new IllegalArgumentException("잘못된 타입입니다. type=" + type);
	}

	public static Notification stockTargetPriceNotification(
		String title,
		String referenceId,
		String link,
		Member member,
		List<String> messageIds,
		String stockName,
		Money targetPrice,
		Long targetPriceNotificationId) {
		return StockTargetPriceNotification.newNotification(
			title,
			referenceId,
			link,
			member,
			messageIds,
			stockName,
			targetPrice,
			targetPriceNotificationId);
	}

	// 알림을 읽음 처리
	public void read() {
		this.isRead = true;
	}

	public boolean hasAuthorization(Long memberId) {
		return member.hasAuthorization(memberId);
	}

	public String formatted(String format) {
		return format.formatted(getIdToSentHistory());
	}

	public abstract Notification withId(Long id);

	public abstract NotificationBody getBody();

	public abstract String getContent();

	public abstract String getName();

	public abstract Long getIdToSentHistory();
}
