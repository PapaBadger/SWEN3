package org.swen.dms.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static org.swen.dms.config.RabbitConfig.*;

/**
 * Publishes domain events related to documents to RabbitMQ.
 *
 * Responsible for converting {@link DocumentCreatedEvent} objects
 * and sending them to the configured exchange and routing key.
 *
 * Provides centralized error handling and logging for messaging failures.
 */

@Component
public class DocumentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(DocumentEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public DocumentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDocumentCreated(DocumentCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE_DOCS, ROUTING_DOC_CREATED, event);
            log.info("Published DocumentCreatedEvent: id={}, title={}", event.getId(), event.getTitle());
        } catch (Exception ex) {
            log.error("Failed to publish DocumentCreatedEvent for id={}: {}", event.getId(), ex.getMessage(), ex);
            // Decide: swallow or rethrow as custom MessagingException
            throw new MessagingException("Unable to publish document event", ex);
        }
    }

    public void publishDocumentUpdated(DocumentUpdatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE_DOCS, ROUTING_DOC_UPDATED, event);
            log.info("Published DocumentUpdatedEvent: id={}, {} -> {}",
                    event.getId(), event.getTitleBefore(), event.getTitleAfter());
        } catch (Exception ex) {
            log.error("Failed to publish DocumentUpdatedEvent for id={}: {}", event.getId(), ex.getMessage(), ex);
            throw new MessagingException("Unable to publish document event", ex);
        }
    }

    /** Layer-specific exception for messaging failures */
    public static class MessagingException extends RuntimeException {
        public MessagingException(String msg, Throwable cause) { super(msg, cause); }
    }
}
