package codesquad.fineants.domain.watch_list.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.kis.repository.ClosingPriceRepository;
import codesquad.fineants.domain.kis.repository.CurrentPriceRepository;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.stock.repository.StockRepository;
import codesquad.fineants.domain.watch_list.domain.dto.request.ChangeWatchListNameRequest;
import codesquad.fineants.domain.watch_list.domain.dto.request.CreateWatchListRequest;
import codesquad.fineants.domain.watch_list.domain.dto.request.CreateWatchStockRequest;
import codesquad.fineants.domain.watch_list.domain.dto.request.DeleteWatchListsRequests;
import codesquad.fineants.domain.watch_list.domain.dto.request.DeleteWatchStocksRequest;
import codesquad.fineants.domain.watch_list.domain.dto.response.CreateWatchListResponse;
import codesquad.fineants.domain.watch_list.domain.dto.response.ReadWatchListResponse;
import codesquad.fineants.domain.watch_list.domain.dto.response.ReadWatchListsResponse;
import codesquad.fineants.domain.watch_list.domain.dto.response.WatchListHasStockResponse;
import codesquad.fineants.domain.watch_list.domain.entity.WatchList;
import codesquad.fineants.domain.watch_list.domain.entity.WatchStock;
import codesquad.fineants.domain.watch_list.event.publisher.WatchStockEventPublisher;
import codesquad.fineants.domain.watch_list.repository.WatchListRepository;
import codesquad.fineants.domain.watch_list.repository.WatchStockRepository;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.StockErrorCode;
import codesquad.fineants.global.errors.errorcode.WatchListErrorCode;
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
	public CreateWatchListResponse createWatchList(Long memberId, CreateWatchListRequest request) {
		Member member = findMember(memberId);
		WatchList watchList = WatchList.builder()
			.member(member)
			.name(request.getName())
			.build();
		watchList = watchListRepository.save(watchList);
		return CreateWatchListResponse.create(watchList.getId());
	}

	@Transactional(readOnly = true)
	public List<ReadWatchListsResponse> readWatchLists(Long memberId) {
		Member member = findMember(memberId);
		return watchListRepository.findByMember(member).stream()
			.map(ReadWatchListsResponse::from)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ReadWatchListResponse readWatchList(Long memberId, Long watchListId) {
		Member member = findMember(memberId);
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		List<WatchStock> watchStocks = watchStockRepository.findWithStockAndDividendsByWatchList(watchList);

		List<ReadWatchListResponse.WatchStockResponse> watchStockResponses = watchStocks.stream()
			.map(watchStock -> ReadWatchListResponse.from(watchStock, currentPriceRepository, closingPriceRepository))
			.collect(Collectors.toList());
		return new ReadWatchListResponse(watchList.getName(), watchStockResponses);
	}

	@Transactional
	public void deleteWatchLists(Long memberId, DeleteWatchListsRequests deleteWatchListsRequests) {
		Member member = findMember(memberId);
		List<WatchList> watchLists = watchListRepository.findAllById(deleteWatchListsRequests.getWatchlistIds());

		watchLists.forEach(watchList -> {
			if (!watchList.getMember().getId().equals(member.getId())) {
				throw new ForBiddenException(WatchListErrorCode.FORBIDDEN);
			}
			watchListRepository.deleteById(watchList.getId());
		});
	}

	@Transactional
	public void createWatchStocks(Long memberId, Long watchListId, CreateWatchStockRequest request) {
		Member member = findMember(memberId);
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
	public void deleteWatchStocks(Long memberId, Long watchListId, DeleteWatchStocksRequest request) {
		Member member = findMember(memberId);
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, request.getTickerSymbols());
	}

	@Transactional
	public void deleteWatchStock(Long memberId, Long watchListId, String tickerSymbol) {
		Member member = findMember(memberId);
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, List.of(tickerSymbol));
	}

	@Transactional
	public void changeWatchListName(Long memberId, Long watchListId, ChangeWatchListNameRequest request) {
		Member member = findMember(memberId);
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		watchList.change(request.getName());
	}

	@Transactional(readOnly = true)
	public List<WatchListHasStockResponse> hasStock(Long memberId, String tickerSymbol) {
		Member member = findMember(memberId);
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
