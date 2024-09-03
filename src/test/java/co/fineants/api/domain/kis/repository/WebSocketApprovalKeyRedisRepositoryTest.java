package co.fineants.api.domain.kis.repository;

import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.api.AbstractContainerBaseTest;

class WebSocketApprovalKeyRedisRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private WebSocketApprovalKeyRedisRepository repository;

	@DisplayName("웹소켓 접근 키를 저장한다")
	@Test
	void saveApprovalKey() {
		// given
		String approvalKey = "approvalKey";
		// when
		repository.saveApprovalKey(approvalKey);
		// then
		Assertions.assertThat(repository.fetchApprovalKey().orElseThrow()).isEqualTo("approvalKey");
	}

	@DisplayName("웹소켓 저장소에 유효하지 않은 값을 저장할 수 없다")
	@MethodSource(value = {"invalidApprovalKey"})
	@ParameterizedTest
	void saveApprovalKey_whenInvalidApprovalKey_thenNotSaveApprovalKey(String approvalKey) {
		// given

		// when
		repository.saveApprovalKey(approvalKey);
		// then
		Assertions.assertThat(repository.fetchApprovalKey()).isEmpty();
	}

	public static Stream<Arguments> invalidApprovalKey() {
		return Stream.of(
			Arguments.of(""),
			Arguments.of(" "),
			Arguments.of((Object)null)
		);
	}

	@DisplayName("웹소켓 접근키를 가져온다")
	@Test
	void fetchApprovalKey() {
		// given
		String approvalKey = "approvalKey";
		repository.saveApprovalKey(approvalKey);
		// when
		Optional<String> findApprovalKey = repository.fetchApprovalKey();
		// then
		Assertions.assertThat(findApprovalKey.orElseThrow()).isEqualTo("approvalKey");
	}
}
