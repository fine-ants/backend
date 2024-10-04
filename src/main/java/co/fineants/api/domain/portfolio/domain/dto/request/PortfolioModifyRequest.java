package co.fineants.api.domain.portfolio.domain.dto.request;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.valiator.MoneyNumberWithZero;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioFinancialArgumentException;
import jakarta.validation.constraints.NotBlank;
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

	public Portfolio toEntity(Member member, PortfolioProperties properties) {
		PortfolioDetail detail = PortfolioDetail.of(name, securitiesFirm, properties);
		try {
			return Portfolio.noActive(detail, budget, targetGain, maximumLoss, member);
		} catch (IllegalPortfolioFinancialArgumentException e) {
			throw new BadRequestException(e.getErrorCode(), e.getErrorCode().getMessage());
		}
	}
}
