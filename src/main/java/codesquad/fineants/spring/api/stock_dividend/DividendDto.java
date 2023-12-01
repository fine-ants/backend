package codesquad.fineants.spring.api.stock_dividend;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class DividendDto {

    private LocalDate exDividendDate;
    private LocalDate recordDate;
    private LocalDate paymentDate;
    private String tickerSymbol;
    private float dividendPerShare;

    public DividendDto(LocalDate recordDate, LocalDate paymentDate, String tickerSymbol, float dividendPerShare){
        this.recordDate = recordDate;
        this.paymentDate = paymentDate;
        this.tickerSymbol = tickerSymbol;
        this.dividendPerShare = dividendPerShare;
    }
}
