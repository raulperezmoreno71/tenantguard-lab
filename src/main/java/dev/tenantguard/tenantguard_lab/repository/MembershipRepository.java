package dev.tenantguard.tenantguard_lab.repository;

import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUser_IdAndOrganization_Id(Long userId, Long organizationId);
    Optional<Membership> findByOrganization_IdAndId(Long organizationId, Long memberId);
    List<Membership> findByOrganization_IdOrderByIdAsc(Long organizationId);
    long countByOrganization_IdAndRole(Long organizationId, Role role);
}
