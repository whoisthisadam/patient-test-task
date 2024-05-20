package com.kasperovich.patients.exception;

public class KeycloakException extends Exception{
    public KeycloakException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakException(String message) {
        super(message);
    }

    public static class RecurringException extends KeycloakException {

        public RecurringException(String message) {
            super(message);
        }

    }
}
