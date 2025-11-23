package org.swen.dms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swen.dms.entity.Category;
import org.swen.dms.entity.Document;
import org.swen.dms.repository.jpa.CategoryRepository;
import org.swen.dms.repository.jpa.DocumentRepository;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryRepository categoryRepo;
    private final DocumentRepository documentRepo;

    public CategoryController(CategoryRepository categoryRepo, DocumentRepository documentRepo) {
        this.categoryRepo = categoryRepo;
        this.documentRepo = documentRepo;
    }

    // 1. Get all available categories
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    // 2. Create a new category (e.g., "Invoices")
    @PostMapping
    public Category createCategory(@RequestParam String name, @RequestParam(required = false) String description) {
        if (categoryRepo.findByName(name).isPresent()) {
            throw new RuntimeException("Category already exists: " + name);
        }
        return categoryRepo.save(new Category(name, description));
    }

    // 3. Assign a category to a document
    @PostMapping("/{categoryId}/assign/{docId}")
    public ResponseEntity<Document> assignCategory(@PathVariable Long categoryId, @PathVariable Long docId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Document doc = documentRepo.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Helper method we added in Document.java
        doc.addCategory(category);

        return ResponseEntity.ok(documentRepo.save(doc));
    }

    // 4. Remove a category from a document
    @DeleteMapping("/{categoryId}/remove/{docId}")
    public ResponseEntity<Document> removeCategory(@PathVariable Long categoryId, @PathVariable Long docId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Document doc = documentRepo.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        doc.removeCategory(category);

        return ResponseEntity.ok(documentRepo.save(doc));
    }
}