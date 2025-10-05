package org.swen.dms.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.swen.dms.messaging.DocumentEventPublisher.MessagingException;

import java.time.Instant;
import java.util.Map;

/**
 * Centralized exception handler for the entire REST API.
 *
 * Converts thrown exceptions into meaningful HTTP responses and
 * provides unified logging for persistence, messaging, and unexpected errors.
 *
 * Registered as a {@link org.springframework.web.bind.annotation.RestControllerAdvice}
 * to automatically intercept exceptions from all controllers.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Map<String,Object>> handlePersistence(PersistenceException ex) {
        log.error("Persistence error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "code", "PERSISTENCE_ERROR",
                        "message", ex.getMessage()
                ));
    }


    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<Map<String,Object>> handleMessaging(MessagingException ex) {
        log.error("Messaging error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(err("MESSAGING_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGeneric(Exception ex) {
        log.error("Unhandled error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(err("UNEXPECTED_ERROR", "Unexpected server error"));
    }

    private Map<String,Object> err(String code, String msg) {
        return Map.of("timestamp", Instant.now().toString(), "code", code, "message", msg);
    }
}
