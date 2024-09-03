package co.fineants.api.domain.common.money;

import java.util.Objects;

public class Pair {
	private final Currency from;
	private final Currency to;

	public Pair(Currency from, Currency to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || getClass() != object.getClass()) {
			return false;
		}
		Pair pair = (Pair)object;
		return from == pair.from && to == pair.to;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}
}
