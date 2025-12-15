package org.swen.dms.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swen.dms.entity.Category;
import org.swen.dms.entity.Document;
import org.swen.dms.repository.jpa.CategoryRepository;
import org.swen.dms.repository.jpa.DocumentRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepo;

    @Mock
    private DocumentRepository documentRepo;

    @InjectMocks
    private CategoryServiceImpl service;

    /**
     * Verifies that getAllCategories simply delegates to the repository.
     */
    @Test
    void getAllCategories_ReturnsList() {
        // Arrange
        List<Category> categories = List.of(new Category("A", "Desc A"), new Category("B", "Desc B"));
        when(categoryRepo.findAll()).thenReturn(categories);

        // Act
        List<Category> result = service.getAllCategories();

        // Assert
        assertThat(result).hasSize(2);
        verify(categoryRepo).findAll();
    }

    /**
     * Verifies that createCategory saves and returns a new category
     * when the name is unique.
     */
    @Test
    void createCategory_Success() {
        // Arrange
        String name = "Invoices";
        String desc = "All bills";

        when(categoryRepo.findByName(name)).thenReturn(Optional.empty()); // No duplicate exists

        // Mock the save behavior to return the object passed to it
        when(categoryRepo.save(any(Category.class))).thenAnswer(i -> {
            Category c = i.getArgument(0);
            c.setId(1L); // Simulate DB generating an ID
            return c;
        });

        // Act
        Category created = service.createCategory(name, desc);

        // Assert
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo(name);
        verify(categoryRepo).save(any(Category.class));
    }

    /**
     * Verifies that createCategory throws IllegalArgumentException
     * when a category with the same name already exists.
     */
    @Test
    void createCategory_DuplicateName_ThrowsException() {
        // Arrange
        when(categoryRepo.findByName("Invoices")).thenReturn(Optional.of(new Category()));

        // Act & Assert
        assertThatThrownBy(() -> service.createCategory("Invoices", "Desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepo, never()).save(any());
    }

    /**
     * Verifies that assignCategoryToDoc links the entities and saves the document.
     */
    @Test
    void assignCategoryToDoc_Success() {
        // Arrange
        Long catId = 10L;
        Long docId = 20L;

        Category category = new Category("Work", "Stuff");
        category.setId(catId);

        Document doc = new Document();
        doc.setId(docId);
        // Ensure the Set is initialized (if your Entity doesn't do it in constructor)
        doc.setCategories(new HashSet<>());

        when(categoryRepo.findById(catId)).thenReturn(Optional.of(category));
        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));

        // Act
        service.assignCategoryToDoc(catId, docId);

        // Assert
        // Verify that the document was saved
        verify(documentRepo).save(doc);

        // Verify the logic actually modified the document's category list
        assertThat(doc.getCategories()).contains(category);
    }

    /**
     * Verifies failure when the Category ID does not exist.
     */
    @Test
    void assignCategoryToDoc_CategoryNotFound() {
        // Arrange
        when(categoryRepo.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.assignCategoryToDoc(10L, 20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");

        verify(documentRepo, never()).save(any());
    }

    /**
     * Verifies failure when the Document ID does not exist.
     */
    @Test
    void assignCategoryToDoc_DocumentNotFound() {
        // Arrange
        when(categoryRepo.findById(10L)).thenReturn(Optional.of(new Category()));
        when(documentRepo.findById(20L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.assignCategoryToDoc(10L, 20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Document not found");
    }

    /**
     * Verifies that removeCategoryFromDoc unlinks the entities and saves the document.
     */
    @Test
    void removeCategoryFromDoc_Success() {
        // Arrange
        Long catId = 10L;
        Long docId = 20L;

        Category category = new Category("Work", "Stuff");
        category.setId(catId);

        Document doc = new Document();
        doc.setId(docId);
        doc.setCategories(new HashSet<>());
        doc.getCategories().add(category); // Pre-link them

        when(categoryRepo.findById(catId)).thenReturn(Optional.of(category));
        when(documentRepo.findById(docId)).thenReturn(Optional.of(doc));

        // Act
        service.removeCategoryFromDoc(catId, docId);

        // Assert
        verify(documentRepo).save(doc);
        assertThat(doc.getCategories()).doesNotContain(category);
    }
}