package co.fineants.api.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.fineants.api.domain.member.domain.entity.MemberRole;

public interface MemberRoleRepository extends JpaRepository<MemberRole, Long> {
}
