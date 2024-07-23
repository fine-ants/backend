package codesquad.fineants.domain.purchasehistory.event.aop;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PurchaseHistoryEventSendableParameter {
	private Long portfolioId;
	private Long memberId;

	public static PurchaseHistoryEventSendableParameter create(Long portfolioId, Long memberId) {
		return PurchaseHistoryEventSendableParameter.builder()
			.portfolioId(portfolioId)
			.memberId(memberId)
			.build();
	}
}
