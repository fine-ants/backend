package co.fineants.api.global.errors.exception;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.portfolio.domain.entity.PortfolioFinancial;
import co.fineants.api.global.errors.errorcode.PortfolioErrorCode;
import co.fineants.api.global.errors.exception.portfolio.IllegalPortfolioFinancialArgumentException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class FineAntsExceptionTest extends AbstractContainerBaseTest {

	@DisplayName("예외가 발생하면 에러코드의 메시지를 저장한다")
	@Test
	void testThrowFineAntsException() {
		// given

		// when
		Throwable throwable = Assertions.catchThrowable(
			() -> {
				throw new FineAntsException(PortfolioErrorCode.NOT_FOUND_PORTFOLIO);
			});
		// then
		log.error("fail find portfolio", throwable);
		throwable.printStackTrace(System.out);
		Assertions.assertThat(throwable)
			.isInstanceOf(FineAntsException.class);
	}

	@DisplayName("예외 연쇄를 테스트한다")
	@Test
	void testThrowBadRequestException() {
		// given
		Money budget = Money.won(1_000_000);
		Money targetGain = Money.won(900_000);
		Money maximumLoss = Money.won(900_000);
		// when
		Throwable throwable = Assertions.catchThrowable(() -> {
			try {
				PortfolioFinancial.of(budget, targetGain, maximumLoss);
			} catch (IllegalPortfolioFinancialArgumentException e) {
				throw new BadRequestException(e.getErrorCode(), e);
			}
		});
		// then
		log.error("not create PortfolioFinancial Instance", throwable);
		Assertions.assertThat(throwable)
			.isInstanceOf(BadRequestException.class);
	}

}
