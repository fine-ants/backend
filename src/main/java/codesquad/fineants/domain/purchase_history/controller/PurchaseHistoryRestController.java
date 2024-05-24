package codesquad.fineants.domain.purchase_history.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.purchase_history.domain.dto.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.domain.purchase_history.domain.dto.request.PurchaseHistoryUpdateRequest;
import codesquad.fineants.domain.purchase_history.domain.dto.response.PurchaseHistoryDeleteResponse;
import codesquad.fineants.domain.purchase_history.service.PurchaseHistoryService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.PurchaseHistorySuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}/purchaseHistory")
@RequiredArgsConstructor
@RestController
public class PurchaseHistoryRestController {

	private final PurchaseHistoryService service;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<Void> createPurchaseHistory(
		@PathVariable Long portfolioId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable Long portfolioHoldingId,
		@Valid @RequestBody PurchaseHistoryCreateRequest request) {
		log.info("매입 내역 추가 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);
		service.createPurchaseHistory(request, portfolioId, portfolioHoldingId, authentication.getId());
		return ApiResponse.success(PurchaseHistorySuccessCode.CREATED_ADD_PURCHASE_HISTORY);
	}

	@PutMapping("/{purchaseHistoryId}")
	public ApiResponse<Void> updatePurchaseHistory(
		@PathVariable Long portfolioId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable Long portfolioHoldingId,
		@PathVariable Long purchaseHistoryId,
		@Valid @RequestBody PurchaseHistoryUpdateRequest request) {
		log.info("매입 내역 수정 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);
		service.updatePurchaseHistory(request, portfolioHoldingId, purchaseHistoryId, portfolioId,
			authentication.getId());
		return ApiResponse.success(PurchaseHistorySuccessCode.OK_MODIFY_PURCHASE_HISTORY);
	}

	@DeleteMapping("/{purchaseHistoryId}")
	public ApiResponse<Void> deletePurchaseHistory(
		@PathVariable Long portfolioId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@PathVariable Long portfolioHoldingId,
		@PathVariable Long purchaseHistoryId) {
		log.info("매입 내역 삭제 요청 : portfolioHoldingId={}, purchaseHistoryId={}", portfolioHoldingId, purchaseHistoryId);
		PurchaseHistoryDeleteResponse response = service.deletePurchaseHistory(portfolioHoldingId, purchaseHistoryId,
			portfolioId, authentication.getId());
		log.info("매입 내역 삭제 결과 : response={}", response);
		return ApiResponse.success(PurchaseHistorySuccessCode.OK_DELETE_PURCHASE_HISTORY);
	}
}
