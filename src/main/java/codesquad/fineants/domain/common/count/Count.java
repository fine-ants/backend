package codesquad.fineants.domain.common.count;

import java.math.BigInteger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Count {
	private final BigInteger value;

	public Count() {
		this.value = BigInteger.ZERO;
	}

	public static Count from(String value) {
		return new Count(new BigInteger(value));
	}

	public static Count from(Long value) {
		return new Count(BigInteger.valueOf(value));
	}
}
