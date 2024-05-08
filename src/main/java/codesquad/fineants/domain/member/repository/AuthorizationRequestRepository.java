package codesquad.fineants.domain.member.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.member.domain.dto.request.AuthorizationRequest;
import codesquad.fineants.global.errors.errorcode.OauthErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;

@Component
public class AuthorizationRequestRepository {
	private final Map<String, AuthorizationRequest> store;

	public AuthorizationRequestRepository() {
		store = new ConcurrentHashMap<>();
	}

	public AuthorizationRequest pop(String state) {
		AuthorizationRequest request = store.remove(state);
		if (request == null) {
			throw new FineAntsException(OauthErrorCode.WRONG_STATE);
		}
		return request;
	}

	public void add(String state, AuthorizationRequest request) {
		store.put(state, request);
	}

	public int size() {
		return store.size();
	}

	@Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES)
	public void cleanUpExpirationRequests() {
		long currentTime = System.currentTimeMillis();
		List<AuthorizationRequest> requests = new ArrayList<>(store.values());
		for (AuthorizationRequest request : requests) {
			if (request.isExpiration(currentTime)) {
				store.remove(request.getState());
			}
		}
	}
}
