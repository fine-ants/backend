package codesquad.fineants.domain.portfolio;

import static codesquad.fineants.domain.portfolio.QPortfolio.*;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class PortfolioPaginationRepository {

	private final JPAQueryFactory queryFactory;

	public Slice<Portfolio> findAll(BooleanBuilder conditions, Pageable pageable) {
		List<Portfolio> result = queryFactory.selectFrom(portfolio)
			.where(conditions)
			.limit(pageable.getPageSize() + 1)
			.fetch();
		return new PageImpl<>(result, pageable, result.size());
	}
}
