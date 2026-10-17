package dev.tenantguard.tenantguard_lab.service;

import dev.tenantguard.tenantguard_lab.entity.Document;
import dev.tenantguard.tenantguard_lab.entity.Organization;
import dev.tenantguard.tenantguard_lab.entity.Role;
import dev.tenantguard.tenantguard_lab.entity.User;
import dev.tenantguard.tenantguard_lab.exception.DocumentNotFoundException;
import dev.tenantguard.tenantguard_lab.repository.DocumentRepository;
import dev.tenantguard.tenantguard_lab.repository.OrganizationRepository;
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
class DocumentServiceTest {
    @Mock DocumentRepository documents;
    @Mock OrganizationRepository organizations;
    @Mock AuthService auth;
    @Mock MembershipService memberships;

    DocumentService service;
    Organization organization;
    User author;
    Document document;

    @BeforeEach
    void setUp() {
        service = new DocumentService(documents, organizations, auth, memberships);
        organization = new Organization("Alpha");
        author = new User("author", "hash");
        document = new Document("Old", "Body", organization, author);
    }

    @Test
    void viewerCanReadOnlyDocumentsInTheirOrganization() {
        when(documents.findByOrganization_Id(1L)).thenReturn(List.of(document));
        when(documents.findByOrganization_IdAndId(1L, 7L)).thenReturn(Optional.of(document));

        assertEquals(List.of(document), service.getDocuments(1L, 10L));
        assertSame(document, service.getDocumentById(1L, 7L, 10L));
        verify(memberships, times(2)).requireRole(10L, 1L, Role.VIEWER);
        verify(documents, never()).findById(anyLong());
    }

    @Test
    void editorCanCreateAndUpdateDocumentWithoutChangingAuthor() {
        when(organizations.findById(1L)).thenReturn(Optional.of(organization));
        when(auth.findUserById(10L)).thenReturn(author);
        when(documents.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documents.findByOrganization_IdAndId(1L, 7L)).thenReturn(Optional.of(document));

        Document created = service.createDocument(1L, 10L, "New", "Content");
        Document updated = service.updateDocument(1L, 7L, 10L, "Updated", "Changed");

        assertEquals("New", created.getTitle());
        assertSame(author, created.getCreatedBy());
        assertEquals("Updated", updated.getTitle());
        assertEquals("Changed", updated.getContent());
        assertSame(author, updated.getCreatedBy());
        verify(memberships, times(2)).requireRole(10L, 1L, Role.EDITOR);
    }

    @Test
    void editorCanDeleteOnlyDocumentFoundWithinOrganization() {
        when(documents.findByOrganization_IdAndId(1L, 7L)).thenReturn(Optional.of(document));
        service.deleteDocument(1L, 7L, 10L);
        verify(documents).delete(document);
        verify(documents, never()).findById(anyLong());
    }

    @Test
    void documentInAnotherOrganizationIsNotFound() {
        when(documents.findByOrganization_IdAndId(1L, 7L)).thenReturn(Optional.empty());
        assertThrows(DocumentNotFoundException.class, () -> service.getDocumentById(1L, 7L, 10L));
        assertThrows(DocumentNotFoundException.class, () -> service.updateDocument(1L, 7L, 10L, "X", "Y"));
        assertThrows(DocumentNotFoundException.class, () -> service.deleteDocument(1L, 7L, 10L));
        verify(documents, never()).delete(any());
    }

    @Test
    void accessIsCheckedBeforeDocumentLookup() {
        doThrow(new AccessDeniedException("Access denied."))
                .when(memberships).requireRole(10L, 1L, Role.EDITOR);
        assertThrows(AccessDeniedException.class, () -> service.updateDocument(1L, 7L, 10L, "X", "Y"));
        verifyNoInteractions(documents);
    }
}
