package dev.tenantguard.tenantguard_lab.repository;

import dev.tenantguard.tenantguard_lab.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByUser_IdAndOrganization_Id(Long userId, Long organizationId);
}
