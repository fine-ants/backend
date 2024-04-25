package codesquad.fineants.domain.common.money;

public class Pair {
	private final String from;
	private final String to;

	public Pair(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean equals(Object object) {
		Pair pair = (Pair)object;
		return from.equals(pair.from) && to.equals(pair.to);
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
