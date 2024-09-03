package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class WebSocketApprovalKeyRedisRepository {
	private static final String APPROVAL_KEY_FORMAT = "websocket-approval-key";
	private final RedisTemplate<String, String> redisTemplate;

	public void saveApprovalKey(String approvalKey) {
		if (Strings.isBlank(approvalKey)) {
			return;
		}
		log.info("Save Approval Key : {}", approvalKey);
		redisTemplate.opsForValue().set(APPROVAL_KEY_FORMAT, approvalKey);
	}

	public Optional<String> fetchApprovalKey() {
		return Optional.ofNullable(redisTemplate.opsForValue().get(APPROVAL_KEY_FORMAT));
	}
}
