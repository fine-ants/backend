package codesquad.fineants.global.init;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import codesquad.fineants.domain.member.domain.entity.Role;
import lombok.Getter;

@ConfigurationProperties(prefix = "role")
public class RoleProperties {

	private final RoleProperty admin;
	private final RoleProperty manager;
	private final RoleProperty user;

	@ConstructorBinding
	public RoleProperties(RoleProperty admin, RoleProperty manager, RoleProperty user) {
		this.admin = admin;
		this.manager = manager;
		this.user = user;
	}

	@Getter
	public static class RoleProperty {
		private final String roleName;
		private final String roleDesc;

		@ConstructorBinding
		public RoleProperty(String roleName, String roleDesc) {
			this.roleName = roleName;
			this.roleDesc = roleDesc;
		}

		public Role toRoleEntity() {
			return Role.create(roleName, roleDesc);
		}
	}

	public List<RoleProperty> getRoles() {
		return List.of(admin, manager, user);
	}
}
