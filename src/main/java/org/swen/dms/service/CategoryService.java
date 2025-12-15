package org.swen.dms.service;

import org.swen.dms.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category createCategory(String name, String description);
    void assignCategoryToDoc(Long categoryId, Long documentId);
    void removeCategoryFromDoc(Long categoryId, Long documentId);
}