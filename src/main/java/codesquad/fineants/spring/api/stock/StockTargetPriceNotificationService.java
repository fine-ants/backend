package codesquad.fineants.spring.api.stock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.stock.Stock;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.stock_target_price.StockTargetPrice;
import codesquad.fineants.domain.stock_target_price.StockTargetPriceRepository;
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.exception.BadRequestException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.stock.request.TargetPriceNotificationCreateRequest;
import codesquad.fineants.spring.api.stock.response.TargetPriceNotificationCreateResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTargetPriceNotificationService {

	private static final int TARGET_PRICE_NOTIFICATION_LIMIT = 5;

	private final StockTargetPriceRepository repository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;

	@Transactional
	public TargetPriceNotificationCreateResponse createStockTargetPriceNotification(
		String tickerSymbol,
		TargetPriceNotificationCreateRequest request,
		Long memberId
	) {
		// 현재 종목 지정가 알림 개수가 최대 갯수를 초과 했는지 검증
		verifyNumberOfLimitNotifications(tickerSymbol);
		// 종목에 따른 지정가가 이미 존재하는지 검증
		verifyExistTargetPrice(tickerSymbol, request.getTargetPrice());

		Member member = findMember(memberId);
		Stock stock = findStock(tickerSymbol);
		StockTargetPrice stockTargetPrice = repository.save(request.toEntity(member, stock));
		return TargetPriceNotificationCreateResponse.from(stockTargetPrice);
	}

	private Stock findStock(String tickerSymbol) {
		return stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private void verifyNumberOfLimitNotifications(String tickerSymbol) {
		if (repository.findAllByTickerSymbol(tickerSymbol).size() >= TARGET_PRICE_NOTIFICATION_LIMIT) {
			throw new BadRequestException(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_LIMIT);
		}
	}

	private void verifyExistTargetPrice(String tickerSymbol, Long targetPrice) {
		if (repository.findByTickerSymbolAndTargetPrice(tickerSymbol, targetPrice).isPresent()) {
			throw new BadRequestException(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_EXIST);
		}
	}
}
