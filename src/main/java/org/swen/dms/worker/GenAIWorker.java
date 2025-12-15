package org.swen.dms.worker;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.swen.dms.entity.Document;
import org.swen.dms.messaging.OcrCompletedEvent;
import org.swen.dms.repository.jpa.DocumentRepository;

import static org.swen.dms.config.RabbitConfig.QUEUE_GENAI;

@Component
@Profile("genAIWorker")
public class GenAIWorker {
    private static final Logger log = LoggerFactory.getLogger(GenAIWorker.class);
    private final DocumentRepository repo;

    // We keep Client here for production use
    private final Client client;

    @Autowired
    public GenAIWorker(DocumentRepository repo, @Value("${GENAI_API_KEY}") String apiKey) {
        this.repo = repo;
        this.client = Client.builder().apiKey(apiKey).build();
    }

    // Protected constructor for testing (lets us pass null client)
    protected GenAIWorker(DocumentRepository repo) {
        this.repo = repo;
        this.client = null;
    }

    /**
     * EXTRACTED METHOD: This isolates the "hidden" Google types.
     * In production, it calls Google. In tests, we override this to return a string.
     */
    protected String callGenAiApi(String prompt) {
        // This is the ONLY place that touches the hidden 'Models' class
        GenerateContentResponse response = client.models
                .generateContent("gemini-2.5-flash", prompt, null);
        return response.text();
    }

    public String summarize(String ocrText) {
        try {
            String prompt = "Summarize the following document in German:\n\n" + ocrText;

            // Call our wrapper method instead of 'client.models...' directly
            return callGenAiApi(prompt);

        } catch (Exception e) {
            System.err.println("GenAI request failed: " + e.getMessage());
            return "[Summary unavailable due to API error]";
        }
    }

    @RabbitListener(queues = QUEUE_GENAI)
    public void handle(OcrCompletedEvent event) {
        log.info("Got OcrComplete Event!!!" + event);

        Document doc = repo.findById(event.getDocumentId()).orElse(null);
        if (doc == null) return;

        String summary = summarize(doc.getOcrText());
        doc.setOcrSummaryText(summary);
        repo.save(doc);

        log.info("GenAI summary saved for doc ID:" + event.getDocumentId());
    }
}