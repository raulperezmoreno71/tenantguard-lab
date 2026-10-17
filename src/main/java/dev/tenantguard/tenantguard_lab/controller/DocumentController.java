package dev.tenantguard.tenantguard_lab.controller;

import dev.tenantguard.tenantguard_lab.dto.CreateDocumentRequest;
import dev.tenantguard.tenantguard_lab.dto.CreateDocumentResponse;
import dev.tenantguard.tenantguard_lab.entity.Document;
import dev.tenantguard.tenantguard_lab.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<CreateDocumentResponse> createDocument(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal Jwt jwt
            ) {
        Long userId = Long.valueOf(jwt.getSubject());

        Document document = documentService.createDocument(organizationId, userId, request.title(), request.content());

        CreateDocumentResponse response = new CreateDocumentResponse(document.getId(), document.getTitle(), document.getContent(), organizationId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CreateDocumentResponse>> getDocuments(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        List<Document> documents = documentService.getDocuments(organizationId, userId);

        List<CreateDocumentResponse> responses = new ArrayList<>();

        for (Document document : documents) {
            responses.add(new CreateDocumentResponse(
                    document.getId(),
                    document.getTitle(),
                    document.getContent(),
                    document.getOrganization().getId(),
                    document.getCreatedBy().getId()
            ));
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<CreateDocumentResponse> getDocumentById(
        @PathVariable Long organizationId,
        @PathVariable Long documentId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        Document document = documentService.getDocumentById(organizationId, documentId, userId);

        return ResponseEntity.ok(new CreateDocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getOrganization().getId(),
                document.getCreatedBy().getId()
        ));
    }

    @PutMapping("/{documentId}")
    public ResponseEntity<CreateDocumentResponse> updateDocument(
            @PathVariable Long organizationId,
            @PathVariable Long documentId,
            @Valid @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Document document = documentService.updateDocument(
                organizationId, documentId, Long.valueOf(jwt.getSubject()), request.title(), request.content());
        return ResponseEntity.ok(new CreateDocumentResponse(
                document.getId(), document.getTitle(), document.getContent(),
                document.getOrganization().getId(), document.getCreatedBy().getId()));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long organizationId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        documentService.deleteDocument(organizationId, documentId, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}
