package co.fineants.api.domain.stock_target_price.repository;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;

class StockTargetPriceRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockTargetPriceRepository stockTargetPriceRepository;

	@DisplayName("종목 지정가의 모든 티커 심볼을 조회한다")
	@Test
	void findAllTickerSymbol() {
		// given
		Member member = memberRepository.save(createMember());
		Stock samsung = stockRepository.save(createSamsungStock());
		Stock kakao = stockRepository.save(createKakaoStock());
		stockTargetPriceRepository.save(createStockTargetPrice(member, samsung));
		stockTargetPriceRepository.save(createStockTargetPrice(member, kakao));

		member = memberRepository.save(createMember("ant1234"));
		Stock dongwha = stockRepository.save(createDongwhaPharmStock());
		stockTargetPriceRepository.save(createStockTargetPrice(member, samsung));
		stockTargetPriceRepository.save(createStockTargetPrice(member, kakao));
		stockTargetPriceRepository.save(createStockTargetPrice(member, dongwha));

		// when
		List<String> actual = stockTargetPriceRepository.findAllTickerSymbol();
		// then
		Assertions.assertThat(actual)
			.hasSize(3)
			.containsExactly("000020", "005930", "035720");
	}
}
