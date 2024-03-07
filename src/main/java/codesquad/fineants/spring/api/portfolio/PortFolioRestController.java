package codesquad.fineants.spring.api.portfolio;

import javax.validation.Valid;

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

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.portfolio.request.PortfolioCreateRequest;
import codesquad.fineants.spring.api.portfolio.request.PortfolioModifyRequest;
import codesquad.fineants.spring.api.portfolio.request.PortfoliosDeleteRequest;
import codesquad.fineants.spring.api.portfolio.response.PortFolioCreateResponse;
import codesquad.fineants.spring.api.portfolio.response.PortfoliosResponse;
import codesquad.fineants.spring.api.response.ApiResponse;
import codesquad.fineants.spring.api.success.code.PortfolioSuccessCode;
import codesquad.fineants.spring.auth.HasPortfolioAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@RestController
public class PortFolioRestController {

	private final PortFolioService portFolioService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<PortFolioCreateResponse> createPortfolio(@Valid @RequestBody PortfolioCreateRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		log.info("포트폴리오 추가 요청, request={}", request);
		PortFolioCreateResponse response = portFolioService.createPortfolio(request, authMember);
		return ApiResponse.success(PortfolioSuccessCode.CREATED_ADD_PORTFOLIO, response);
	}

	@GetMapping
	public ApiResponse<PortfoliosResponse> searchMyAllPortfolios(@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(PortfolioSuccessCode.OK_SEARCH_PORTFOLIOS,
			portFolioService.readMyAllPortfolio(authMember));
	}

	@HasPortfolioAuthorization
	@PutMapping("/{portfolioId}")
	public ApiResponse<Void> updatePortfolio(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember,
		@Valid @RequestBody PortfolioModifyRequest request) {
		log.info("포트폴리오 수정 요청, request={}", request);
		portFolioService.updatePortfolio(request, portfolioId, authMember);
		return ApiResponse.success(PortfolioSuccessCode.OK_MODIFY_PORTFOLIO);
	}

	@HasPortfolioAuthorization
	@DeleteMapping("/{portfolioId}")
	public ApiResponse<Void> deletePortfolio(@PathVariable Long portfolioId,
		@AuthPrincipalMember AuthMember authMember) {
		log.info("포트폴리오 삭제 요청, portfolioId={}", portfolioId);
		portFolioService.deletePortfolio(portfolioId, authMember);
		return ApiResponse.success(PortfolioSuccessCode.OK_DELETE_PORTFOLIO);
	}

	@DeleteMapping
	public ApiResponse<Void> deletePortfolios(@RequestBody PortfoliosDeleteRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		portFolioService.deletePortfolios(request, authMember);
		return ApiResponse.success(PortfolioSuccessCode.OK_DELETE_PORTFOLIO);
	}
}
