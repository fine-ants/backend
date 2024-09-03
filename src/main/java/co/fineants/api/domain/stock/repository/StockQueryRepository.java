package co.fineants.api.domain.stock.repository;

import static co.fineants.api.domain.stock.domain.entity.QStock.*;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import co.fineants.api.domain.stock.domain.entity.Stock;
import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	public List<Stock> getSliceOfStock(@Nullable String tickerSymbol, int size, @Nullable String keyword) {
		return jpaQueryFactory.selectFrom(stock)
			.where(ltStockTickerSymbol(tickerSymbol), search(keyword))
			.orderBy(stock.tickerSymbol.desc())
			.limit(size)
			.fetch();
	}

	private BooleanExpression ltStockTickerSymbol(@Nullable String tickerSymbol) {
		return tickerSymbol == null ? null : stock.tickerSymbol.lt(tickerSymbol);
	}

	private BooleanExpression search(String keyword) {
		if (keyword == null) {
			return null;
		}
		return Objects.requireNonNull(containsStockCode(keyword))
			.or(containsTickerSymbol(keyword))
			.or(containsCompanyName(keyword))
			.or(containsCompanyNameEng(keyword));
	}

	private BooleanExpression containsStockCode(@Nullable String keyword) {
		return Strings.hasText(keyword) ? stock.stockCode.contains(keyword) : null;
	}

	private BooleanExpression containsTickerSymbol(@Nullable String keyword) {
		return Strings.hasText(keyword) ? stock.tickerSymbol.contains(keyword) : null;
	}

	private BooleanExpression containsCompanyName(@Nullable String keyword) {
		return Strings.hasText(keyword) ? stock.companyName.contains(keyword) : null;
	}

	private BooleanExpression containsCompanyNameEng(@Nullable String keyword) {
		return Strings.hasText(keyword) ? stock.companyNameEng.contains(keyword) : null;
	}
}
