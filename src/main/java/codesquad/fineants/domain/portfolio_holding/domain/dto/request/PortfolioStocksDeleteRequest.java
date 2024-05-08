package codesquad.fineants.domain.portfolio_holding.domain.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioStocksDeleteRequest {
	@NotNull(message = "삭제할 포트폴리오 종목들이 없습니다")
	@Size(min = 1, message = "삭제할 포트폴리오 종목들이 없습니다")
	private List<Long> portfolioHoldingIds;

	@Override
	public String toString() {
		return String.format("%s %s(portfolioHoldingIds=%s)", "포트폴리오 종목 다수 삭제 요청",
			this.getClass().getSimpleName(),
			portfolioHoldingIds);
	}
}
