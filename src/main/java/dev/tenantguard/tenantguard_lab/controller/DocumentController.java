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
}
