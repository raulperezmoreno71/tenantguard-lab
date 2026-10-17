package dev.tenantguard.tenantguard_lab.dto;

public record CreateDocumentResponse(Long id, String title, String content, Long organizationId, Long createdBy) {
}
