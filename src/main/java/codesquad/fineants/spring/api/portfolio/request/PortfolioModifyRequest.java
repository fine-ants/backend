package codesquad.fineants.spring.api.portfolio.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioModifyRequest {
	@NotBlank(message = "포트폴리오 이름은 필수 정보입니다")
	private String name;

	@NotBlank(message = "증권사는 필수 정보입니다")
	private String securitiesFirm;

	@NotNull(message = "예산은 필수 정보입니다.")
	@Positive(message = "예산은 양수여야 합니다")
	private Long budget;

	@NotNull(message = "목표 수익 금액은 필수 정보입니다.")
	@Positive(message = "목표 수익 금액은 양수여야 합니다")
	private Long targetGain;

	@NotNull(message = "최대 손실 금액은 필수 정보입니다.")
	@Positive(message = "최대 손실 금액은 양수여야 합니다")
	private Long maximumLoss;

	public Portfolio toEntity(Member member) {
		return Portfolio.builder()
			.name(name)
			.securitiesFirm(securitiesFirm)
			.budget(budget)
			.targetGain(targetGain)
			.maximumLoss(maximumLoss)
			.member(member)
			.build();
	}
}
