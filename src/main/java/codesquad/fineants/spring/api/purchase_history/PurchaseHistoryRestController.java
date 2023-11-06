package codesquad.fineants.spring.api.purchase_history;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryCreateRequest;
import codesquad.fineants.spring.api.purchase_history.request.PurchaseHistoryModifyRequest;
import codesquad.fineants.spring.api.purchase_history.response.PurchaseHistoryDeleteResponse;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.PurchaseHistorySuccessCode;
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
	public ApiResponse<Void> addPurchaseHistory(@Valid @RequestBody PurchaseHistoryCreateRequest request,
		@PathVariable Long portfolioHoldingId,
		@AuthPrincipalMember AuthMember authMember) {
		log.info("매입 내역 추가 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);
		service.addPurchaseHistory(request, portfolioHoldingId, authMember);
		return ApiResponse.success(PurchaseHistorySuccessCode.CREATED_ADD_PURCHASE_HISTORY);
	}

	@PutMapping("/{purchaseHistoryId}")
	public ApiResponse<Void> modifyPurchaseHistory(@Valid @RequestBody PurchaseHistoryModifyRequest request,
		@PathVariable Long portfolioHoldingId,
		@PathVariable Long purchaseHistoryId,
		@AuthPrincipalMember AuthMember authMember) {
		log.info("매입 내역 수정 요청 : request={}, portfolioHoldingId={}", request, portfolioHoldingId);
		service.modifyPurchaseHistory(request, portfolioHoldingId, purchaseHistoryId, authMember);
		return ApiResponse.success(PurchaseHistorySuccessCode.OK_MODIFY_PURCHASE_HISTORY);
	}

	@DeleteMapping("/{purchaseHistoryId}")
	public ApiResponse<Void> deletePurchaseHistory(
		@PathVariable Long portfolioHoldingId,
		@PathVariable Long purchaseHistoryId,
		@AuthPrincipalMember AuthMember authMember) {
		log.info("매입 내역 삭제 요청 : portfolioHoldingId={}, purchaseHistoryId={}", portfolioHoldingId, purchaseHistoryId);
		PurchaseHistoryDeleteResponse response = service.deletePurchaseHistory(portfolioHoldingId, purchaseHistoryId,
			authMember);
		log.info("매입 내역 삭제 결과 : response={}", response);
		return ApiResponse.success(PurchaseHistorySuccessCode.OK_DELETE_PURCHASE_HISTORY);
	}
}
