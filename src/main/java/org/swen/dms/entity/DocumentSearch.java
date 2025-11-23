package org.swen.dms.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "documents") // This creates an index called 'documents'
public class DocumentSearch {

    @Id
    private String id; // We will use the same ID as the PostgreSQL DB

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content; // This will store the OCR text

    // --- Constructors ---
    public DocumentSearch() {}

    public DocumentSearch(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
