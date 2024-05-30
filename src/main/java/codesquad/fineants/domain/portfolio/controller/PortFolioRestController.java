package codesquad.fineants.domain.portfolio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioCreateRequest;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfolioModifyRequest;
import codesquad.fineants.domain.portfolio.domain.dto.request.PortfoliosDeleteRequest;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortFolioCreateResponse;
import codesquad.fineants.domain.portfolio.domain.dto.response.PortfoliosResponse;
import codesquad.fineants.domain.portfolio.service.PortFolioService;
import codesquad.fineants.global.api.ApiResponse;
import codesquad.fineants.global.security.oauth.dto.MemberAuthentication;
import codesquad.fineants.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import codesquad.fineants.global.success.PortfolioSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@RestController
public class PortFolioRestController {

	private final PortFolioService portFolioService;

	// 포트폴리오 생성
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<PortFolioCreateResponse> createPortfolio(@Valid @RequestBody PortfolioCreateRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		log.info("포트폴리오 추가 요청, request={}", request);
		PortFolioCreateResponse response = portFolioService.createPortfolio(request, authentication.getId());
		return ApiResponse.success(PortfolioSuccessCode.CREATED_ADD_PORTFOLIO, response);
	}

	// 포트폴리오 목록 조회
	@GetMapping
	public ApiResponse<PortfoliosResponse> searchMyAllPortfolios(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		return ApiResponse.success(PortfolioSuccessCode.OK_SEARCH_PORTFOLIOS,
			portFolioService.readMyAllPortfolio(authentication.getId()));
	}

	// 포트폴리오 수정
	@PutMapping("/{portfolioId}")
	public ApiResponse<Void> updatePortfolio(@PathVariable Long portfolioId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		@Valid @RequestBody PortfolioModifyRequest request) {
		log.info("포트폴리오 수정 요청, request={}", request);
		portFolioService.updatePortfolio(request, portfolioId, authentication.getId());
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO);
	}

	// 포트폴리오 단일 삭제
	@DeleteMapping("/{portfolioId}")
	public ApiResponse<Void> deletePortfolio(@PathVariable Long portfolioId,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		log.info("포트폴리오 삭제 요청, portfolioId={}", portfolioId);
		portFolioService.deletePortfolio(portfolioId, authentication.getId());
		return ApiResponse.success(PortfolioSuccessCode.OK_DELETE_PORTFOLIO);
	}

	// 포트폴리오 다수 삭제
	@DeleteMapping
	public ApiResponse<Void> deletePortfolios(@RequestBody PortfoliosDeleteRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		portFolioService.deletePortfolios(request, authentication.getId());
		return ApiResponse.success(PortfolioSuccessCode.OK_DELETE_PORTFOLIO);
	}
}
