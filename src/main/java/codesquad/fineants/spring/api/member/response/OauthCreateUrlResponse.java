package codesquad.fineants.spring.api.member.response;

import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OauthCreateUrlResponse {
	private String authURL;
	private AuthorizationRequest authorizationRequest;
}
