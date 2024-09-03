package co.fineants.api.domain.kis.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

}
