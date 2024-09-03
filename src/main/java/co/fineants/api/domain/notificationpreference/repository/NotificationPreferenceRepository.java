package co.fineants.api.domain.notificationpreference.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

	@Query("select n from NotificationPreference n where n.member.id = :memberId")
	Optional<NotificationPreference> findByMemberId(@Param("memberId") Long memberId);

	@Query("select n from NotificationPreference n where n.member.id in :memberIds")
	List<NotificationPreference> findAllByMemberIds(@Param("memberIds") List<Long> memberIds);
}
