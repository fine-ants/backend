package co.fineants.api.domain.portfolio.domain.dto.request;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.valiator.MoneyNumberWithZero;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioCreateRequest {
	@NotBlank(message = "포트폴리오 이름은 필수 정보입니다")
	@Pattern(regexp = PortfolioDetail.NAME_REGEXP, message = "유효하지 않은 포트폴리오 이름입니다.")
	private String name;

	@NotBlank(message = "증권사는 필수 정보입니다")
	private String securitiesFirm;

	@MoneyNumberWithZero
	private Money budget;

	@MoneyNumberWithZero
	private Money targetGain;

	@MoneyNumberWithZero
	private Money maximumLoss;

	public static PortfolioCreateRequest create(String name, String securitiesFirm, Money budget, Money targetGain,
		Money maximumLoss) {
		return new PortfolioCreateRequest(name, securitiesFirm, budget, targetGain, maximumLoss);
	}

	public Portfolio toEntity(Member member) {
		return Portfolio.noActive(name, securitiesFirm, budget, targetGain, maximumLoss, member);
	}
}
