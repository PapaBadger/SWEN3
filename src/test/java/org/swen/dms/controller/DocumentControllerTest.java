package org.swen.dms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.swen.dms.entity.Document;
import org.swen.dms.service.DocumentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link org.swen.dms.controller.DocumentController}.
 * <p>
 * Uses {@link org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest}
 * and mocks {@link org.swen.dms.service.DocumentService}.
 * Ensures REST endpoints behave correctly (HTTP status, JSON output).
 */
@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentService service;

    private Document createTestDocument(Long id) {
        Document doc = new Document();
        doc.setId(id);
        doc.setTitle("Test Document " + id);
        doc.setFileKey("file-key-" + id);
        doc.setContentType("application/pdf");
        doc.setFileSize(1024L);
        doc.setUploadedAt(LocalDateTime.now());
        return doc;
    }

    /**
     * Verifies that POST /api/documents/upload successfully uploads a document
     * and returns the saved entity as JSON.
     */
    @Test
    void uploadDocument_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes()
        );

        Document savedDoc = createTestDocument(1L);

        // Alternative: Use doReturn instead of when().thenReturn()
        doReturn(ResponseEntity.ok(savedDoc))
                .when(service).uploadDocument(any(), anyString(), null);

        mvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("title", "Test Document"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Document 1"));
    }

    /**
     * Verifies that POST /api/documents/upload returns 400 Bad Request
     * when an empty file is provided.
     */
    @Test
    void uploadDocument_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", new byte[0]
        );

        // Alternative: Use doReturn instead of when().thenReturn()
        doReturn(ResponseEntity.badRequest().body("Nothing uploaded."))
                .when(service).uploadDocument(any(), anyString(), null);

        mvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("title", "Test Document"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that GET /api/documents returns a list of all documents as JSON.
     */
    @Test
    void listDocuments_Success() throws Exception {
        List<Document> documents = List.of(
                createTestDocument(1L),
                createTestDocument(2L)
        );

        when(service.findAll()).thenReturn(documents);

        mvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    /**
     * Verifies that GET /api/documents/{id}/download returns the document file
     * with correct content type and headers.
     */
    @Test
    void downloadDocument_Success() throws Exception {
        byte[] fileContent = "PDF content".getBytes();

        when(service.downloadDocument(1L))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(fileContent));

        mvc.perform(get("/api/documents/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    /**
     * Verifies that PUT /api/documents/{id} updates a document
     * and returns the updated entity.
     */
    @Test
    void updateDocument_Success() throws Exception {
        Document updatedDoc = createTestDocument(1L);
        updatedDoc.setTitle("Updated Title");

        // Fix: Use fully qualified class name to resolve ambiguity
        when(service.update(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(Document.class)))
                .thenReturn(updatedDoc);

        mvc.perform(put("/api/documents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDoc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    /**
     * Verifies that DELETE /api/documents/{id} successfully removes a document.
     */
    @Test
    void deleteDocument_Success() throws Exception {
        mvc.perform(delete("/api/documents/1"))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }
}