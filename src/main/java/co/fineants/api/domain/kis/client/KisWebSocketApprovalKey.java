package co.fineants.api.domain.kis.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KisWebSocketApprovalKey {
	@JsonProperty("approval_key")
	private String approvalKey;

	public static KisWebSocketApprovalKey create(String approvalKey) {
		return new KisWebSocketApprovalKey(approvalKey);
	}
}
