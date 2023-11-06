package codesquad.fineants.spring.api.kis.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OauthKisApprovalResponse {
	@JsonProperty("approval_key")
	private String approvalKey;
}
