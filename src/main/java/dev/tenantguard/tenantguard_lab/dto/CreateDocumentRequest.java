package dev.tenantguard.tenantguard_lab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDocumentRequest(@NotBlank @Size(max = 150) String title, @NotNull String content) {
}
