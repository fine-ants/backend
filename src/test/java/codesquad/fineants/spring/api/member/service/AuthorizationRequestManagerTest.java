package codesquad.fineants.spring.api.member.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import codesquad.fineants.spring.api.member.manager.AuthorizationRequestManager;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;

class AuthorizationRequestManagerTest {

	@DisplayName("만료된 인가코드 요청들을 제거한다")
	@Test
	void cleanUpExpirationRequests() {
		// given
		AuthorizationRequestManager manager = new AuthorizationRequestManager();
		manager.add("1111", AuthorizationRequest.of("1111", "codeVerifier", "codeChallenge", "nonce",
			System.currentTimeMillis()));
		manager.add("1234", AuthorizationRequest.of("1234", "codeVerifier", "codeChallenge", "nonce",
			System.currentTimeMillis() - (60 * 2 * 1000L)));
		// when
		manager.cleanUpExpirationRequests();
		// then
		int size = manager.size();
		Assertions.assertThat(size).isEqualTo(1);
	}
}
