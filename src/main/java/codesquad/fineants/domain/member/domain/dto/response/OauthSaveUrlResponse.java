package codesquad.fineants.domain.member.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import codesquad.fineants.domain.member.domain.dto.request.AuthorizationRequest;
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
	@JsonProperty("authURL")
	private String authUrl;
	@JsonIgnore
	private AuthorizationRequest authorizationRequest;
}
