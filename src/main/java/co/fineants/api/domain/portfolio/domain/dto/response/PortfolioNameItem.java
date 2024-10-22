package co.fineants.api.domain.portfolio.domain.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PortfolioNameItem {
	@JsonProperty
	private final Long id;
	@JsonProperty
	private final String name;
	@JsonProperty
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
