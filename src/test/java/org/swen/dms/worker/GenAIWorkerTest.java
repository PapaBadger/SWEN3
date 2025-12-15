package org.swen.dms.worker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swen.dms.entity.Document;
import org.swen.dms.messaging.OcrCompletedEvent;
import org.swen.dms.repository.jpa.DocumentRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenAIWorkerTest {

    @Mock
    private DocumentRepository repo;

    @Test
    void handle_Success() {
        // Arrange
        Long docId = 1L;
        String ocrText = "Das ist ein langer Vertragstext.";
        String expectedSummary = "Zusammenfassung: Vertrag ok.";

        Document doc = new Document();
        doc.setId(docId);
        doc.setOcrText(ocrText);

        when(repo.findById(docId)).thenReturn(Optional.of(doc));

        // 1. Create a SPY of the worker (using the protected constructor)
        // We pass 'repo', but 'client' is null (we won't use it)
        GenAIWorker worker = new GenAIWorker(repo);
        GenAIWorker spyWorker = spy(worker);

        // 2. Override the protected method to return our fake summary
        // This completely bypasses the Google library types
        doReturn(expectedSummary).when(spyWorker).callGenAiApi(anyString());

        // Act
        spyWorker.handle(new OcrCompletedEvent(docId));

        // Assert
        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(repo).save(docCaptor.capture());

        assertThat(docCaptor.getValue().getOcrSummaryText()).isEqualTo(expectedSummary);
    }

    @Test
    void handle_ApiError_SavesFallbackMessage() {
        // Arrange
        Long docId = 1L;
        Document doc = new Document();
        doc.setId(docId);
        doc.setOcrText("Input");

        when(repo.findById(docId)).thenReturn(Optional.of(doc));

        GenAIWorker worker = new GenAIWorker(repo);
        GenAIWorker spyWorker = spy(worker);

        // Simulate an API failure by making the method throw
        doThrow(new RuntimeException("API Quota Exceeded"))
                .when(spyWorker).callGenAiApi(anyString());

        // Act
        spyWorker.handle(new OcrCompletedEvent(docId));

        // Assert
        ArgumentCaptor<Document> docCaptor = ArgumentCaptor.forClass(Document.class);
        verify(repo).save(docCaptor.capture());

        assertThat(docCaptor.getValue().getOcrSummaryText())
                .contains("unavailable due to API error");
    }
}