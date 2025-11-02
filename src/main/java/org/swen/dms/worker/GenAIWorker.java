package org.swen.dms.worker;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Component;

@Component
public class GenAIWorker {

    private final Client client;

    public GenAIWorker() {
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
}
