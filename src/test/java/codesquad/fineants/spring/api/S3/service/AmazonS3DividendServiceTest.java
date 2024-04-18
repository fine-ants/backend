package codesquad.fineants.spring.api.S3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.S3.dto.Dividend;

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
		Assertions.assertThat(dividends).isNotEmpty();
	}
}
