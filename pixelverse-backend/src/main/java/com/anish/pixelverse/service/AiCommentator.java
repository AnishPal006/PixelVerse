package com.anish.pixelverse.service;

import com.anish.pixelverse.model.PixelEvent;
import com.anish.pixelverse.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AiCommentator {

    private final PixelRepository pixelRepository;
    private final GeminiService geminiService;
    private final SimpMessagingTemplate messagingTemplate;

    // Run every 20 seconds
    @Scheduled(fixedRate = 20000)
    public void analyzeBoard() {
        log.info("AI Commentator is looking at the board...");

        List<PixelEvent> pixels = pixelRepository.findAll();

        if (pixels.isEmpty()) {
            return; // Nothing to say
        }

        // 1. Build a prompt describing the board
        // We only send non-white pixels to save tokens
        String boardDescription = pixels.stream()
                .map(p -> String.format("[%d,%d is %s]", p.x(), p.y(), p.color()))
                .limit(200) // Limit to 200 pixels to avoid overwhelming the AI (or cost)
                .collect(Collectors.joining(", "));

        String prompt = "You are a hilarious sports commentator watching a collaborative pixel art game. " +
                "Here is a list of colored pixels (x,y is color): " + boardDescription + ". " +
                "Describe what you think is being drawn. Be brief, funny, and excited. Max 1 sentence.";

        // 2. Ask Gemini
        String commentary = geminiService.getCommentary(prompt);
        log.info("AI Says: {}", commentary);

        // 3. Broadcast to Frontend
        messagingTemplate.convertAndSend("/topic/commentary", commentary);
    }
}