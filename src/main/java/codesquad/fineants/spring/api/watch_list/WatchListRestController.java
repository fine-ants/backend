package codesquad.fineants.spring.api.watch_list;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.WatchListSuccessCode;
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

@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
@RestController
public class WatchListRestController {

	private final WatchListService watchListService;

	@PostMapping
	public ApiResponse<CreateWatchListResponse> createWatchList(@AuthPrincipalMember AuthMember authMember,
		@RequestBody CreateWatchListRequest request) {
		return ApiResponse.success(WatchListSuccessCode.CREATED_WATCH_LIST,
			watchListService.createWatchList(authMember, request));
	}

	@GetMapping
	public ApiResponse<List<ReadWatchListsResponse>> readWatchLists(@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(WatchListSuccessCode.READ_WATCH_LISTS, watchListService.readWatchLists(authMember));
	}

	@DeleteMapping
	public ApiResponse<Void> deleteWatchLists(@AuthPrincipalMember AuthMember authMember,
		@RequestBody DeleteWatchListsRequests deleteWatchListsRequests) {
		watchListService.deleteWatchLists(authMember, deleteWatchListsRequests);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_LIST);
	}

	@GetMapping("/{watchlistId}")
	public ApiResponse<ReadWatchListResponse> readWatchList(@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long watchlistId) {
		return ApiResponse.success(WatchListSuccessCode.READ_WATCH_LIST,
			watchListService.readWatchList(authMember, watchlistId));
	}

	@PutMapping("/{watchlistId}")
	public ApiResponse<Void> changeWatchListName(@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long watchlistId, @RequestBody ChangeWatchListNameRequest request) {
		watchListService.changeWatchListName(authMember, watchlistId, request);
		return ApiResponse.success(WatchListSuccessCode.CHANGE_WATCH_LIST_NAME);
	}

	@PostMapping("/{watchlistId}/stock")
	public ApiResponse<Void> createWatchStocks(@AuthPrincipalMember AuthMember authMember,
		@PathVariable Long watchlistId, @RequestBody CreateWatchStockRequest request) {
		watchListService.createWatchStocks(authMember, watchlistId, request);
		return ApiResponse.success(WatchListSuccessCode.CREATED_WATCH_STOCK);
	}

	@DeleteMapping("/{watchlistId}/stock")
	public ApiResponse<Void> deleteWatchStocks(@AuthPrincipalMember AuthMember authMember,
		@PathVariable("watchlistId") Long watchListId, @RequestBody DeleteWatchStocksRequest deleteWatchStocksRequest) {
		watchListService.deleteWatchStocks(authMember, watchListId, deleteWatchStocksRequest);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_STOCK);
	}

	@DeleteMapping("/{watchlistId}/stock/{tickerSymbol}")
	public ApiResponse<Void> deleteWatchStock(@AuthPrincipalMember AuthMember authMember,
		@PathVariable("watchlistId") Long watchListId, @PathVariable("tickerSymbol") String tickerSymbol) {
		watchListService.deleteWatchStock(authMember, watchListId, tickerSymbol);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_STOCK);
	}

	@GetMapping("/stockExists/{tickerSymbol}")
	public ApiResponse<List<WatchListHasStockResponse>> watchListHasStock(@AuthPrincipalMember AuthMember authMember,
		@PathVariable("tickerSymbol") String tickerSymbol) {
		return ApiResponse.success(WatchListSuccessCode.HAS_STOCK,
			watchListService.hasStock(authMember, tickerSymbol));
	}
}
