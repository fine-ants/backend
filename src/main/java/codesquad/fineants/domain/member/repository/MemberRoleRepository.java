package codesquad.fineants.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import codesquad.fineants.domain.member.domain.entity.MemberRole;

public interface MemberRoleRepository extends JpaRepository<MemberRole, Long> {
}
