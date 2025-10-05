package org.swen.dms.messaging;

import java.time.Instant;

/**
 * Data Transfer Object representing a document creation event.
 *
 * Sent to RabbitMQ when a new document is created in the system.
 * Contains minimal metadata (ID, title, timestamp) describing the uploaded document.
 *
 * Consumed by the OCR worker to trigger or simulate OCR processing.
 */

public class DocumentCreatedEvent {
    private Long id;
    private String title;
    private Instant createdAt;

    public DocumentCreatedEvent() {}

    public DocumentCreatedEvent(Long id, String title, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
