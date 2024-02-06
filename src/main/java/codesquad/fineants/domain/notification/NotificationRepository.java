package codesquad.fineants.domain.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	@Query("select n from Notification n where n.member.id = :memberId order by n.createAt desc")
	List<Notification> findAllByMemberId(@Param("memberId") Long memberId);

	@Query("select n from Notification n where n.member.id = :memberId and n.id in (:notificationIds)")
	List<Notification> findAllByMemberIdAndIds(
		@Param("memberId") Long memberId,
		@Param("notificationIds") List<Long> notificationIds
	);
}
