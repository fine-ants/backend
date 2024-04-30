package codesquad.fineants.domain.common.money;

import java.util.Hashtable;

public final class Bank {

	private static Bank instance;
	private final Hashtable<Pair, Double> rates = new Hashtable<>();

	Bank() {
	}

	public static Bank getInstance() {
		if (instance == null) {
			instance = new Bank();
		}
		return instance;
	}

	public Money reduce(Expression source, Currency to) {
		return source.reduce(this, to);
	}

	public void addRate(Currency from, Currency to, double rate) {
		rates.put(new Pair(from, to), rate);
	}

	double rate(Currency from, Currency to) {
		if (from.equals(to)) {
			return 1;
		}
		return rates.get(new Pair(from, to));
	}

	public Money toWon(Expression amount) {
		return reduce(amount, Currency.KRW);
	}
}
