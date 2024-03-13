package codesquad.fineants.domain.notification.type;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;

@Getter
public enum NotificationType {
	PORTFOLIO_TARGET_GAIN("목표 수익률", "portfolio"),
	PORTFOLIO_MAX_LOSS("최대 손실율", "portfolio"),
	STOCK_TARGET_PRICE("종목 지정가", "stock");

	private final String name;
	private final String category;

	NotificationType(String name, String category) {
		this.name = name;
		this.category = category;
	}

	@JsonCreator
	public static NotificationType from(String type) {
		return NotificationType.valueOf(type);
	}
}
