package codesquad.fineants.domain.kis.repository;

import java.util.Optional;

import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.kis.client.KisCurrentPrice;

public interface PriceRepository {
	void savePrice(KisCurrentPrice... currentPrices);

	Optional<Money> fetchPriceBy(String tickerSymbol);
}
