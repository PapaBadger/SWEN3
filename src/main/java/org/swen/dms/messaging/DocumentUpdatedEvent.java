package org.swen.dms.messaging;

import java.time.Instant;

/** Fired when a document's metadata changes (e.g., title). */
public class DocumentUpdatedEvent {
    private Long id;
    private String titleBefore;
    private String titleAfter;
    private Instant updatedAt;

    public DocumentUpdatedEvent() {}

    public DocumentUpdatedEvent(Long id, String titleBefore, String titleAfter, Instant updatedAt) {
        this.id = id;
        this.titleBefore = titleBefore;
        this.titleAfter = titleAfter;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitleBefore() { return titleBefore; }
    public String getTitleAfter() { return titleAfter; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setTitleBefore(String titleBefore) { this.titleBefore = titleBefore; }
    public void setTitleAfter(String titleAfter) { this.titleAfter = titleAfter; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
