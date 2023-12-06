package codesquad.fineants.spring.api.member.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.errors.errorcode.OauthErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.member.request.AuthorizationRequest;

@Component
public class AuthorizationRequestManager {
	private static final Map<String, AuthorizationRequest> store = new ConcurrentHashMap<>();

	public AuthorizationRequest pop(String state) {
		AuthorizationRequest request = store.remove(state);
		if (request == null) {
			throw new BadRequestException(OauthErrorCode.WRONG_STATE);
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
