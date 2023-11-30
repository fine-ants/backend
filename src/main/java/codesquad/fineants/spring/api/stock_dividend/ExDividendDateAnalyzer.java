package codesquad.fineants.spring.api.stock_dividend;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExDividendDateAnalyzer {
    private final DividendFileReader dividendFileReader;
    private final HolidayFileReader holidayFileReader;

    public void analyze() throws IOException {
        List<DividendDto> dividendDtoList = dividendFileReader.read();
        List<HolidayDto> holidayDtoList = holidayFileReader.read();
        Set<LocalDate> holidays = holidayDtoList.stream()
                .map(HolidayDto::getDate)
                .collect(Collectors.toSet());

        for (DividendDto dividendDto : dividendDtoList) {
            LocalDate recordDate = dividendDto.getRecordDate();
            LocalDate previousDate = getOneBusinessDayBefore(recordDate, holidays);
            dividendDto.setExDividendDate(previousDate);
        }

        exportToTsv(dividendDtoList);
    }

    private LocalDate getOneBusinessDayBefore(LocalDate date, Set holidays) {
        LocalDate previousDay = date.minusDays(1);

        while (isHolidayOrWeekend(previousDay, holidays)) {
            previousDay = previousDay.minusDays(1);
        }

        return previousDay;
    }

    private boolean isHolidayOrWeekend(LocalDate date, Set holidays) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY || holidays.contains(date);
    }

    public void exportToTsv(List<DividendDto> dividendDtoList) throws IOException {
        Path path = Paths.get("./ex-dividend-date.tsv");
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            String header = String.join("\t",
                    "ex_dividend_date", "record_date", "payment_date", "ticker_symbol", "dividend_per_share");
            writer.write(header);
            writer.newLine();

            for (DividendDto dividendDto : dividendDtoList) {
                String line = formatDividendDtoForTsv(dividendDto);
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private String formatDividendDtoForTsv(DividendDto dividendDto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String exDividendDate = dividendDto.getExDividendDate().format(formatter);
        String recordDate = dividendDto.getRecordDate().format(formatter);
        String paymentDate = dividendDto.getPaymentDate() != null ?
                dividendDto.getPaymentDate().format(formatter) : "";
        String tickerSymbol = dividendDto.getTickerSymbol();
        String dividendPerShare = String.valueOf(dividendDto.getDividendPerShare());

        return String.join("\t", exDividendDate, recordDate, paymentDate, tickerSymbol, dividendPerShare);
    }
}
