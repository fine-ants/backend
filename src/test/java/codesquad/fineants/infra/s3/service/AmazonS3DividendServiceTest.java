package codesquad.fineants.infra.s3.service;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.AbstractContainerBaseTest;
import codesquad.fineants.domain.dividend.domain.entity.StockDividend;

class AmazonS3DividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private AmazonS3DividendService service;

	@DisplayName("배당금 데이터를 읽어온다")
	@Test
	void fetchDividend() {
		// given

		// when
		List<StockDividend> dividends = service.fetchDividends();
		// then
		Assertions.assertThat(dividends)
			.hasSize(3271);
	}

	@DisplayName("dividends.csv 파일에 배당 일정을 작성한다")
	@Test
	void writeDividend() {
		// given
		List<StockDividend> dividends = Collections.emptyList();
		// when
		service.writeDividends(dividends);

		// then
		Assertions.assertThat(service.fetchDividends())
			.asList()
			.isEmpty();
	}
}
