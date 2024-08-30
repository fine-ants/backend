package co.fineants.api.domain.kis.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OauthKisApprovalResponse {
	@JsonProperty("approval_key")
	private String approvalKey;
}
