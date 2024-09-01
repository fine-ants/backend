package co.fineants.api.domain.watchlist.service;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.watchlist.domain.dto.request.ChangeWatchListNameRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchListRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchStockRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.DeleteWatchStocksRequest;
import co.fineants.api.domain.watchlist.domain.dto.response.CreateWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListsResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import co.fineants.api.domain.watchlist.domain.entity.WatchList;
import co.fineants.api.domain.watchlist.domain.entity.WatchStock;
import co.fineants.api.domain.watchlist.event.publisher.WatchStockEventPublisher;
import co.fineants.api.domain.watchlist.repository.WatchListRepository;
import co.fineants.api.domain.watchlist.repository.WatchStockRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.WatchListAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.common.resource.ResourceIds;
import co.fineants.api.global.errors.errorcode.MemberErrorCode;
import co.fineants.api.global.errors.errorcode.StockErrorCode;
import co.fineants.api.global.errors.errorcode.WatchListErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;
import co.fineants.api.global.errors.exception.ForBiddenException;
import co.fineants.api.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WatchListService {

	private final WatchListRepository watchListRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final WatchStockRepository watchStockRepository;
	private final CurrentPriceRedisRepository currentPriceRedisRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final WatchStockEventPublisher watchStockEventPublisher;

	@Transactional
	@Secured("ROLE_USER")
	public CreateWatchListResponse createWatchList(Long memberId, CreateWatchListRequest request) {
		Member member = findMember(memberId);
		WatchList watchList = WatchList.newWatchList(request.getName(), member);
		watchList = watchListRepository.save(watchList);
		return CreateWatchListResponse.create(watchList.getId());
	}

	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	public List<ReadWatchListsResponse> readWatchLists(Long memberId) {
		Member member = findMember(memberId);
		return watchListRepository.findByMember(member).stream()
			.map(ReadWatchListsResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	@Authorized(serviceClass = WatchListAuthorizedService.class)
	@Secured("ROLE_USER")
	public ReadWatchListResponse readWatchList(Long memberId, @ResourceId Long watchListId) {
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(watchList, memberId);

		List<WatchStock> watchStocks = watchStockRepository.findWithStockAndDividendsByWatchList(watchList);

		List<ReadWatchListResponse.WatchStockResponse> watchStockResponses = watchStocks.stream()
			.map(watchStock -> ReadWatchListResponse.from(watchStock, currentPriceRedisRepository,
				closingPriceRepository))
			.toList();
		return new ReadWatchListResponse(watchList.getName(), watchStockResponses);
	}

	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	public List<WatchListHasStockResponse> hasStock(Long memberId, String tickerSymbol) {
		Member member = findMember(memberId);
		return watchListRepository.findWatchListsAndStockPresenceByMemberAndTickerSymbol(member, tickerSymbol);
	}

	@Transactional
	@Authorized(serviceClass = WatchListAuthorizedService.class)
	@Secured("ROLE_USER")
	public void changeWatchListName(Long memberId, @ResourceId Long watchListId, ChangeWatchListNameRequest request) {
		Member member = findMember(memberId);
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, member.getId());

		watchList.changeName(request.getName());
	}

	@Transactional
	@Authorized(serviceClass = WatchListAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deleteWatchLists(Long memberId, @ResourceIds List<Long> watchlistIds) {
		List<WatchList> watchLists = watchListRepository.findAllById(watchlistIds);

		watchLists.forEach(watchList -> {
			validateWatchListAuthorization(watchList, memberId);
			watchListRepository.deleteById(watchList.getId());
		});
	}

	@Transactional
	@Authorized(serviceClass = WatchListAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deleteWatchList(Long memberId, @ResourceId Long watchlistId) {
		WatchList watchList = watchListRepository.findById(watchlistId)
			.orElseThrow(() -> new FineAntsException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, memberId);
		watchListRepository.deleteById(watchlistId);
	}

	@Transactional
	@Authorized(serviceClass = WatchListAuthorizedService.class)
	@Secured("ROLE_USER")
	public void createWatchStocks(Long memberId, @ResourceId Long watchListId, CreateWatchStockRequest request) {
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(watchList, memberId);
		validateAlreadyExistWatchStocks(watchList, request.getTickerSymbols());

		request.getTickerSymbols().stream()
			.map(tickerSymbol -> stockRepository.findByTickerSymbol(tickerSymbol)
				.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK)))
			.map(stock -> WatchStock.newWatchStock(watchList, stock))
			.forEach(watchStockRepository::save);

		request.getTickerSymbols()
			.forEach(watchStockEventPublisher::publishWatchStock);
	}

	private void validateAlreadyExistWatchStocks(WatchList watchList, List<String> tickerSymbols) {
		List<WatchStock> watchStocks = watchStockRepository.findByWatchListAndStock_TickerSymbolIn(watchList.getId(),
			tickerSymbols);
		if (!watchStocks.isEmpty()) {
			throw new ForBiddenException(WatchListErrorCode.ALREADY_WATCH_STOCK);
		}
	}

	@Transactional
	@Authorized(serviceClass = WatchListAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deleteWatchStocks(Long memberId, @ResourceId Long watchListId, DeleteWatchStocksRequest request) {
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, memberId);

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, request.getTickerSymbols());
	}

	@Transactional
	@Authorized(serviceClass = WatchListAuthorizedService.class)
	@Secured("ROLE_USER")
	public void deleteWatchStock(Long memberId, @ResourceId Long watchListId, String tickerSymbol) {
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, memberId);

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, List.of(tickerSymbol));
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private void validateWatchListAuthorization(WatchList watchList, Long memberId) {
		if (!watchList.hasAuthorization(memberId)) {
			throw new FineAntsException(WatchListErrorCode.FORBIDDEN_WATCHLIST);
		}
	}
}
