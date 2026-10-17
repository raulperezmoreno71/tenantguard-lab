package dev.tenantguard.tenantguard_lab.controller;

import dev.tenantguard.tenantguard_lab.dto.AddMemberRequest;
import dev.tenantguard.tenantguard_lab.dto.AddMemberResponse;
import dev.tenantguard.tenantguard_lab.dto.UpdateMemberRoleRequest;
import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(membership));
    }

    @GetMapping
    public ResponseEntity<List<AddMemberResponse>> getMembers(
            @PathVariable Long organizationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(membershipService.getMembers(organizationId, Long.valueOf(jwt.getSubject()))
                .stream().map(this::toResponse).toList());
    }

    @PutMapping("/{memberId}/role")
    public ResponseEntity<AddMemberResponse> updateRole(
            @PathVariable Long organizationId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(toResponse(membershipService.updateRole(
                organizationId, memberId, Long.valueOf(jwt.getSubject()), request.role())));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long organizationId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        membershipService.removeMember(organizationId, memberId, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    private AddMemberResponse toResponse(Membership membership) {
        return new AddMemberResponse(
                membership.getId(), membership.getUser().getId(), membership.getUser().getUsername(),
                membership.getOrganization().getId(), membership.getRole());
    }
}
