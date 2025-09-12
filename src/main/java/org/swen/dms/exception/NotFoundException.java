package org.swen.dms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when an entity cannot be found.
 * <p>
 * Annotated with {@link ResponseStatus} to automatically return
 * a 404 Not Found HTTP response to clients when this exception is raised.
 */

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
