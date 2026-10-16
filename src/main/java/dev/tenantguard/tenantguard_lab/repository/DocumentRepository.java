package dev.tenantguard.tenantguard_lab.repository;

import dev.tenantguard.tenantguard_lab.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOrganization_Id (Long organizationId);
}
