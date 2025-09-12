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
@RequestMapping("/documents")
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
}
