package co.fineants.api.global.init.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import co.fineants.api.domain.member.domain.entity.Role;
import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "role")
public class RoleProperties {

	private final List<RoleProperty> rolePropertyList;

	@ConstructorBinding
	public RoleProperties(RoleProperty admin, RoleProperty manager, RoleProperty user) {
		this.rolePropertyList = List.of(admin, manager, user);
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
}
