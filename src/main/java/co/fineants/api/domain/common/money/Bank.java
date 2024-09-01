package co.fineants.api.domain.common.money;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Bank {

	private static Bank instance;
	private final Map<Pair, Double> rates = new ConcurrentHashMap<>();

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
