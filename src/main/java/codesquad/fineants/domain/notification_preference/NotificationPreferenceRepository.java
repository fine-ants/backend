package codesquad.fineants.domain.notification_preference;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

	@Query("select n from NotificationPreference n where n.member.id = :memberId")
	Optional<NotificationPreference> findByMemberId(@Param("memberId") Long memberId);
}
