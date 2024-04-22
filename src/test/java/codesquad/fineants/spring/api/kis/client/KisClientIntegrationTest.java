package codesquad.fineants.spring.api.kis.client;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import codesquad.fineants.spring.AbstractContainerBaseTest;
import codesquad.fineants.spring.api.kis.response.KisDividend;

public class KisClientIntegrationTest extends AbstractContainerBaseTest {

	@Autowired
	private KisClient kisClient;

	@DisplayName("사용자는 배당 일정을 조회한다")
	@Test
	void fetchDividendAll() {
		// given
		LocalDate from = LocalDate.now();
		LocalDate to = from.with(TemporalAdjusters.lastDayOfYear());
		KisAccessToken token = kisClient.fetchAccessToken().block(Duration.ofSeconds(5L));

		// when
		List<KisDividend> kisDividends = kisClient.fetchDividendAll(from, to,
			Objects.requireNonNull(token).createAuthorization());

		// then
		assertThat(kisDividends).isNotNull();
	}
}
