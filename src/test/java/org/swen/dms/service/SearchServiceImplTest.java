package org.swen.dms.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.repository.search.DocumentSearchRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private DocumentSearchRepository repository;

    @InjectMocks
    private SearchServiceImpl service;

    /**
     * Verifies that searchDocuments returns the list provided by the repository.
     */
    @Test
    void searchDocuments_Success() {
        // Arrange
        String query = "invoice";
        DocumentSearch doc1 = new DocumentSearch();
        doc1.setId("1");
        doc1.setContent("This is an invoice");

        List<DocumentSearch> expectedResults = List.of(doc1);

        when(repository.searchByContent(query)).thenReturn(expectedResults);

        // Act
        List<DocumentSearch> result = service.searchDocuments(query);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("This is an invoice");
        verify(repository).searchByContent(query);
    }

    /**
     * Verifies that searchDocuments returns an empty list (graceful degradation)
     * if the repository throws an exception (e.g., Elasticsearch is down).
     */
    @Test
    void searchDocuments_RepositoryError_ReturnsEmpty() {
        // Arrange
        String query = "crash";

        // Simulate DB failure
        when(repository.searchByContent(query)).thenThrow(new RuntimeException("Elasticsearch down"));

        // Act
        List<DocumentSearch> result = service.searchDocuments(query);

        // Assert
        assertThat(result).isEmpty();
        // Ensure the exception was caught and not rethrown
    }

    /**
     * Verifies that searchDocuments returns an empty list if no matches are found.
     */
    @Test
    void searchDocuments_NoMatches_ReturnsEmpty() {
        // Arrange
        String query = "missing";
        when(repository.searchByContent(query)).thenReturn(List.of());

        // Act
        List<DocumentSearch> result = service.searchDocuments(query);

        // Assert
        assertThat(result).isEmpty();
    }
}