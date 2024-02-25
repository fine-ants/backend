package codesquad.fineants.domain.notification.type;

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

	public static NotificationType from(String target) {
		for (NotificationType type : values()) {
			if (type.name.equals(target)) {
				return type;
			}
		}
		throw new IllegalArgumentException("잘못된 매개변수입니다. target=" + target);
	}
}
