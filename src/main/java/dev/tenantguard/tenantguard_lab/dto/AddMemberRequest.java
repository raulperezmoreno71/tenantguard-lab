package dev.tenantguard.tenantguard_lab.dto;

import dev.tenantguard.tenantguard_lab.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(@NotBlank String username, @NotNull Role role) {
}
