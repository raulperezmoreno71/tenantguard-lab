package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.Document;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.exception.DocumentNotFoundException;
import dev.tenantguard.tenantguard_lab.exception.ResourceNotFoundException;
import dev.tenantguard.tenantguard_lab.repository.DocumentRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found."));

        return documentRepository.save(new Document(title, content, organization, authService.findUserById(userId)));
    }

    @Transactional(readOnly = true)
    public List<Document> getDocuments(Long organizationId, Long userId) {
        membershipService.requireRole(userId, organizationId, Role.VIEWER);

        return documentRepository.findByOrganization_Id(organizationId);
    }

    @Transactional(readOnly = true)
    public Document getDocumentById(Long organizationId, Long documentId, Long userId) {
        membershipService.requireRole(userId, organizationId, Role.VIEWER);

        return documentRepository.findByOrganization_IdAndId(organizationId, documentId)
                .orElseThrow(DocumentNotFoundException::new);
    }

    @Transactional
    public Document updateDocument(Long organizationId, Long documentId, Long userId, String title, String content) {
        membershipService.requireRole(userId, organizationId, Role.EDITOR);
        Document document = documentRepository.findByOrganization_IdAndId(organizationId, documentId)
                .orElseThrow(DocumentNotFoundException::new);
        document.setTitle(title);
        document.setContent(content);
        return document;
    }

    @Transactional
    public void deleteDocument(Long organizationId, Long documentId, Long userId) {
        membershipService.requireRole(userId, organizationId, Role.EDITOR);
        Document document = documentRepository.findByOrganization_IdAndId(organizationId, documentId)
                .orElseThrow(DocumentNotFoundException::new);
        documentRepository.delete(document);
    }
}
