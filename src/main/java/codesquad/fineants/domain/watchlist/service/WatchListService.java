package codesquad.fineants.domain.watchlist.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.watchlist.domain.dto.request.ChangeWatchListNameRequest;
import codesquad.fineants.domain.watchlist.domain.dto.request.CreateWatchListRequest;
import codesquad.fineants.domain.watchlist.domain.dto.request.CreateWatchStockRequest;
import codesquad.fineants.domain.watchlist.domain.dto.request.DeleteWatchListsRequests;
import codesquad.fineants.domain.watchlist.domain.dto.request.DeleteWatchStocksRequest;
import codesquad.fineants.domain.watchlist.domain.dto.response.CreateWatchListResponse;
import codesquad.fineants.domain.watchlist.domain.dto.response.ReadWatchListResponse;
import codesquad.fineants.domain.watchlist.domain.dto.response.ReadWatchListsResponse;
import codesquad.fineants.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import codesquad.fineants.domain.watchlist.domain.entity.WatchList;
import codesquad.fineants.domain.watchlist.domain.entity.WatchStock;
import codesquad.fineants.domain.watchlist.event.publisher.WatchStockEventPublisher;
import codesquad.fineants.domain.watchlist.repository.WatchListRepository;
import codesquad.fineants.domain.watchlist.repository.WatchStockRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.errorcode.WatchListErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.ForBiddenException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WatchListService {

	private final WatchListRepository watchListRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final WatchStockRepository watchStockRepository;
	private final CurrentPriceRepository currentPriceRepository;
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
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	public ReadWatchListResponse readWatchList(Long memberId, Long watchListId) {
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(watchList, memberId);

		List<WatchStock> watchStocks = watchStockRepository.findWithStockAndDividendsByWatchList(watchList);

		List<ReadWatchListResponse.WatchStockResponse> watchStockResponses = watchStocks.stream()
			.map(watchStock -> ReadWatchListResponse.from(watchStock, currentPriceRepository, closingPriceRepository))
			.collect(Collectors.toList());
		return new ReadWatchListResponse(watchList.getName(), watchStockResponses);
	}

	@Transactional
	@Secured("ROLE_USER")
	public void deleteWatchLists(Long memberId, DeleteWatchListsRequests deleteWatchListsRequests) {
		List<WatchList> watchLists = watchListRepository.findAllById(deleteWatchListsRequests.getWatchlistIds());

		watchLists.forEach(watchList -> {
			validateWatchListAuthorization(watchList, memberId);
			watchListRepository.deleteById(watchList.getId());
		});
	}

	@Transactional
	@Secured("ROLE_USER")
	public void deleteWatchList(Long memberId, Long watchlistId) {
		WatchList watchList = watchListRepository.findById(watchlistId)
			.orElseThrow(() -> new FineAntsException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, memberId);
		watchListRepository.deleteById(watchlistId);
	}

	@Transactional
	@Secured("ROLE_USER")
	public void createWatchStocks(Long memberId, Long watchListId, CreateWatchStockRequest request) {
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
	@Secured("ROLE_USER")
	public void deleteWatchStocks(Long memberId, Long watchListId, DeleteWatchStocksRequest request) {
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, memberId);

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, request.getTickerSymbols());
	}

	@Transactional
	@Secured("ROLE_USER")
	public void deleteWatchStock(Long memberId, Long watchListId, String tickerSymbol) {
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, memberId);

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, List.of(tickerSymbol));
	}

	@Transactional
	@Secured("ROLE_USER")
	public void changeWatchListName(Long memberId, Long watchListId, ChangeWatchListNameRequest request) {
		Member member = findMember(memberId);
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(watchList, member.getId());

		watchList.changeName(request.getName());
	}

	@Transactional(readOnly = true)
	@Secured("ROLE_USER")
	public List<WatchListHasStockResponse> hasStock(Long memberId, String tickerSymbol) {
		Member member = findMember(memberId);
		return watchListRepository.findWatchListsAndStockPresenceByMemberAndTickerSymbol(member, tickerSymbol);
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
