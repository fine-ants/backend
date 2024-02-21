package codesquad.fineants.domain.notification;

import java.time.LocalDateTime;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
@Entity
public abstract class Notification extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private Boolean isRead;
	private String type;
	private String referenceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public Notification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id, String title, Boolean isRead,
		String type, String referenceId, Member member) {
		super(createAt, modifiedAt);
		this.id = id;
		this.title = title;
		this.isRead = isRead;
		this.type = type;
		this.referenceId = referenceId;
		this.member = member;
	}

	// 알림을 읽음 처리
	public void readNotification() {
		this.isRead = true;
	}

	public abstract NotificationBody createNotificationBody();

	public abstract String createNotificationContent();
}
