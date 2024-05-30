package codesquad.fineants.domain.fcm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codesquad.fineants.domain.fcm.domain.entity.FcmToken;

public interface FcmRepository extends JpaRepository<FcmToken, Long> {
	@Query("select f from FcmToken f where f.member.id = :memberId")
	List<FcmToken> findAllByMemberId(@Param("memberId") Long memberId);

	@Query("select f from FcmToken f join fetch f.member m where m.id in (:memberIds)")
	List<FcmToken> findAllByMemberIds(@Param("memberIds") List<Long> memberIds);

	@Query("select f from FcmToken f where f.token = :token and f.member.id = :memberId")
	Optional<FcmToken> findByTokenAndMemberId(@Param("token") String token, @Param("memberId") Long memberId);

	@Modifying
	@Query("delete from FcmToken f where f.token in (:tokens)")
	int deleteAllByTokens(@Param("tokens") List<String> tokens);

	@Modifying
	@Query("delete from FcmToken f where f.id = :fcmTokenId and f.member.id = :memberId")
	int deleteByFcmTokenId(@Param("fcmTokenId") Long fcmTokenId, @Param("memberId") Long memberId);

	@Modifying
	@Query("delete from FcmToken f where f.token = :token")
	int deleteAllByToken(@Param("token") String token);

	@Modifying
	@Query("delete from FcmToken f where f.member.id = :memberId")
	int deleteAllByMemberId(@Param("memberId") Long memberId);
}
