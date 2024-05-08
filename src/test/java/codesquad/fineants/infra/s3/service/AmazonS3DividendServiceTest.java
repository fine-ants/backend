package codesquad.fineants.infra.s3.service;

import static codesquad.fineants.infra.s3.service.AmazonS3DividendService.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import codesquad.fineants.infra.s3.dto.Dividend;
import codesquad.fineants.AbstractContainerBaseTest;

class AmazonS3DividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private AmazonS3DividendService service;

	@DisplayName("배당금 데이터를 읽어온다")
	@Test
	void fetchDividend() {
		// given

		// when
		List<Dividend> dividends = service.fetchDividend();
		// then
		Assertions.assertThat(dividends)
			.hasSize(3271);
	}

	@DisplayName("dividends.csv 파일에 배당 일정을 작성한다")
	@Test
	void writeDividend() throws IOException {
		// given
		BufferedReader br = new BufferedReader(
			new InputStreamReader(new ClassPathResource("dividends.csv").getInputStream()));
		List<Dividend> dividends = br.lines()
			.skip(1) // skip title
			.map(line -> line.split(CSV_SEPARATOR))
			.map(Dividend::parse)
			.distinct()
			.collect(Collectors.toList());

		// when
		service.writeDividend(dividends);

		// then
		List<Dividend> findDividends = service.fetchDividend();
		Assertions.assertThat(findDividends)
			.hasSize(3271)
			.containsAll(dividends);
	}
}
