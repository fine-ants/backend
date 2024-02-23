package codesquad.fineants.spring.api.purchase_history.response;

import codesquad.fineants.domain.purchase_history.PurchaseHistory;
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
public class PurchaseHistoryDeleteResponse {
	private Long id;
	private Long portfolioId;
	private Long memberId;

	public static PurchaseHistoryDeleteResponse from(PurchaseHistory history, Long portfolioId, Long memberId) {
		return PurchaseHistoryDeleteResponse.builder()
			.id(history.getId())
			.portfolioId(portfolioId)
			.memberId(memberId)
			.build();
	}
}
