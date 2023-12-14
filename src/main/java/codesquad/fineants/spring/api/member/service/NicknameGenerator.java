package codesquad.fineants.spring.api.member.service;

import static org.apache.logging.log4j.util.Strings.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NicknameGenerator {

	private static final String HYPHEN = "-";

	private final String prefix;
	private final int len;

	public NicknameGenerator(
		@Value("${member.nickname.prefix}") String prefix,
		@Value("${member.nickname.len}") int len) {
		this.prefix = prefix;
		this.len = len;
	}

	public String generate() {
		return String.join(EMPTY, prefix, UUID.randomUUID().toString().replaceAll(HYPHEN, EMPTY).substring(0, len));
	}
}
