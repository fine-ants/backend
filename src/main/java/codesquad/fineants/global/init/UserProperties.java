package codesquad.fineants.global.init;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties(value = "member.user")
public class UserProperties {
	private final String email;
	private final String nickname;
	private final String password;

	@ConstructorBinding
	public UserProperties(String email, String nickname, String password) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
	}
}
