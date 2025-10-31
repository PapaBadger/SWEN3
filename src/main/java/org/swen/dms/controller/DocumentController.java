package org.swen.dms.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "title", required = false) String title) {
        return service.uploadDocument(file, title);
    }

    @GetMapping
    public List<Document> list() {
        return service.findAll();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        return service.downloadDocument(id);
    }


    @PutMapping("/{id}")
    public Document update(@PathVariable Long id, @RequestBody Document update) {
        return service.update(id, update);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
