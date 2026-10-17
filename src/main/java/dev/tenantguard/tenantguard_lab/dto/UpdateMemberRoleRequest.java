package dev.tenantguard.tenantguard_lab.dto;

import dev.tenantguard.tenantguard_lab.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(@NotNull Role role) {
}
