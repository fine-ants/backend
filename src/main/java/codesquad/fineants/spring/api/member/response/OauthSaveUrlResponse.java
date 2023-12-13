package codesquad.fineants.spring.api.member.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OauthSaveUrlResponse {
	private String authURL;
	@JsonIgnore
	private AuthorizationRequest authorizationRequest;
}
