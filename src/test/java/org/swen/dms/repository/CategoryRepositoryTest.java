package org.swen.dms.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.swen.dms.entity.Category;
import org.swen.dms.repository.jpa.CategoryRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository repo;

    @Test
    void findByName_Success() {
        // Arrange
        Category cat = new Category("Invoices", "All bills");
        entityManager.persist(cat);
        entityManager.flush();

        // Act
        Optional<Category> found = repo.findByName("Invoices");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Invoices");
    }

    @Test
    void findByName_NotFound() {
        // Act
        Optional<Category> found = repo.findByName("NonExistent");

        // Assert
        assertThat(found).isEmpty();
    }
}