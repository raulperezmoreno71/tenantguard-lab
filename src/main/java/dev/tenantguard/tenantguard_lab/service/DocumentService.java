package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.Document;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.repository.DocumentRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthService authService;
    private final MembershipService membershipService;

    public DocumentService(
            DocumentRepository documentRepository,
            OrganizationRepository organizationRepository,
            AuthService authService,
            MembershipService membershipService
    ) {
        this.documentRepository = documentRepository;
        this.organizationRepository = organizationRepository;
        this.authService = authService;
        this.membershipService = membershipService;
    }

    @Transactional
    public Document createDocument(Long organizationId, Long userId, String title, String content) {
        membershipService.requireRole(userId, organizationId, Role.EDITOR);

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found."));

        return documentRepository.save(new Document(title, content, organization, authService.findUserById(userId)));
    }
}
