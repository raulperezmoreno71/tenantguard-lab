package dev.tenantguard.tenantguard_lab.exception;

public class OrganizationAlreadyExistsException extends RuntimeException{
    public OrganizationAlreadyExistsException() {super("Organization already exists.");}
}
