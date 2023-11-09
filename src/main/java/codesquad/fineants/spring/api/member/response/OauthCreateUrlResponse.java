package codesquad.fineants.spring.api.member.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OauthCreateUrlResponse {
	private String authURL;
	@JsonIgnore
	private String state;
	@JsonIgnore
	private String codeVerifier;
}
