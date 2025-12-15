package org.swen.dms.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.service.SearchService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link SearchController}.
 * <p>
 * Uses {@link WebMvcTest} and mocks {@link SearchService}.
 * Ensures REST endpoints for document search behave correctly.
 */
@WebMvcTest(controllers = SearchController.class,
    excludeAutoConfiguration = {RabbitAutoConfiguration.class, ElasticsearchDataAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SearchService searchService;

    private DocumentSearch createTestDocumentSearch(String id, String title, String content) {
        DocumentSearch doc = new DocumentSearch();
        doc.setId(id);
        doc.setTitle(title);
        doc.setContent(content);
        return doc;
    }

    /**
     * Verifies that GET /api/search?q=query returns matching documents.
     */
    @Test
    void search_Success() throws Exception {
        List<DocumentSearch> results = List.of(
                createTestDocumentSearch("1", "Invoice Document", "Invoice content"),
                createTestDocumentSearch("2", "Invoice Report", "Report content")
        );

        when(searchService.searchDocuments("invoice")).thenReturn(results);

        mvc.perform(get("/api/search")
                        .param("q", "invoice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Invoice Document"))
                .andExpect(jsonPath("$[1].title").value("Invoice Report"));

        verify(searchService).searchDocuments("invoice");
    }

    /**
     * Verifies that GET /api/search?q=query returns empty list when no matches found.
     */
    @Test
    void search_NoResults() throws Exception {
        when(searchService.searchDocuments("nonexistent")).thenReturn(List.of());

        mvc.perform(get("/api/search")
                        .param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchService).searchDocuments("nonexistent");
    }

    /**
     * Verifies that GET /api/search handles empty query parameter.
     */
    @Test
    void search_EmptyQuery() throws Exception {
        when(searchService.searchDocuments("")).thenReturn(List.of());

        mvc.perform(get("/api/search")
                        .param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchService).searchDocuments("");
    }

    /**
     * Verifies that GET /api/search without query parameter returns 400 Bad Request.
     */
    @Test
    void search_MissingQueryParameter() throws Exception {
        mvc.perform(get("/api/search"))
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof MissingServletRequestParameterException
                ));
    }

    /**
     * Verifies that GET /api/search with multiple results returns all matching documents.
     */
    @Test
    void search_MultipleResults() throws Exception {
        List<DocumentSearch> results = List.of(
                createTestDocumentSearch("1", "Contract 2024", "Annual contract"),
                createTestDocumentSearch("2", "Contract 2023", "Previous contract"),
                createTestDocumentSearch("3", "Contract Amendment", "Contract update")
        );

        when(searchService.searchDocuments("contract")).thenReturn(results);

        mvc.perform(get("/api/search")
                        .param("q", "contract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[2].id").value("3"));

        verify(searchService).searchDocuments("contract");
    }

    /**
     * Verifies that GET /api/search handles special characters in query.
     */
    @Test
    void search_WithSpecialCharacters() throws Exception {
        when(searchService.searchDocuments("invoice-2024")).thenReturn(List.of());

        mvc.perform(get("/api/search")
                        .param("q", "invoice-2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchService).searchDocuments("invoice-2024");
    }

    /**
     * Verifies that GET /api/search handles numeric query.
     */
    @Test
    void search_NumericQuery() throws Exception {
        List<DocumentSearch> results = List.of(
                createTestDocumentSearch("1", "Document 12345", "Reference number 12345")
        );

        when(searchService.searchDocuments("12345")).thenReturn(results);

        mvc.perform(get("/api/search")
                        .param("q", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Document 12345"));

        verify(searchService).searchDocuments("12345");
    }

    /**
     * Verifies that GET /api/search handles long query strings.
     */
    @Test
    void search_LongQuery() throws Exception {
        String longQuery = "this is a very long search query that contains multiple words and phrases";
        when(searchService.searchDocuments(longQuery)).thenReturn(List.of());

        mvc.perform(get("/api/search")
                        .param("q", longQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(searchService).searchDocuments(longQuery);
    }

    /**
     * Verifies that if the SearchService throws an exception (e.g. DB down),
     * the controller propagates it (resulting in 500 Internal Server Error).
     */
    @Test
    void search_ServiceFailure() throws Exception {
        when(searchService.searchDocuments("crash")).thenThrow(new RuntimeException("Elasticsearch is down"));

        mvc.perform(get("/api/search")
                        .param("q", "crash"))
                .andExpect(status().isInternalServerError()) // Expect 500
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResolvedException() instanceof RuntimeException
                ));
    }

    /**
     * Verifies that leading/trailing whitespace is preserved (passed to service as-is).
     * If you want it trimmed, you'd handle that logic in the Controller and update this test.
     */
    @Test
    void search_PreservesWhitespace() throws Exception {
        String queryWithSpace = "  invoice  ";
        when(searchService.searchDocuments(anyString())).thenReturn(List.of());

        mvc.perform(get("/api/search")
                        .param("q", queryWithSpace))
                .andExpect(status().isOk());

        // Verify the service received the EXACT string with spaces
        verify(searchService).searchDocuments("  invoice  ");
    }

    /**
     * Verifies behavior when the service returns null (instead of empty list).
     * The controller should serialize this as a null JSON response body.
     */
    @Test
    void search_ServiceReturnsNull() throws Exception {
        // Simulating a buggy service returning null
        when(searchService.searchDocuments("null-check")).thenReturn(null);

        mvc.perform(get("/api/search")
                        .param("q", "null-check"))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Or empty string/null depending on Jackson config
    }
}
