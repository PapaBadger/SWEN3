package org.swen.dms.worker;

import net.sourceforge.tess4j.ITesseract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swen.dms.messaging.DocumentCreatedEvent;
import org.swen.dms.repository.DocumentRepository;

import io.minio.MinioClient;
import io.minio.GetObjectArgs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link org.swen.dms.worker.OcrWorker}.
 * <p>
 * Verifies that OCR processing is triggered correctly when document events are received
 * and that text extraction results are properly saved to the database.
 */
@ExtendWith(MockitoExtension.class)
class OcrWorkerTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ITesseract tesseract;

    @Mock
    private DocumentRepository repo;

    private OcrWorker ocrWorker;

    /**
     * Verifies that handle method processes a DocumentCreatedEvent successfully,
     * extracts text from the PDF, and saves the OCR result to the database.
     */
//    @Test
//    void handleDocumentCreatedEvent_Success() throws Exception {
//        // Arrange
//        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo);
//
//        DocumentCreatedEvent event = new DocumentCreatedEvent(
//                1L, "Test Document", Instant.now(), "documents", "file-key-1"
//        );
//
//        // Create a minimal valid PDF
//        byte[] validPdf = createMinimalPdf();
//
//        when(minioClient.getObject(any(GetObjectArgs.class)))
//                .thenReturn(new io.minio.GetObjectResponse(
//                        null, "documents", "file-key-1", null,
//                        new ByteArrayInputStream(validPdf)
//                ));
//
//        when(tesseract.doOCR(any(File.class))).thenReturn("Extracted text");
//        when(repo.findById(1L)).thenReturn(Optional.of(new org.swen.dms.entity.Document()));
//
//        // Act
//        ocrWorker.handle(event);
//
//        // Assert
//        verify(repo).save(any());
//        verify(tesseract, atLeastOnce()).doOCR(any(File.class));
//    }
//
//    /**
//     * Verifies that handle method gracefully handles the situation
//     * when the document cannot be found in the database.
//     */
//    @Test
//    void handleDocumentCreatedEvent_DocumentNotFound() throws Exception {
//        // Arrange
//        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo);
//
//        DocumentCreatedEvent event = new DocumentCreatedEvent(
//                1L, "Test Document", Instant.now(), "documents", "file-key-1"
//        );
//
//        // Create a minimal valid PDF
//        byte[] validPdf = createMinimalPdf();
//
//        when(minioClient.getObject(any(GetObjectArgs.class)))
//                .thenReturn(new io.minio.GetObjectResponse(
//                        null, "documents", "file-key-1", null,
//                        new ByteArrayInputStream(validPdf)
//                ));
//
//        when(tesseract.doOCR(any(File.class))).thenReturn("Extracted text");
//        when(repo.findById(1L)).thenReturn(Optional.empty());
//
//        // Act
//        ocrWorker.handle(event);
//
//        // Assert - Should log error but not crash
//        verify(repo, never()).save(any());
//        verify(tesseract, atLeastOnce()).doOCR(any(File.class));
//    }
//
//    /**
//     * Verifies that handle method gracefully handles MinIO failures
//     * when retrieving the document file.
//     */
//    @Test
//    void handleDocumentCreatedEvent_MinIOFailure() throws Exception {
//        // Arrange
//        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo);
//
//        DocumentCreatedEvent event = new DocumentCreatedEvent(
//                1L, "Test Document", Instant.now(), "documents", "file-key-1"
//        );
//
//        when(minioClient.getObject(any(GetObjectArgs.class)))
//                .thenThrow(new RuntimeException("MinIO connection failed"));
//
//        // Act
//        ocrWorker.handle(event);
//
//        // Assert - Should log error but not crash
//        verify(repo, never()).save(any());
//        verify(tesseract, never()).doOCR(any(File.class));
//    }
//
//    /**
//     * Verifies that handle method gracefully handles OCR processing failures
//     * when Tesseract encounters an error.
//     */
//    @Test
//    void handleDocumentCreatedEvent_OcrFailure() throws Exception {
//        // Arrange
//        ocrWorker = new OcrWorker(minioClient, tesseract, 300, repo);
//
//        DocumentCreatedEvent event = new DocumentCreatedEvent(
//                1L, "Test Document", Instant.now(), "documents", "file-key-1"
//        );
//
//        // Create a minimal valid PDF
//        byte[] validPdf = createMinimalPdf();
//
//        when(minioClient.getObject(any(GetObjectArgs.class)))
//                .thenReturn(new io.minio.GetObjectResponse(
//                        null, "documents", "file-key-1", null,
//                        new ByteArrayInputStream(validPdf)
//                ));
//
//        when(tesseract.doOCR(any(File.class)))
//                .thenThrow(new RuntimeException("OCR processing failed"));
//        // Removed repo.findById stub - it's not called when OCR fails
//
//        // Act
//        ocrWorker.handle(event);
//
//        // Assert - Should log error but not crash
//        verify(repo, never()).save(any());
//        verify(repo, never()).findById(any());
//    }

    /**
     * Creates a minimal valid PDF file content for testing.
     * This is the smallest possible PDF that can be parsed by PDFBox.
     */
    private byte[] createMinimalPdf() {
        String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\n" +
                "endobj\n" +
                "xref\n" +
                "0 4\n" +
                "0000000000 65535 f\n" +
                "0000000009 00000 n\n" +
                "0000000058 00000 n\n" +
                "0000000115 00000 n\n" +
                "trailer\n" +
                "<< /Size 4 /Root 1 0 R >>\n" +
                "startxref\n" +
                "190\n" +
                "%%EOF";
        return pdfContent.getBytes();
    }
}