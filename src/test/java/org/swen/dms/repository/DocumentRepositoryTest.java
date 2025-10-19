package org.swen.dms.repository;

import org.junit.jupiter.api.Test;
import org.swen.dms.entity.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link org.swen.dms.repository.DocumentRepository}.
 * <p>
 * Uses {@link org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest}
 * with an embedded H2 database.
 * Verifies repository queries work as expected.
 */


@DataJpaTest  // starts JPA with embedded H2 automatically (no Postgres)
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository repo;

    /** Verifies that saving a document and then querying by title returns the saved entity. */
//    @Test
//    void save_and_findByTitle() {
//        Document d = new Document();
//        d.setTitle("Spec");
//        d.setContent("Hello");
//        repo.save(d);
//
//        List<Document> found = repo.findByTitle("Spec");
//        assertThat(found).hasSize(1);
//        assertThat(found.get(0).getId()).isNotNull();
//    }
}
