package codesquad.fineants.domain.notification_preference.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.notification_preference.domain.entity.NotificationPreference;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

	@Query("select n from NotificationPreference n where n.member.id = :memberId")
	Optional<NotificationPreference> findByMemberId(@Param("memberId") Long memberId);

	@Modifying
	@Query("delete from NotificationPreference n where n.member.id = :memberId")
	int deleteAllByMemberId(@Param("memberId") Long memberId);
}
