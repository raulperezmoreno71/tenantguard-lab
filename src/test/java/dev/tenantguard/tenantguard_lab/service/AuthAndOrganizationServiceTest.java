package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.Membership;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.exception.InvalidCredentialsException;
import dev.tenantguard.tenantguard_lab.exception.OrganizationAlreadyExistsException;
import dev.tenantguard.tenantguard_lab.exception.UserAlreadyExistsException;
import dev.tenantguard.tenantguard_lab.repository.MembershipRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import dev.tenantguard.tenantguard_lab.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthAndOrganizationServiceTest {
    @Test
    void registrationHashesPasswordAndRejectsDuplicateUsername() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserRepository users = mock(UserRepository.class);
        AuthService service = new AuthService(encoder, users);
        when(encoder.encode("a sufficiently long password")).thenReturn("hash");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = service.register("user", "a sufficiently long password");
        assertEquals("hash", registered.getPasswordHash());
        when(users.findByUsername("user")).thenReturn(Optional.of(registered));
        assertThrows(UserAlreadyExistsException.class,
                () -> service.register("user", "a sufficiently long password"));
    }

    @Test
    void loginRejectsUnknownUserAndWrongPassword() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserRepository users = mock(UserRepository.class);
        AuthService service = new AuthService(encoder, users);
        assertThrows(InvalidCredentialsException.class, () -> service.authenticate("missing", "wrong"));

        User user = new User("user", "hash");
        when(users.findByUsername("user")).thenReturn(Optional.of(user));
        assertThrows(InvalidCredentialsException.class, () -> service.authenticate("user", "wrong"));
        when(encoder.matches("correct", "hash")).thenReturn(true);
        assertSame(user, service.authenticate("user", "correct"));
    }

    @Test
    void organizationCreatorBecomesAdminAndNamesAreUnique() {
        OrganizationRepository organizations = mock(OrganizationRepository.class);
        MembershipRepository memberships = mock(MembershipRepository.class);
        OrganizationService service = new OrganizationService(organizations, memberships);
        User creator = new User("creator", "hash");
        when(organizations.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Organization organization = service.createOrganization("  Alpha  ", creator);
        assertEquals("Alpha", organization.getName());
        verify(memberships).save(argThat(m -> m.getUser() == creator
                && m.getOrganization() == organization && m.getRole() == Role.ADMIN));
        when(organizations.findByName("Alpha")).thenReturn(Optional.of(organization));
        assertThrows(OrganizationAlreadyExistsException.class,
                () -> service.createOrganization(" Alpha ", creator));
    }
}
