package dev.tenantguard.tenantguard_lab.controller;

import dev.tenantguard.tenantguard_lab.dto.CreateOrganizationRequest;
import dev.tenantguard.tenantguard_lab.dto.CreateOrganizationResponse;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.service.AuthService;
import dev.tenantguard.tenantguard_lab.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final AuthService authService;

    public OrganizationController(OrganizationService organizationService, AuthService authService) {
        this.organizationService = organizationService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CreateOrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        User user = authService.findUserById(userId);

        Organization organization = organizationService.createOrganization(request.name(), user);

        CreateOrganizationResponse response = new CreateOrganizationResponse(organization.getId(), organization.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
