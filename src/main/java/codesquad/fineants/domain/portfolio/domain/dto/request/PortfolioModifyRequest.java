package codesquad.fineants.domain.portfolio.domain.dto.request;

import javax.validation.constraints.NotBlank;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.valiator.MoneyNumberWithZero;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.portfolio.domain.entity.Portfolio;
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

	@MoneyNumberWithZero
	private Money budget;

	@MoneyNumberWithZero
	private Money targetGain;

	@MoneyNumberWithZero
	private Money maximumLoss;

	public Portfolio toEntity(Member member) {
		return Portfolio.noActive(name, securitiesFirm, budget, targetGain, maximumLoss, member);
	}
}