package org.swen.dms.repository.jpa;

import org.swen.dms.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


/**
 * Repository interface for managing {@link Document} persistence.
 * <p>
 * Extends {@link JpaRepository} to provide CRUD operations and query execution
 * without requiring explicit SQL. Additional finder methods can be declared
 * using Spring Data’s query derivation (e.g., {@code findByTitle}).
 */

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTitle(String title); // Spring auto-implements this
    boolean existsByTitle(String title);
}
