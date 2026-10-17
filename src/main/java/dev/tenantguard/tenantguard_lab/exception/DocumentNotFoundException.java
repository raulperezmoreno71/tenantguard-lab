package dev.tenantguard.tenantguard_lab.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() {
        super("Document not found.");
    }
}
