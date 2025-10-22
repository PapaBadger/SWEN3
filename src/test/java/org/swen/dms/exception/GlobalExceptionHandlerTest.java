package org.swen.dms.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.swen.dms.messaging.DocumentEventPublisher;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlePersistenceException() {
        // Arrange
        PersistenceException ex = new PersistenceException("Database error");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handlePersistence(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsKeys("timestamp", "code", "message");
        assertThat(response.getBody().get("code")).isEqualTo("PERSISTENCE_ERROR");
    }

    @Test
    void handleMessagingException() {
        // Arrange
        DocumentEventPublisher.MessagingException ex =
                new DocumentEventPublisher.MessagingException("RabbitMQ error", new RuntimeException());

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleMessaging(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody().get("code")).isEqualTo("MESSAGING_ERROR");
    }

    @Test
    void handleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("code")).isEqualTo("UNEXPECTED_ERROR");
    }
}