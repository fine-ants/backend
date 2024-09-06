package co.fineants.api.domain.kis.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import reactor.core.publisher.Mono;

class CurrentPriceRedisRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@MockBean
	private KisClient kisClient;

	@DisplayName("저장소에 종목의 현재가가 없으면 한국투자증권에서 조회한 다음에 저장후 값을 반환한다")
	@Test
	void fetchPriceBy_whenNotStorePrice_thenFetchPriceFromKis() {
		// given
		String ticker = "005930";
		BDDMockito.given(kisClient.fetchCurrentPrice(ticker))
			.willReturn(Mono.just(KisCurrentPrice.create(ticker, 50000L)));
		// when
		Money money = currentPriceRedisRepository.fetchPriceBy(ticker).orElseThrow();
		// then
		Assertions.assertThat(money).isEqualTo(Money.won(50000));
	}
}
