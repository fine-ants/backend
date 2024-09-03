package co.fineants.api.domain.watchlist.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.fineants.api.domain.watchlist.domain.dto.request.ChangeWatchListNameRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchListRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.CreateWatchStockRequest;
import co.fineants.api.domain.watchlist.domain.dto.request.DeleteWatchListsRequests;
import co.fineants.api.domain.watchlist.domain.dto.request.DeleteWatchStocksRequest;
import co.fineants.api.domain.watchlist.domain.dto.response.CreateWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.ReadWatchListsResponse;
import co.fineants.api.domain.watchlist.domain.dto.response.WatchListHasStockResponse;
import co.fineants.api.domain.watchlist.service.WatchListService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.WatchListSuccessCode;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
@RestController
public class WatchListRestController {

	private final WatchListService watchListService;

	@PostMapping
	public ApiResponse<CreateWatchListResponse> createWatchList(
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@RequestBody CreateWatchListRequest request) {
		return ApiResponse.success(WatchListSuccessCode.CREATED_WATCH_LIST,
			watchListService.createWatchList(authentication.getId(), request));
	}

	@GetMapping
	public ApiResponse<List<ReadWatchListsResponse>> readWatchLists(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		return ApiResponse.success(WatchListSuccessCode.READ_WATCH_LISTS,
			watchListService.readWatchLists(authentication.getId()));
	}

	@GetMapping("/{watchlistId}")
	public ApiResponse<ReadWatchListResponse> readWatchList(
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable Long watchlistId) {
		return ApiResponse.success(WatchListSuccessCode.READ_WATCH_LIST,
			watchListService.readWatchList(authentication.getId(), watchlistId));
	}

	@GetMapping("/stockExists/{tickerSymbol}")
	public ApiResponse<List<WatchListHasStockResponse>> watchListHasStock(
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable String tickerSymbol) {
		return ApiResponse.success(WatchListSuccessCode.HAS_STOCK,
			watchListService.hasStock(authentication.getId(), tickerSymbol));
	}

	@PutMapping("/{watchlistId}")
	public ApiResponse<Void> changeWatchListName(@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable Long watchlistId, @RequestBody ChangeWatchListNameRequest request) {
		watchListService.changeWatchListName(authentication.getId(), watchlistId, request);
		return ApiResponse.success(WatchListSuccessCode.CHANGE_WATCH_LIST_NAME);
	}

	@DeleteMapping
	public ApiResponse<Void> deleteWatchLists(@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@RequestBody DeleteWatchListsRequests deleteWatchListsRequests) {
		watchListService.deleteWatchLists(authentication.getId(), deleteWatchListsRequests.getWatchlistIds());
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_LIST);
	}

	@DeleteMapping("/{watchlistId}")
	public ApiResponse<Void> deleteWatchList(@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable Long watchlistId) {
		watchListService.deleteWatchList(authentication.getId(), watchlistId);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_LIST);
	}

	@PostMapping("/{watchlistId}/stock")
	public ApiResponse<Void> createWatchStocks(@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable Long watchlistId, @RequestBody CreateWatchStockRequest request) {
		watchListService.createWatchStocks(authentication.getId(), watchlistId, request);
		return ApiResponse.success(WatchListSuccessCode.CREATED_WATCH_STOCK);
	}

	@DeleteMapping("/{watchlistId}/stock")
	public ApiResponse<Void> deleteWatchStocks(@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable("watchlistId") Long watchListId, @RequestBody DeleteWatchStocksRequest deleteWatchStocksRequest) {
		watchListService.deleteWatchStocks(authentication.getId(), watchListId, deleteWatchStocksRequest);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_STOCK);
	}

	@DeleteMapping("/{watchlistId}/stock/{tickerSymbol}")
	public ApiResponse<Void> deleteWatchStock(@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable("watchlistId") Long watchListId, @PathVariable("tickerSymbol") String tickerSymbol) {
		watchListService.deleteWatchStock(authentication.getId(), watchListId, tickerSymbol);
		return ApiResponse.success(WatchListSuccessCode.DELETED_WATCH_STOCK);
	}
}
