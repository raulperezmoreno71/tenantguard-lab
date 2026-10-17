package dev.tenantguard.tenantguard_lab.config;

import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.repository.DocumentRepository;
import dev.tenantguard.tenantguard_lab.repository.MembershipRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
import dev.tenantguard.tenantguard_lab.repository.UserRepository;
import dev.tenantguard.tenantguard_lab.service.AuthService;
import dev.tenantguard.tenantguard_lab.service.DocumentService;
import dev.tenantguard.tenantguard_lab.service.MembershipService;
import dev.tenantguard.tenantguard_lab.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LabDataInitializerTest {
    private static final String TEST_PASSWORD = "test-only-placeholder";
    UserRepository users = mock(UserRepository.class);
    OrganizationRepository organizations = mock(OrganizationRepository.class);
    MembershipRepository memberships = mock(MembershipRepository.class);
    DocumentRepository documents = mock(DocumentRepository.class);
    AuthService auth = mock(AuthService.class);
    OrganizationService organizationService = mock(OrganizationService.class);
    MembershipService membershipService = mock(MembershipService.class);
    DocumentService documentService = mock(DocumentService.class);
    LabDataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new LabDataInitializer(users, organizations, memberships, documents,
                auth, organizationService, membershipService, documentService,
                TEST_PASSWORD, TEST_PASSWORD, TEST_PASSWORD, TEST_PASSWORD, TEST_PASSWORD);
    }

    @Test
    void existingDataIsNeverChangedOrCompletedSilently() throws Exception {
        when(organizations.count()).thenReturn(1L);

        initializer.run(new DefaultApplicationArguments());

        verifyNoInteractions(auth, organizationService, membershipService, documentService);
    }

    @Test
    void emptyDatabaseGetsExpectedUsersRolesAndDocuments() throws Exception {
        User alice = user(1L, "alice");
        User bob = user(2L, "bob");
        User charlie = user(3L, "charlie");
        User david = user(4L, "david");
        User eve = user(5L, "eve");
        Organization acme = organization(10L, "Acme Corp");
        Organization globex = organization(20L, "Globex Corp");
        when(auth.register(eq("alice"), anyString())).thenReturn(alice);
        when(auth.register(eq("bob"), anyString())).thenReturn(bob);
        when(auth.register(eq("charlie"), anyString())).thenReturn(charlie);
        when(auth.register(eq("david"), anyString())).thenReturn(david);
        when(auth.register(eq("eve"), anyString())).thenReturn(eve);
        when(organizationService.createOrganization("Acme Corp", alice)).thenReturn(acme);
        when(organizationService.createOrganization("Globex Corp", david)).thenReturn(globex);

        initializer.run(new DefaultApplicationArguments());

        verify(auth, times(5)).register(anyString(), anyString());
        verify(organizationService).createOrganization("Acme Corp", alice);
        verify(organizationService).createOrganization("Globex Corp", david);
        verify(membershipService).addMember(10L, 1L, "bob", Role.EDITOR);
        verify(membershipService).addMember(10L, 1L, "charlie", Role.VIEWER);
        verify(membershipService).addMember(20L, 4L, "eve", Role.VIEWER);
        verify(documentService, times(3)).createDocument(eq(10L), anyLong(), startsWith("ACME-"), anyString());
        verify(documentService, times(3)).createDocument(eq(20L), eq(4L), startsWith("GLOBEX-"), anyString());
    }

    private User user(Long id, String username) {
        User user = new User(username, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Organization organization(Long id, String name) {
        Organization organization = new Organization(name);
        ReflectionTestUtils.setField(organization, "id", id);
        return organization;
    }
}
