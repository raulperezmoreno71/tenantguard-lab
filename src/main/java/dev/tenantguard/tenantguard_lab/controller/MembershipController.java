package dev.tenantguard.tenantguard_lab.controller;

import dev.tenantguard.tenantguard_lab.dto.AddMemberRequest;
import dev.tenantguard.tenantguard_lab.dto.AddMemberResponse;
import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/{organizationId}/members")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<AddMemberResponse> addMember(
            @PathVariable Long organizationId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal Jwt jwt
            ) {
        Long adminId = Long.valueOf(jwt.getSubject());

        Membership membership = membershipService.addMember(organizationId, adminId, request.username(), request.role());

        AddMemberResponse response = new AddMemberResponse(
                membership.getId(),
                membership.getUser().getId(),
                membership.getUser().getUsername(),
                membership.getOrganization().getId(),
                membership.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
