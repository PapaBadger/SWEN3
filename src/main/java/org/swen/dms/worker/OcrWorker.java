package org.swen.dms.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.swen.dms.messaging.DocumentCreatedEvent;

import static org.swen.dms.config.RabbitConfig.QUEUE_OCR;

/**
 * Simulated OCR worker service.
 *
 * Acts as a RabbitMQ message listener, consuming messages from the {@code docs.ocr.queue}.
 * Currently logs receipt of {@link DocumentCreatedEvent} objects to represent future OCR processing.
 *
 * In later sprints, this component can be expanded to perform actual text extraction.
 */

@Component
public class OcrWorker {
    private static final Logger log = LoggerFactory.getLogger(OcrWorker.class);

    /** Dummy consumer – logs receipt (placeholder for real OCR) */
    @RabbitListener(queues = QUEUE_OCR)
    public void handle(DocumentCreatedEvent event) {
        log.info("OCR worker received event: id={}, title={}", event.getId(), event.getTitle());
        // TODO: implement OCR later
    }
}
