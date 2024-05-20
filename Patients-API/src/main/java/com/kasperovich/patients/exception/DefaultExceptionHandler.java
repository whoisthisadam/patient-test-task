package com.kasperovich.patients.exception;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
@Slf4j
public class DefaultExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorContainer> handle(@Nonnull final Exception exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(ErrorContainer
                .builder()
                .exceptionId(UUID.randomUUID().toString())
                .errorMessage("General error")
                .errorCode(2)
                .e(exception.getClass().getName())
                .build(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ErrorContainer> handle(@Nonnull final NotFoundException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(ErrorContainer
                .builder()
                .exceptionId(UUID.randomUUID().toString())
                .errorMessage(exception.getMessage())
                .errorCode(2)
                .e(exception.getClass().getName())
                .build(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<ErrorContainer> handle(@Nonnull final DataIntegrityViolationException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(ErrorContainer
                .builder()
                .exceptionId(UUID.randomUUID().toString())
                .errorMessage("This record already exists")
                .errorCode(3)
                .e(exception.getClass().getName())
                .build(),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = KeycloakException.class)
    public ResponseEntity<ErrorContainer> handle(@Nonnull final KeycloakException exception) {
        log.error(exception.getMessage(), exception.getCause());
        HttpStatus httpStatus = HttpStatus.BAD_GATEWAY;
        int errorCode = 4;
        if(exception instanceof KeycloakException.RecurringException){
            httpStatus = HttpStatus.CONFLICT;
            errorCode = 3;
        }
        return new ResponseEntity<>(ErrorContainer
                .builder()
                .exceptionId(UUID.randomUUID().toString())
                .errorMessage(exception.getMessage())
                .errorCode(errorCode)
                .e(exception.getClass().getName())
                .build(),
                httpStatus);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ErrorContainer> handle(@Nonnull final AccessDeniedException exception) {
        log.error(exception.getMessage(), exception.getCause());
        return new ResponseEntity<>(ErrorContainer
                .builder()
                .exceptionId(UUID.randomUUID().toString())
                .errorMessage("Access Denied")
                .errorCode(5)
                .e(exception.getClass().getName())
                .build(),
                HttpStatus.FORBIDDEN);
    }


}
