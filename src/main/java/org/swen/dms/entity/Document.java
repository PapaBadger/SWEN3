package org.swen.dms.entity;

import jakarta.persistence.*;

/**
 * Entity class representing a document in the system.
 * <p>
 * Each instance maps to a row in the {@code document} table in PostgreSQL.
 * Fields correspond to columns and are persisted automatically by JPA/Hibernate.
 */

@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
