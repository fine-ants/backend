package codesquad.fineants.spring.api.watch_list;

import java.util.List;
import java.util.Optional;
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
import codesquad.fineants.spring.api.errors.errorcode.MemberErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.StockErrorCode;
import codesquad.fineants.spring.api.errors.errorcode.WatchListErrorCode;
import codesquad.fineants.spring.api.errors.exception.ForBiddenException;
import codesquad.fineants.spring.api.errors.exception.NotFoundResourceException;
import codesquad.fineants.spring.api.kis.manager.CurrentPriceManager;
import codesquad.fineants.spring.api.kis.manager.LastDayClosingPriceManager;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchListRequest;
import codesquad.fineants.spring.api.watch_list.request.CreateWatchStockRequest;
import codesquad.fineants.spring.api.watch_list.request.DeleteWatchListsRequests;
import codesquad.fineants.spring.api.watch_list.request.DeleteWatchStocksRequest;
import codesquad.fineants.spring.api.watch_list.response.CreateWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListResponse;
import codesquad.fineants.spring.api.watch_list.response.ReadWatchListsResponse;
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

	@Transactional
	public CreateWatchListResponse createWatchList(AuthMember authMember, CreateWatchListRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = WatchList.builder()
			.member(member)
			.name(request.getName())
			.build();
		watchList = watchListRepository.save(watchList);
		return new CreateWatchListResponse(watchList.getId());
	}

	@Transactional(readOnly = true)
	public List<ReadWatchListsResponse> readWatchLists(AuthMember authMember) {
		Member member = findMember(authMember.getMemberId());
		List<WatchList> watchLists = watchListRepository.findByMember(member);
		return ReadWatchListsResponse.from(watchLists);
	}

	@Transactional(readOnly = true)
	public List<ReadWatchListResponse> readWatchList(AuthMember authMember, Long watchListId) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));

		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		List<WatchStock> watchStocks = watchStockRepository.findWithStockAndDividendsByWatchList(watchList);

		return watchStocks.stream()
			.map(watchStock -> ReadWatchListResponse.from(watchStock, currentPriceManager, lastDayClosingPriceManager))
			.collect(Collectors.toList());
	}

	@Transactional
	public void deleteWatchLists(AuthMember authMember, DeleteWatchListsRequests deleteWatchListsRequests) {
		Member member = findMember(authMember.getMemberId());
		List<WatchList> watchLists = watchListRepository.findAllById(deleteWatchListsRequests.getWatchlistIds());

		watchLists.stream()
			.filter(watchList -> watchList.getMember().getId().equals(member.getId()))
			.forEach(watchList -> watchListRepository.deleteById(watchList.getId()));

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
	}

	@Transactional
	public void deleteWatchStocks(AuthMember authMember, Long watchListId, DeleteWatchStocksRequest request) {
		Member member = findMember(authMember.getMemberId());
		WatchList watchList = watchListRepository.findById(watchListId)
			.orElseThrow(() -> new NotFoundResourceException(WatchListErrorCode.NOT_FOUND_WATCH_LIST));
		validateWatchListAuthorization(member.getId(), watchList.getMember().getId());

		watchStockRepository.deleteByWatchListAndStock_TickerSymbolIn(watchList, request.getTickerSymbols());
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
