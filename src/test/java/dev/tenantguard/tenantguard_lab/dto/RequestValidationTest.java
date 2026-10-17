package dev.tenantguard.tenantguard_lab.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidDocumentMemberAndOrganizationRequests() {
        assertFalse(validator.validate(new CreateDocumentRequest(" ", "body")).isEmpty());
        assertFalse(validator.validate(new CreateDocumentRequest("x".repeat(151), "body")).isEmpty());
        assertFalse(validator.validate(new AddMemberRequest("user", null)).isEmpty());
        assertFalse(validator.validate(new UpdateMemberRoleRequest(null)).isEmpty());
        assertFalse(validator.validate(new CreateOrganizationRequest("x".repeat(101))).isEmpty());
        assertTrue(validator.validate(new CreateDocumentRequest("title", "")).isEmpty());
    }
}
