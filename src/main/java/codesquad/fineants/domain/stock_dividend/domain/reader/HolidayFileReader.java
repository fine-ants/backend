package codesquad.fineants.domain.stock_dividend.domain.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import codesquad.fineants.domain.stock_dividend.domain.dto.response.HolidayDto;

@Component
public class HolidayFileReader {

	private static final String FILE_NAME = "holidays.tsv";

	public List<HolidayDto> read() throws IOException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		Resource resource = new ClassPathResource(FILE_NAME);
		try (InputStream inputStream = resource.getInputStream();
			 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

			return reader.lines()
				.skip(1)
				.map(line -> line.split("\t"))
				.map(parts -> new HolidayDto(
					LocalDate.parse(parts[0], formatter),
					parts[1]))
				.collect(Collectors.toList());
		}
	}
}
