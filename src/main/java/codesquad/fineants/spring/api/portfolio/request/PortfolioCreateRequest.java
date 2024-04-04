package codesquad.fineants.spring.api.portfolio.request;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.valiator.MoneyNumber;
import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.portfolio.Portfolio;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioCreateRequest {
	@NotBlank(message = "포트폴리오 이름은 필수 정보입니다")
	private String name;

	@NotBlank(message = "증권사는 필수 정보입니다")
	private String securitiesFirm;

	@MoneyNumber
	private Money budget;

	@MoneyNumber
	private Money targetGain;

	@MoneyNumber
	private Money maximumLoss;

	public Portfolio toEntity(Member member) {
		return Portfolio.builder()
			.name(name)
			.securitiesFirm(securitiesFirm)
			.budget(budget)
			.targetGain(targetGain)
			.maximumLoss(maximumLoss)
			.targetGainIsActive(false)
			.maximumLossIsActive(false)
			.member(member)
			.build();
	}
}
