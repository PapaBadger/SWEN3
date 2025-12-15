package org.swen.dms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String fileKey;

    @Column(nullable = false)
    private String contentType;

    @Column
    private Long fileSize;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ocrText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ocrSummaryText;

    private Long accessCount = 0L;

    @ManyToMany(fetch = FetchType.EAGER) // Eager so we see tags immediately when loading a doc
    @JoinTable(
            name = "document_categories",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    //just to test things constructors
    public Document() {
    }

    public Document(Long id, String title, String fileKey, String contentType, Long fileSize, LocalDateTime uploadedAt) {
        this.id = id;
        this.title = title;
        this.fileKey = fileKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    //Gett und Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public String getOcrSummaryText() { return ocrSummaryText; }
    public void setOcrSummaryText(String ocrSummaryText) { this.ocrSummaryText = ocrSummaryText; }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Long getAccessCount() { return accessCount; }

    public void setAccessCount(Long accessCount) { this.accessCount = accessCount; }

    public void addCategory(Category category) {
        this.categories.add(category);
        category.getDocuments().add(this);
    }

    public void removeCategory(Category category) {
        this.categories.remove(category);
        category.getDocuments().remove(this);
    }
}
