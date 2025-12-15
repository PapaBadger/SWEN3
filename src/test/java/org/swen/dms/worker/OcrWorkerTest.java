package org.swen.dms.worker;

import net.sourceforge.tess4j.ITesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.swen.dms.messaging.DocumentCreatedEvent;
import org.swen.dms.messaging.OcrCompletedEvent;
import org.swen.dms.repository.jpa.DocumentRepository;

import org.swen.dms.repository.search.DocumentSearchRepository;
import org.swen.dms.entity.DocumentSearch;
import org.swen.dms.entity.Document;

import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.swen.dms.config.RabbitConfig.EXCHANGE_DOCS;
import static org.swen.dms.config.RabbitConfig.ROUTING_OCR_COMPLETED;

@ExtendWith(MockitoExtension.class)
class OcrWorkerTest {

    @Mock
    private MinioClient minioClient;
    @Mock
    private ITesseract tesseract;
    @Mock
    private DocumentRepository repo;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private DocumentSearchRepository searchRepository;

    private OcrWorker ocrWorker;

    @Test
    void handleDocumentCreatedEvent_Success() throws Exception {
        // Arrange
        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo, rabbitTemplate, searchRepository);

        DocumentCreatedEvent event = new DocumentCreatedEvent(
                1L, "Test Document", Instant.now(), "documents", "file-key-1"
        );

        byte[] validPdf = createMinimalPdf();

        // Mock MinIO Response
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.transferTo(any())).thenAnswer(invocation -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(validPdf);
            bais.transferTo(invocation.getArgument(0));
            return (long) validPdf.length;
        });
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // Mock Tesseract & DB
        when(tesseract.doOCR(any(File.class))).thenReturn("Extracted text content");

        Document existingDoc = new Document();
        existingDoc.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existingDoc));

        // Act
        ocrWorker.handle(event);

        // Assert
        verify(repo).save(any(Document.class));
        verify(searchRepository).save(any(DocumentSearch.class)); // Verify Elastic save
        verify(tesseract, atLeastOnce()).doOCR(any(File.class));
    }

    @Test
    void handleDocumentCreatedEvent_DocumentNotFound() throws Exception {
        // Arrange
        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo, rabbitTemplate, searchRepository);

        DocumentCreatedEvent event = new DocumentCreatedEvent(
                1L, "Test Document", Instant.now(), "documents", "file-key-1"
        );

        byte[] validPdf = createMinimalPdf();
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.transferTo(any())).thenAnswer(invocation -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(validPdf);
            bais.transferTo(invocation.getArgument(0));
            return (long) validPdf.length;
        });
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        when(tesseract.doOCR(any(File.class))).thenReturn("Extracted text");

        // Simulate Doc NOT found in DB
        when(repo.findById(1L)).thenReturn(Optional.empty());

        // Act
        ocrWorker.handle(event);

        // Assert - Should log error but NOT save to ES or DB
        verify(repo, never()).save(any());
        verify(searchRepository, never()).save(any());
    }

    @Test
    void handleDocumentCreatedEvent_MinIOFailure() throws Exception {
        // Arrange
        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo, rabbitTemplate, searchRepository);

        DocumentCreatedEvent event = new DocumentCreatedEvent(
                1L, "Test Document", Instant.now(), "documents", "file-key-1"
        );

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        // Act
        ocrWorker.handle(event);

        // Assert
        verify(repo, never()).save(any());
        verify(searchRepository, never()).save(any());
        verify(tesseract, never()).doOCR(any(File.class));
    }

    @Test
    void shouldIndexOcrResultInElasticsearch() throws Exception {
        // Arrange
        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo, rabbitTemplate, searchRepository);

        Long docId = 123L;
        String expectedOcrText = "This is the text detected by OCR";
        DocumentCreatedEvent event = new DocumentCreatedEvent(docId, "Test Title", null, "documents", "test.pdf");

        // Mock DB
        Document existingDoc = new Document();
        existingDoc.setId(docId);
        when(repo.findById(docId)).thenReturn(Optional.of(existingDoc));

        // Mock MinIO
        byte[] pdfBytes = createMinimalPdf();
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.transferTo(any())).thenAnswer(invocation -> {
            ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
            bais.transferTo(invocation.getArgument(0));
            return (long) pdfBytes.length;
        });
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // Mock Tesseract
        when(tesseract.doOCR(any(File.class))).thenReturn(expectedOcrText);

        // Act
        ocrWorker.handle(event);

        // Assert
        verify(repo, times(1)).save(any(Document.class));

        // Capture what was saved to Elastic
        ArgumentCaptor<DocumentSearch> esCaptor = ArgumentCaptor.forClass(DocumentSearch.class);
        verify(searchRepository, times(1)).save(esCaptor.capture());

        DocumentSearch savedEsDoc = esCaptor.getValue();
        assertEquals(String.valueOf(docId), savedEsDoc.getId());

        // Use contains because the worker might append newlines
        assertTrue(savedEsDoc.getContent().contains(expectedOcrText));
    }

    /**
     * Helper: Creates a minimal valid PDF file content for testing.
     */
    private byte[] createMinimalPdf() throws Exception {
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new PDPage());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Verifies that if Elasticsearch is down, the worker DOES NOT crash.
     * It should still save to the DB and send the completion event.
     */
    @Test
    void handle_ElasticsearchFailure_ShouldContinue() throws Exception {
        // Arrange
        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo, rabbitTemplate, searchRepository);

        DocumentCreatedEvent event = new DocumentCreatedEvent(1L, "Test", Instant.now(), "docs", "key");
        byte[] pdfBytes = createMinimalPdf();

        // 1. MinIO works
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.transferTo(any())).thenAnswer(i -> {
            new ByteArrayInputStream(pdfBytes).transferTo(i.getArgument(0));
            return (long) pdfBytes.length;
        });
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        // 2. DB and OCR work
        when(repo.findById(1L)).thenReturn(Optional.of(new Document()));
        when(tesseract.doOCR(any(File.class))).thenReturn("Text");

        // 3. ELASTICSEARCH FAILS (The critical part)
        doThrow(new RuntimeException("Elasticsearch is down"))
                .when(searchRepository).save(any(DocumentSearch.class));

        // Act
        ocrWorker.handle(event);

        // Assert
        // The DB update MUST still happen
        verify(repo).save(any(Document.class));

        // The RabbitMQ completion event MUST still be sent
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE_DOCS), eq(ROUTING_OCR_COMPLETED), any(OcrCompletedEvent.class));

        // Verify we tried to save to ES (and it failed, but didn't stop us)
        verify(searchRepository).save(any(DocumentSearch.class));
    }
}