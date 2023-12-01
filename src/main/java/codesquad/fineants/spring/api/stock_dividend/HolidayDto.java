package codesquad.fineants.spring.api.stock_dividend;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class HolidayDto {

    private LocalDate date;
    private String note;
}
