package codesquad.fineants.spring.api.member.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
}
