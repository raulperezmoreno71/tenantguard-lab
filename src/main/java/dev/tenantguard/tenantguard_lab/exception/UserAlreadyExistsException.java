package dev.tenantguard.tenantguard_lab.exception;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String username) {super(username + " already registered.");}
}
