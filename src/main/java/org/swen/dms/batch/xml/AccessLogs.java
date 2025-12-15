package org.swen.dms.batch.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "accessLogs")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccessLogs {

    @XmlElement(name = "entry")
    private List<AccessLogEntry> entries;

    public List<AccessLogEntry> getEntries() { return entries; }
    public void setEntries(List<AccessLogEntry> entries) { this.entries = entries; }
}