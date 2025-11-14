package org.swen.dms.worker;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.swen.dms.entity.Document;
import org.swen.dms.messaging.DocumentEventPublisher;
import org.swen.dms.messaging.OcrCompletedEvent;
import org.swen.dms.repository.DocumentRepository;

import static org.swen.dms.config.RabbitConfig.QUEUE_GENAI;
import static org.swen.dms.config.RabbitConfig.QUEUE_OCR;

@Component
@Profile("genAIWorker")
public class GenAIWorker {
    private static final Logger log = LoggerFactory.getLogger(GenAIWorker.class);
    private final DocumentRepository repo;

    private final Client client;

    public GenAIWorker(DocumentRepository repo) {
        this.repo = repo;
        String apiKey = "AIzaSyBT4TXaT7GNs5nttFlBvTjCYPPCpVhErio";
        this.client = Client.builder().apiKey(apiKey).build();
    }

    public String summarize(String ocrText) {
        try {
            String prompt = "Summarize the following document in German:\n\n" + ocrText;

            GenerateContentResponse response = client.models
                    .generateContent("gemini-2.0-flash", prompt, null);

            return response.text();
        } catch (Exception e) {
            System.err.println("GenAI request failed: " + e.getMessage());
            return "[Summary unavailable due to API error]";
        }
    }

    @RabbitListener(queues = QUEUE_GENAI)
    public void handle(OcrCompletedEvent event) {
        log.info("Got OcrComplete Event!!!" + event);

        Document doc = repo.findById(event.getDocumentId()).orElse(null);

        String summary = summarize(doc.getOcrText());
        doc.setOcrSummaryText(summary);
        repo.save(doc);

        log.info("GenAI summary saved for doc ID:" + event.getDocumentId());
    }
}
