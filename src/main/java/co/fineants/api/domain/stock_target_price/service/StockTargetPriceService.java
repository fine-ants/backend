package co.fineants.api.domain.stock_target_price.service;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationCreateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.request.TargetPriceNotificationUpdateRequest;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationCreateResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSearchItem;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSearchResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSpecificItem;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationSpecifiedSearchResponse;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotificationUpdateResponse;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;
import co.fineants.api.domain.stock_target_price.repository.StockTargetPriceRepository;
import co.fineants.api.domain.stock_target_price.repository.TargetPriceNotificationRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.StockTargetPriceAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.StockErrorCode;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTargetPriceService {
	private static final int TARGET_PRICE_NOTIFICATION_LIMIT = 5;

	private final StockTargetPriceRepository repository;
	private final TargetPriceNotificationRepository targetPriceNotificationRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final ClosingPriceRepository manager;

	// 종목 지정가 및 지정가 알림 생성
	@Transactional
	@Secured("ROLE_USER")
	public TargetPriceNotificationCreateResponse createStockTargetPrice(
		TargetPriceNotificationCreateRequest request,
		Long memberId
	) {
		String tickerSymbol = request.getTickerSymbol();
		// 현재 지정가 알림 개수가 최대 갯수를 초과 했는지 검증
		verifyNumberOfLimitTargetPriceNotifications(tickerSymbol, memberId);
		// 지정가 알림이 이미 존재하는지 검증
		verifyExistTargetPriceNotification(tickerSymbol, request.getTargetPrice(), memberId);

		Member member = findMember(memberId);
		Stock stock = findStock(tickerSymbol);
		StockTargetPrice stockTargetPrice = saveStockTargetPrice(member, stock);
		TargetPriceNotification targetPriceNotification = targetPriceNotificationRepository.save(
			request.toEntity(stockTargetPrice));
		log.info("종목 지정가 알림 추가 결과 : stockTargetPrice={}, targetPriceNotification={}", stockTargetPrice,
			targetPriceNotification);
		return TargetPriceNotificationCreateResponse.from(stockTargetPrice, targetPriceNotification);
	}

	private void verifyNumberOfLimitTargetPriceNotifications(String tickerSymbol, Long memberId) {
		int size = (int)repository.findByTickerSymbolAndMemberId(tickerSymbol, memberId)
			.stream()
			.map(StockTargetPrice::getId)
			.mapToLong(id -> targetPriceNotificationRepository.findAllByStockTargetPriceId(id).size())
			.sum();
		if (size >= TARGET_PRICE_NOTIFICATION_LIMIT) {
			throw new BadRequestException(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_LIMIT);
		}
	}

	private StockTargetPrice saveStockTargetPrice(Member member, Stock stock) {
		StockTargetPrice stockTargetPrice = repository.findByTickerSymbolAndMemberId(stock.getTickerSymbol(),
				member.getId())
			.orElseGet(() -> StockTargetPrice.newStockTargetPriceWithActive(member, stock));
		return repository.save(stockTargetPrice);
	}

	private void verifyExistTargetPriceNotification(String tickerSymbol, Money targetPrice, Long memberId) {
		if (targetPriceNotificationRepository.findByTickerSymbolAndTargetPriceAndMemberId(
			tickerSymbol,
			targetPrice,
			memberId).isPresent()) {
			throw new BadRequestException(StockErrorCode.BAD_REQUEST_TARGET_PRICE_NOTIFICATION_EXIST);
		}
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private Stock findStock(String tickerSymbol) {
		return stockRepository.findByTickerSymbol(tickerSymbol)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK));
	}

	@Secured("ROLE_USER")
	public TargetPriceNotificationSearchResponse searchStockTargetPrices(Long memberId) {
		List<StockTargetPrice> stockTargetPrices = repository.findAllByMemberId(memberId);
		List<TargetPriceNotificationSearchItem> stocks = stockTargetPrices.stream()
			.map(stockTargetPrice -> TargetPriceNotificationSearchItem.from(stockTargetPrice, manager))
			.toList();
		return TargetPriceNotificationSearchResponse.from(stocks);
	}

	@Secured("ROLE_USER")
	public TargetPriceNotificationSpecifiedSearchResponse searchStockTargetPrice(
		String tickerSymbol,
		Long memberId
	) {
		List<TargetPriceNotificationSpecificItem> targetPrices = repository.findByTickerSymbolAndMemberIdUsingFetchJoin(
				tickerSymbol, memberId)
			.stream()
			.flatMap(stockTargetPrice -> stockTargetPrice.getTargetPriceNotifications().stream())
			.map(TargetPriceNotificationSpecificItem::from)
			.toList();
		TargetPriceNotificationSpecifiedSearchResponse response = TargetPriceNotificationSpecifiedSearchResponse.from(
			targetPrices
		);
		log.info("특정 종목의 지정가 알림들 조회 결과 : response={}", response);
		return response;
	}

	@Transactional
	@Secured("ROLE_USER")
	public TargetPriceNotificationUpdateResponse updateStockTargetPrice(
		TargetPriceNotificationUpdateRequest request, Long memberId) {
		StockTargetPrice stockTargetPrice = repository.findByTickerSymbolAndMemberId(request.getTickerSymbol(),
				memberId)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK_TARGET_PRICE));
		stockTargetPrice.changeIsActive(request.getIsActive());
		return TargetPriceNotificationUpdateResponse.from(stockTargetPrice);
	}

	// 종목 지정가 단일 제거
	@Transactional
	@Authorized(serviceClass = StockTargetPriceAuthorizedService.class)
	public void deleteStockTargetPrice(@ResourceId Long stockTargetPriceId) {
		StockTargetPrice stockTargetPrice = repository.findById(stockTargetPriceId)
			.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK_TARGET_PRICE));
		targetPriceNotificationRepository.deleteAllByStockTargetPrices(List.of(stockTargetPrice));
		repository.deleteById(stockTargetPriceId);
	}
}
