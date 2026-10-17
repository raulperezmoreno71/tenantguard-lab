package dev.tenantguard.tenantguard_lab.dto;

import dev.tenantguard.tenantguard_lab.entity.Role;

public record AddMemberResponse(Long id, Long userId, String username, Long organizationId, Role role) {
}
