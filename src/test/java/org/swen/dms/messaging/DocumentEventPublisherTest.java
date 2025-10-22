package org.swen.dms.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.swen.dms.config.RabbitConfig.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link org.swen.dms.messaging.DocumentEventPublisher}.
 * <p>
 * Verifies that document events are properly published to RabbitMQ
 * and that messaging failures are handled correctly.
 */
@ExtendWith(MockitoExtension.class)
class DocumentEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DocumentEventPublisher publisher;

    /**
     * Verifies that publishDocumentCreated successfully sends the event
     * to the correct exchange and routing key.
     */
    @Test
    void publishDocumentCreated_Success() {
        // Arrange
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                1L, "Test Document", Instant.now(), "documents", "file-key-1"
        );

        // Act
        publisher.publishDocumentCreated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE_DOCS),
                eq(ROUTING_DOC_CREATED),
                eq(event)
        );
    }

    /**
     * Verifies that publishDocumentCreated throws MessagingException
     * when RabbitMQ communication fails.
     */
    @Test
    void publishDocumentCreated_RabbitMQFailure() {
        // Arrange
        DocumentCreatedEvent event = new DocumentCreatedEvent(
                1L, "Test Document", Instant.now(), "documents", "file-key-1"
        );

        // Use specific argument matchers to avoid ambiguity
        doThrow(new RuntimeException("RabbitMQ down"))
                .when(rabbitTemplate).convertAndSend(
                        eq(EXCHANGE_DOCS),
                        eq(ROUTING_DOC_CREATED),
                        any(DocumentCreatedEvent.class)
                );

        // Act & Assert
        assertThatThrownBy(() -> publisher.publishDocumentCreated(event))
                .isInstanceOf(DocumentEventPublisher.MessagingException.class)
                .hasMessageContaining("Unable to publish document event");
    }

    /**
     * Verifies that publishDocumentUpdated successfully sends the update event
     * to the correct exchange and routing key.
     */
    @Test
    void publishDocumentUpdated_Success() {
        // Arrange
        DocumentUpdatedEvent event = new DocumentUpdatedEvent(
                1L, "Old Title", "New Title", Instant.now()
        );

        // Act
        publisher.publishDocumentUpdated(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(EXCHANGE_DOCS),
                eq(ROUTING_DOC_UPDATED),
                eq(event)
        );
    }

    /**
     * Verifies that publishDocumentUpdated throws MessagingException
     * when RabbitMQ communication fails.
     */
    @Test
    void publishDocumentUpdated_RabbitMQFailure() {
        // Arrange
        DocumentUpdatedEvent event = new DocumentUpdatedEvent(
                1L, "Old Title", "New Title", Instant.now()
        );

        // Use specific argument matchers to avoid ambiguity
        doThrow(new RuntimeException("RabbitMQ down"))
                .when(rabbitTemplate).convertAndSend(
                        eq(EXCHANGE_DOCS),
                        eq(ROUTING_DOC_UPDATED),
                        any(DocumentUpdatedEvent.class)
                );

        // Act & Assert
        assertThatThrownBy(() -> publisher.publishDocumentUpdated(event))
                .isInstanceOf(DocumentEventPublisher.MessagingException.class)
                .hasMessageContaining("Unable to publish document event");
    }
}