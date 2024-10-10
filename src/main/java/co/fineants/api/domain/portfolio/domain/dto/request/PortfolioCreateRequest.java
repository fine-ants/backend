package co.fineants.api.domain.portfolio.domain.dto.request;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.valiator.MoneyNumberWithZero;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioDetail;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.domain.portfolio.properties.PortfolioProperties;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioArgumentException;
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

	/**
	 * 포트폴리오 엔티티 객체를 생성하여 반환한다
	 *
	 * @param member 포트폴리오를 소유한 회원 객체
	 * @param properties 증권사 목록을 포함하는 프로퍼티 객체
	 * @return 포트폴리오 객체
	 * @throws BadRequestException 포트폴리오의 상세 정보가 유효하지 않거나 금융 정보 조합이 유효하지 않으면 예외 발생
	 */
	public Portfolio toEntity(Member member, PortfolioProperties properties) {
		try {
			PortfolioDetail detail = PortfolioDetail.of(name, securitiesFirm, properties);
			PortfolioFinancial financial = PortfolioFinancial.of(budget, targetGain, maximumLoss);
			return Portfolio.allInActive(detail, financial, member);
		} catch (IllegalPortfolioArgumentException e) {
			throw new BadRequestException(e.getErrorCode(), e);
		}
	}
}
