package org.swen.dms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swen.dms.entity.Category;
import org.swen.dms.entity.Document;
import org.swen.dms.exception.NotFoundException; // Use your existing custom exception if available, else RuntimeException
import org.swen.dms.repository.jpa.CategoryRepository;
import org.swen.dms.repository.jpa.DocumentRepository;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    private final DocumentRepository documentRepo;

    public CategoryServiceImpl(CategoryRepository categoryRepo, DocumentRepository documentRepo) {
        this.categoryRepo = categoryRepo;
        this.documentRepo = documentRepo;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @Override
    @Transactional
    public Category createCategory(String name, String description) {
        // Logic extracted from Controller: Check for duplicates
        if (categoryRepo.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Category already exists: " + name);
        }
        return categoryRepo.save(new Category(name, description));
    }

    @Override
    @Transactional
    public void assignCategoryToDoc(Long categoryId, Long documentId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        Document doc = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        // Logic extracted from Controller: Add helper
        doc.addCategory(category);
        documentRepo.save(doc);
    }

    @Override
    @Transactional
    public void removeCategoryFromDoc(Long categoryId, Long documentId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        Document doc = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        // Logic extracted from Controller: Remove helper
        doc.removeCategory(category);
        documentRepo.save(doc);
    }
}