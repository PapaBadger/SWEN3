package org.swen.dms.batch.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class AccessLogEntry {

    @XmlElement(name = "documentId")
    private Long documentId;

    @XmlElement(name = "accessCount")
    private Long accessCount;

    // Getters and Setters
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getAccessCount() { return accessCount; }
    public void setAccessCount(Long accessCount) { this.accessCount = accessCount; }
}