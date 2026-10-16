package dev.tenantguard.tenantguard_lab.controller;

import dev.tenantguard.tenantguard_lab.dto.CreateOrganizationRequest;
import dev.tenantguard.tenantguard_lab.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public void createOrganization(@Valid @RequestBody CreateOrganizationRequest createOrganizationRequest) {

    }
}
