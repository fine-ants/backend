package codesquad.fineants.spring.api.purchase_history.event;

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
public class SendableParameter {
	private Long portfolioId;
	private Long memberId;

	public static SendableParameter create(Long portfolioId, Long memberId) {
		return SendableParameter.builder()
			.portfolioId(portfolioId)
			.memberId(memberId)
			.build();
	}
}
