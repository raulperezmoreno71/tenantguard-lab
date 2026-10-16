package dev.tenantguard.tenantguard_lab.repository;

import dev.tenantguard.tenantguard_lab.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);
}
