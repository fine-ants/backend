package co.fineants.api.domain.dividend.domain.calculator;

import java.time.LocalDate;

public interface ExDividendDateCalculator {
	LocalDate calculate(LocalDate recordDate);
}
