package org.swen.dms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swen.dms.entity.Category;
import org.swen.dms.service.CategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:4200") // Best practice: specific origin
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // 1. Get all available categories
    @GetMapping
    public List<Category> getAllCategories() {
        return service.getAllCategories();
    }

    // 2. Create a new category
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestParam String name, @RequestParam(required = false) String description) {
        Category created = service.createCategory(name, description);
        return ResponseEntity.ok(created);
    }

    // 3. Assign a category to a document
    @PostMapping("/{categoryId}/assign/{docId}")
    public ResponseEntity<?> assignCategory(@PathVariable Long categoryId, @PathVariable Long docId) {
        service.assignCategoryToDoc(categoryId, docId);
        // Returning JSON is friendlier for the frontend than a raw 200 OK
        return ResponseEntity.ok(Map.of("message", "Category assigned successfully"));
    }

    // 4. Remove a category from a document
    @DeleteMapping("/{categoryId}/remove/{docId}")
    public ResponseEntity<?> removeCategory(@PathVariable Long categoryId, @PathVariable Long docId) {
        service.removeCategoryFromDoc(categoryId, docId);
        return ResponseEntity.ok(Map.of("message", "Category removed successfully"));
    }
}