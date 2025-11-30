package com.anish.pixelverse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getCommentary(String prompt) {
        // Construct the JSON Body
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        try {
            // 🟢 UPDATED: Send API Key in the HEADER, not the URL
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("x-goog-api-key", apiKey) // <--- The Fix
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("❌ Gemini API Error: {}", e.getMessage());
            // Print the full error for debugging if needed
            // e.printStackTrace();
            return "The AI commentator is having technical difficulties! (Check logs)";
        }
    }

    private String extractTextFromResponse(String json) {
        try {
            int startIndex = json.indexOf("\"text\": \"") + 9;
            int endIndex = json.indexOf("\"", startIndex);
            // Handle escaped newlines (\n) to make it look nice
            return json.substring(startIndex, endIndex).replace("\\n", " ");
        } catch (Exception e) {
            return "Wow! Look at those colors!";
        }
    }
}