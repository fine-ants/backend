package co.fineants.api.domain.portfolio.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioNameItem {
	private final Long id;
	private final String name;
	private final LocalDateTime dateCreated;

	public static PortfolioNameItem from(Portfolio portfolio) {
		return new PortfolioNameItem(
			portfolio.getId(),
			portfolio.name(),
			portfolio.getCreateAt()
		);
	}

	@Override
	public String toString() {
		return String.format("(id=%d, name=%s, dateCreated=%s)", id, name, dateCreated);
	}
}
