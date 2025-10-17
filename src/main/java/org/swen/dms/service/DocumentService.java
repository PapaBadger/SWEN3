package org.swen.dms.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.swen.dms.entity.Document;

import java.util.List;

/**
 * Service interface defining business operations on {@link Document} entities.
 * <p>
 * Abstracts away data access logic and provides a clear contract for higher layers
 * (controllers). This makes the application easier to maintain, test, and extend.
 */

public interface DocumentService {
    Document create(Document doc);
    List<Document> findAll();
    Document findById(Long id);
    Document update(Long id, Document update);
    void delete(Long id);
    ResponseEntity<?> uploadDocuments(MultipartFile file);

    // optional example query
    List<Document> findByTitle(String title);
}
