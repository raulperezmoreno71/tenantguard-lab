package dev.tenantguard.tenantguard_lab.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsAuthenticationAuthorizationAndMissingResources() {
        assertEquals(HttpStatus.UNAUTHORIZED, handler.handleInvalidCredentials(new InvalidCredentialsException()).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, handler.handleAccessDenied(new AccessDeniedException("denied")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleDocumentNotFound(new DocumentNotFoundException()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleResourceNotFound(new ResourceNotFoundException("missing")).getStatusCode());
    }

    @Test
    void mapsValidationAndConflictsWithoutLeakingDatabaseDetails() {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleBadRequest(new IllegalArgumentException("bad")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleUserAlreadyExists(new UserAlreadyExistsException("name")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleOrganizationAlreadyExists(new OrganizationAlreadyExistsException()).getStatusCode());
        var conflict = handler.handleDataConflict(new DataIntegrityViolationException("sensitive database detail"));
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals("Resource conflict.", conflict.getBody());
        assertEquals("already exists", handler.handleConflict(new ConflictException("already exists")).getBody());
    }
}
