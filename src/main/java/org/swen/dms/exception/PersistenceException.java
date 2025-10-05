package org.swen.dms.exception;

/**
 * Exception thrown when a persistence or database operation fails.
 *
 * Used within the service layer to isolate data access errors from
 * business logic and provide clean, layer-specific error handling.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
