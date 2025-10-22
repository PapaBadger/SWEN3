package org.swen.dms.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.swen.dms.entity.Document;
import org.swen.dms.exception.NotFoundException;
import org.swen.dms.messaging.DocumentCreatedEvent;
import org.swen.dms.messaging.DocumentEventPublisher;
import org.swen.dms.messaging.DocumentUpdatedEvent;
import org.swen.dms.repository.DocumentRepository;

import io.minio.MinioClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link org.swen.dms.service.DocumentServiceImpl}.
 * <p>
 * Uses Mockito to mock dependencies and ensures business logic
 * is correct without touching external systems.
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository repo;

    @Mock
    private DocumentEventPublisher publisher;

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private DocumentServiceImpl service;

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
     * Verifies that uploadDocument successfully saves a document to MinIO and database,
     * and publishes a DocumentCreatedEvent.
     */
    @Test
    void uploadDocument_Success() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "test.pdf", "test.pdf", "application/pdf", "content".getBytes()
        );
        Document savedDoc = createTestDocument(1L);

        when(repo.save(any(Document.class))).thenReturn(savedDoc);

        ResponseEntity<?> result = service.uploadDocument(file, "Test Document");

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        verify(repo).save(any(Document.class));

        ArgumentCaptor<DocumentCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(DocumentCreatedEvent.class);
        verify(publisher).publishDocumentCreated(eventCaptor.capture());

        DocumentCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getId()).isEqualTo(1L);
    }

    /**
     * Verifies that uploadDocument returns 400 Bad Request when an empty file is provided.
     */
    @Test
    void uploadDocument_EmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "empty.pdf", "empty.pdf", "application/pdf", new byte[0]
        );

        ResponseEntity<?> result = service.uploadDocument(file, "Test Document");

        assertThat(result.getStatusCode().is4xxClientError()).isTrue();
    }

    /**
     * Verifies that uploadDocument returns 400 Bad Request
     * when a non-PDF file is uploaded.
     */
    @Test
    void uploadDocument_InvalidContentType() {
        MultipartFile file = new MockMultipartFile(
                "test.txt", "test.txt", "text/plain", "content".getBytes()
        );

        ResponseEntity<?> result = service.uploadDocument(file, "Test Document");

        assertThat(result.getStatusCode().is4xxClientError()).isTrue();
        assertThat(result.getBody()).asString().contains("Only PDFs allowed");
    }

    /**
     * Verifies that findAll returns all documents from the repository.
     */
    @Test
    void findAll_Success() {
        List<Document> documents = List.of(
                createTestDocument(1L),
                createTestDocument(2L)
        );
        when(repo.findAll()).thenReturn(documents);

        List<Document> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    /**
     * Verifies that findById returns the document when it exists.
     */
    @Test
    void findById_returnsEntity() {
        Document doc = createTestDocument(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(doc));

        Document result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(repo).findById(1L);
    }

    /**
     * Verifies that findById throws NotFoundException when the entity does not exist.
     */
    @Test
    void findById_throwsNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    /**
     * Verifies that update changes the document title and publishes a DocumentUpdatedEvent.
     */
    @Test
    void update_Success() {
        Document existing = createTestDocument(1L);
        String originalTitle = existing.getTitle(); // Capture original title

        Document update = new Document();
        update.setTitle("Updated Title");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        // Mock save to return the document with updated title
        when(repo.save(any(Document.class))).thenAnswer(invocation -> {
            Document doc = invocation.getArgument(0);
            return doc; // Return the document that was passed to save
        });

        Document result = service.update(1L, update);

        assertThat(result.getTitle()).isEqualTo("Updated Title");

        ArgumentCaptor<DocumentUpdatedEvent> eventCaptor =
                ArgumentCaptor.forClass(DocumentUpdatedEvent.class);
        verify(publisher).publishDocumentUpdated(eventCaptor.capture());

        DocumentUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getId()).isEqualTo(1L);
        assertThat(capturedEvent.getTitleBefore()).isEqualTo(originalTitle);
        assertThat(capturedEvent.getTitleAfter()).isEqualTo("Updated Title");
    }

    /**
     * Verifies that delete removes an entity if it exists.
     */
    @Test
    void delete_existing_deletes() {
        Document doc = createTestDocument(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(doc));

        service.delete(1L);

        verify(repo).deleteById(1L);
    }

    /**
     * Verifies that delete throws NotFoundException if the entity does not exist.
     */
    @Test
    void delete_missing_throws() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(NotFoundException.class);
        verify(repo, never()).deleteById(anyLong());
    }

    /**
     * Verifies that existsByTitle delegates to the repository method.
     */
    @Test
    void existsByTitle_delegates() {
        when(repo.existsByTitle("Test Document")).thenReturn(true);

        boolean result = service.existsByTitle("Test Document");

        assertThat(result).isTrue();
        verify(repo).existsByTitle("Test Document");
    }
}