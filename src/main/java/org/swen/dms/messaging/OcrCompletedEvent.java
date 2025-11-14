package org.swen.dms.messaging;

public class OcrCompletedEvent {

    private Long documentId;

    public OcrCompletedEvent() {
        // Default-Konstruktor für Jackson
    }

    public OcrCompletedEvent(Long documentId) {
        this.documentId = documentId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return "OcrCompletedEvent{documentId=" + documentId + '}';
    }
}
