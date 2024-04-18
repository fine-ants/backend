package codesquad.fineants.spring.api.watch_list.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.member.Member;
import codesquad.fineants.domain.member.MemberRepository;
import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.stock.StockRepository;
import codesquad.fineants.domain.watch_list.WatchList;
import codesquad.fineants.domain.watch_list.WatchListRepository;
import codesquad.fineants.domain.watch_stock.WatchStock;
import codesquad.fineants.domain.watch_stock.WatchStockRepository;
import codesquad.fineants.spring.api.common.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.common.errors.errorcode.WatchListErrorCode;
import codesquad.fineants.spring.api.common.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.common.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.watch_list.event.publisher.WatchStockEventPublisher;
import codesquad.fineants.spring.api.watch_list.request.ChangeWatchListNameRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchStockRequest;
import codesquad.fineants.spring.api.watch_list.request.DeleteWatchListsRequests;
import codesquad.fineants.spring.api.watch_list.request.DeleteWatchStocksRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;
import codesquad.fineants.spring.api.watch_list.response.WatchListHasStockResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WatchListService {

	private final WatchListRepository watchListRepository;
	private final MemberRepository memberRepository;
	private final StockRepository stockRepository;
	private final WatchStockRepository watchStockRepository;
	private final CurrentPriceManager currentPriceManager;
	private final LastDayClosingPriceManager lastDayClosingPriceManager;
	private final WatchStockEventPublisher watchStockEventPublisher;

	@Transactional
	public CreateWatchListResponse createWatchList(AuthMember authMember, CreateWatchListRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = WatchList.builder()
			.member(member)
			.name(request.getName())
			.build();
		watchList = watchListRepository.save(watchList);
		return CreateWatchListResponse.create(watchList.getId());
	}

	@Transactional(readOnly = true)
	public List<ReadWatchListsResponse> readWatchLists(AuthMember authMember) {
		Member member = findMember(authMember.getMemberId());
		return watchListRepository.findByMember(member).stream()
			.map(ReadWatchListsResponse::from)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ReadWatchListResponse readWatchList(AuthMember authMember, Long watchListId) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		List<WatchStock> watchStocks = watchStockRepository.findWithStockAndDividendsByWatchList(watchList);

		List<ReadWatchListResponse.WatchStockResponse> watchStockResponses = watchStocks.stream()
			.map(watchStock -> ReadWatchListResponse.from(watchStock, currentPriceManager, lastDayClosingPriceManager))
			.collect(Collectors.toList());
		return new ReadWatchListResponse(watchList.getName(), watchStockResponses);
	}

	@Transactional
	public void deleteWatchLists(AuthMember authMember, DeleteWatchListsRequests deleteWatchListsRequests) {
		Member member = findMember(authMember.getMemberId());
		List<WatchList> watchLists = watchListRepository.findAllById(deleteWatchListsRequests.getWatchlistIds());

		watchLists.forEach(watchList -> {
			if (!watchList.getMember().getId().equals(member.getId())) {
				throw new ForBiddenException(WatchListErrorCode.FORBIDDEN);
			}
			watchListRepository.deleteById(watchList.getId());
		});
	}

	@Transactional
	public void createWatchStocks(AuthMember authMember, Long watchListId, CreateWatchStockRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		request.getTickerSymbols().stream()
			.map(tickerSymbol -> stockRepository.findByTickerSymbol(tickerSymbol)
				.orElseThrow(() -> new NotFoundResourceException(StockErrorCode.NOT_FOUND_STOCK)))
			.map(stock -> WatchStock.builder()
				.watchList(watchList)
				.stock(stock)
				.build())
			.forEach(watchStockRepository::save);

		request.getTickerSymbols()
			.forEach(watchStockEventPublisher::publishWatchStock);
	}

	@Transactional
	public void deleteWatchStocks(AuthMember authMember, Long watchListId, DeleteWatchStocksRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, request.getTickerSymbols());
	}

	@Transactional
	public void deleteWatchStock(AuthMember authMember, Long watchListId, String tickerSymbol) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, List.of(tickerSymbol));
	}

	@Transactional
	public void changeWatchListName(AuthMember authMember, Long watchListId, ChangeWatchListNameRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		watchList.change(request.getName());
	}

	@Transactional(readOnly = true)
	public List<WatchListHasStockResponse> hasStock(AuthMember authMember, String tickerSymbol) {
		Member member = findMember(authMember.getMemberId());
		return watchListRepository.findWatchListsAndStockPresenceByMemberAndTickerSymbol(member, tickerSymbol);
	}

	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundResourceException(MemberErrorCode.NOT_FOUND_MEMBER));
	}

	private void validateWatchListAuthorization(Long memberId, Long watchListMemberId) {
		if (!memberId.equals(watchListMemberId)) {
			throw new ForBiddenException(WatchListErrorCode.FORBIDDEN);
		}
	}
}
