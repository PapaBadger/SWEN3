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
    private String bucket;
    private String fileKey;

    public DocumentCreatedEvent() {}

    public DocumentCreatedEvent(Long id, String title, Instant createdAt, String bucket, String fileKey) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.bucket = bucket;
        this.fileKey = fileKey;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Instant getCreatedAt() { return createdAt; }
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }
}
