package dev.tenantguard.tenantguard_lab.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganizationRequest(
        @NotBlank String name
) {

}
