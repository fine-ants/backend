package codesquad.fineants.spring.api.member.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import codesquad.fineants.spring.api.member.request.AuthorizationRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@ToString
public class OauthSaveUrlResponse {
	private String authURL;
	@JsonIgnore
	private AuthorizationRequest authorizationRequest;
}
