package codesquad.fineants.domain.fcm_token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmRepository extends JpaRepository<FcmToken, Long> {
}
