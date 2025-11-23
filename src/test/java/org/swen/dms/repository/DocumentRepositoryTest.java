package org.swen.dms.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.swen.dms.entity.Document;
import org.swen.dms.repository.jpa.DocumentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DocumentRepository}.
 * <p>
 * Uses {@link org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest}
 * with an embedded H2 database.
 * Verifies repository queries work as expected.
 */

@DataJpaTest
class DocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository repo;

    private Document createAndSaveDocument(String title, String fileKey) {
        Document doc = new Document();
        doc.setTitle(title);
        doc.setFileKey(fileKey);
        doc.setContentType("application/pdf");
        doc.setFileSize(1024L);
        doc.setUploadedAt(LocalDateTime.now());
        return entityManager.persistAndFlush(doc);
    }

    /**
     * Verifies that findByTitle returns all documents with the matching title.
     */
    @Test
    void findByTitle_ShouldReturnMatchingDocuments() {
        // Arrange
        Document doc1 = createAndSaveDocument("Test Document", "key1");
        Document doc2 = createAndSaveDocument("Another Document", "key2");
        Document doc3 = createAndSaveDocument("Test Document", "key3");

        // Act
        List<Document> found = repo.findByTitle("Test Document");

        // Assert
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Document::getId)
                .containsExactlyInAnyOrder(doc1.getId(), doc3.getId()); // Changed from doc2 to doc3
    }

    /**
     * Verifies that findByTitle returns an empty list when no documents match the title.
     */
    @Test
    void findByTitle_NoMatches_ShouldReturnEmptyList() {
        // Arrange
        createAndSaveDocument("Test Document", "key1");

        // Act
        List<Document> found = repo.findByTitle("Non-existent");

        // Assert
        assertThat(found).isEmpty();
    }

    /**
     * Verifies that existsByTitle returns true when a document with the given title exists.
     */
    @Test
    void existsByTitle_ShouldReturnTrueWhenExists() {
        // Arrange
        createAndSaveDocument("Existing Document", "key1");

        // Act
        boolean exists = repo.existsByTitle("Existing Document");

        // Assert
        assertThat(exists).isTrue();
    }

    /**
     * Verifies that existsByTitle returns false when no document with the given title exists.
     */
    @Test
    void existsByTitle_ShouldReturnFalseWhenNotExists() {
        // Act
        boolean exists = repo.existsByTitle("Non-existent Document");

        // Assert
        assertThat(exists).isFalse();
    }

    /**
     * Verifies that documents can be saved and retrieved by ID.
     */
    @Test
    void saveAndFindById_ShouldWork() {
        // Arrange
        Document doc = createAndSaveDocument("Test", "key1");

        // Act
        Optional<Document> found = repo.findById(doc.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test");
        assertThat(found.get().getFileKey()).isEqualTo("key1");
    }

    /**
     * Verifies that findAll returns all documents in the repository.
     */
    @Test
    void findAll_ShouldReturnAllDocuments() {
        // Arrange
        createAndSaveDocument("Doc1", "key1");
        createAndSaveDocument("Doc2", "key2");

        // Act
        List<Document> all = repo.findAll();

        // Assert
        assertThat(all).hasSize(2);
    }

    /**
     * Verifies that deleteById removes a document from the repository.
     */
    @Test
    void delete_ShouldRemoveDocument() {
        // Arrange
        Document doc = createAndSaveDocument("To Delete", "key1");

        // Act
        repo.deleteById(doc.getId());
        entityManager.flush();

        // Assert
        Optional<Document> found = repo.findById(doc.getId());
        assertThat(found).isEmpty();
    }
}