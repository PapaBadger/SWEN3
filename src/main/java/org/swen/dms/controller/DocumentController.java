package org.swen.dms.controller;

import org.swen.dms.entity.Document;
import org.swen.dms.service.DocumentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing endpoints for managing {@link Document} entities.
 * <p>
 * Provides CRUD operations over HTTP:
 * <ul>
 *   <li>POST /documents → create a new document</li>
 *   <li>GET /documents → list all or filter by title</li>
 *   <li>GET /documents/{id} → fetch by ID</li>
 *   <li>PUT /documents/{id} → update an existing document</li>
 *   <li>DELETE /documents/{id} → remove a document</li>
 * </ul>
 * Delegates business logic to {@link DocumentService}.
 */

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    public Document create(@RequestBody Document doc) {
        return service.create(doc);
    }

    @GetMapping
    public List<Document> findAll(@RequestParam(value = "title", required = false) String title) {
        if (title != null && !title.isBlank()) {
            return service.findByTitle(title);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Document findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public Document update(@PathVariable Long id, @RequestBody Document update) {
        return service.update(id, update);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping(path = "/upload", consumes = {"multipart/form-data"})
    public List<Document> uploadDocuments(
            @RequestParam("files") org.springframework.web.multipart.MultipartFile[] files,
            @RequestParam(value = "titlePrefix", required = false) String titlePrefix
    ) {
        java.util.List<Document> saved = new java.util.ArrayList<>();
        int idx = 1;
        for (org.springframework.web.multipart.MultipartFile file : files) {
            try {
                Document d = new Document();
                String title = (titlePrefix == null || titlePrefix.isBlank())
                        ? file.getOriginalFilename()
                        : titlePrefix + " " + idx + " - " + file.getOriginalFilename();
                d.setTitle(title);
                // Store file content as text for simplicity. Adjust if you have a blob/bytes field.
                d.setContent(new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8));
                saved.add(service.create(d));
                idx++;
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to read uploaded file: " + file.getOriginalFilename(), e);
            }
        }
        return saved;
    }
}
