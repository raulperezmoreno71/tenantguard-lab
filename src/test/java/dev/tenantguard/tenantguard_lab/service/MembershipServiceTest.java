package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.exception.ConflictException;
import dev.tenantguard.tenantguard_lab.exception.ResourceNotFoundException;
import dev.tenantguard.tenantguard_lab.repository.MembershipRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import dev.tenantguard.tenantguard_lab.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {
    @Mock MembershipRepository memberships;
    @Mock UserRepository users;
    @Mock OrganizationRepository organizations;

    MembershipService service;
    Organization organization;
    User admin;
    Membership adminMembership;

    @BeforeEach
    void setUp() {
        service = new MembershipService(memberships, users, organizations);
        organization = new Organization("Alpha");
        admin = new User("admin", "hash");
        adminMembership = new Membership(admin, organization, Role.ADMIN);
    }

    @Test
    void roleHierarchyAndMissingMembershipAreEnforced() {
        when(memberships.findByUser_IdAndOrganization_Id(10L, 1L))
                .thenReturn(Optional.of(new Membership(admin, organization, Role.VIEWER)));
        service.requireRole(10L, 1L, Role.VIEWER);
        assertThrows(AccessDeniedException.class, () -> service.requireRole(10L, 1L, Role.EDITOR));

        when(memberships.findByUser_IdAndOrganization_Id(10L, 1L))
                .thenReturn(Optional.of(new Membership(admin, organization, Role.EDITOR)));
        service.requireRole(10L, 1L, Role.EDITOR);
        assertThrows(AccessDeniedException.class, () -> service.requireRole(10L, 1L, Role.ADMIN));

        when(memberships.findByUser_IdAndOrganization_Id(10L, 1L))
                .thenReturn(Optional.of(adminMembership));
        service.requireRole(10L, 1L, Role.ADMIN);
        when(memberships.findByUser_IdAndOrganization_Id(10L, 2L)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> service.requireRole(10L, 2L, Role.VIEWER));
    }

    @Test
    void adminCanAddAndListMembers() {
        allowAdmin();
        User candidate = new User("new", "hash");
        when(users.findByUsername("new")).thenReturn(Optional.of(candidate));
        when(memberships.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Membership added = service.addMember(1L, 10L, "new", Role.VIEWER);
        when(memberships.findByOrganization_IdOrderByIdAsc(1L)).thenReturn(List.of(adminMembership, added));

        assertEquals(Role.VIEWER, added.getRole());
        assertEquals(List.of(adminMembership, added), service.getMembers(1L, 10L));
    }

    @Test
    void duplicateMemberIsConflict() {
        allowAdmin();
        User candidate = new User("new", "hash");
        when(users.findByUsername("new")).thenReturn(Optional.of(candidate));
        when(memberships.findByUser_IdAndOrganization_Id(null, 1L)).thenReturn(Optional.of(adminMembership));
        assertThrows(ConflictException.class, () -> service.addMember(1L, 10L, "new", Role.VIEWER));
    }

    @Test
    void lastAdminCannotBeDemotedOrRemoved() {
        allowAdmin();
        when(memberships.findByOrganization_IdAndId(1L, 7L)).thenReturn(Optional.of(adminMembership));
        when(memberships.countByOrganization_IdAndRole(1L, Role.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> service.updateRole(1L, 7L, 10L, Role.EDITOR));
        assertThrows(ConflictException.class, () -> service.removeMember(1L, 7L, 10L));
        assertEquals(Role.ADMIN, adminMembership.getRole());
        verify(memberships, never()).delete(any());
    }

    @Test
    void adminCanChangeRoleAndRemoveMemberWhenAnotherAdminRemains() {
        allowAdmin();
        Membership otherAdmin = new Membership(new User("other", "hash"), organization, Role.ADMIN);
        when(memberships.findByOrganization_IdAndId(1L, 7L)).thenReturn(Optional.of(otherAdmin));
        when(memberships.countByOrganization_IdAndRole(1L, Role.ADMIN)).thenReturn(2L);

        assertSame(otherAdmin, service.updateRole(1L, 7L, 10L, Role.EDITOR));
        assertEquals(Role.EDITOR, otherAdmin.getRole());
        service.removeMember(1L, 7L, 10L);
        verify(memberships).delete(otherAdmin);
    }

    @Test
    void targetMemberIsScopedToOrganization() {
        allowAdmin();
        when(memberships.findByOrganization_IdAndId(1L, 7L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateRole(1L, 7L, 10L, Role.EDITOR));
        assertThrows(ResourceNotFoundException.class, () -> service.removeMember(1L, 7L, 10L));
        verify(memberships, never()).findById(anyLong());
    }

    @Test
    void nonAdminCannotListOrModifyMembers() {
        when(organizations.findByIdForUpdate(1L)).thenReturn(Optional.of(organization));
        when(memberships.findByUser_IdAndOrganization_Id(10L, 1L))
                .thenReturn(Optional.of(new Membership(admin, organization, Role.EDITOR)));
        assertThrows(AccessDeniedException.class, () -> service.getMembers(1L, 10L));
        assertThrows(AccessDeniedException.class, () -> service.addMember(1L, 10L, "new", Role.VIEWER));
        assertThrows(AccessDeniedException.class, () -> service.updateRole(1L, 7L, 10L, Role.ADMIN));
        assertThrows(AccessDeniedException.class, () -> service.removeMember(1L, 7L, 10L));
        verify(memberships, never()).findByOrganization_IdAndId(anyLong(), anyLong());
    }

    private void allowAdmin() {
        when(organizations.findByIdForUpdate(1L)).thenReturn(Optional.of(organization));
        when(memberships.findByUser_IdAndOrganization_Id(10L, 1L)).thenReturn(Optional.of(adminMembership));
    }
}
