package com.anish.pixelverse.service;

import com.anish.pixelverse.model.PixelEvent;
import com.anish.pixelverse.repository.PixelRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

    private String lastBoardSignature = "";

    // 🟢 1. MEMORY: Store the last response so new users can see it
    @Getter
    private String lastCommentary = "Waiting for satellite link...";

    @Scheduled(fixedRate = 10000)
    public void analyzeBoard() {
        List<PixelEvent> pixels = pixelRepository.findAll();
        if (pixels.isEmpty()) return;

        // Signature check to save API calls
        String currentSignature = pixels.size() + "-" + pixels.hashCode();
        if (currentSignature.equals(lastBoardSignature)) return;
        lastBoardSignature = currentSignature;

        log.info("🎨 Sending Board Visual to Gemini...");

        // 🟢 2. VISION UPGRADE: Convert pixels to ASCII Grid
        char[][] grid = new char[50][50];
        for (char[] row : grid) Arrays.fill(row, '.'); // Fill with empty space

        for (PixelEvent p : pixels) {
            if (p.x() >= 0 && p.x() < 50 && p.y() >= 0 && p.y() < 50) {
                grid[p.x()][p.y()] = '█'; // Draw the pixel
            }
        }

        // Convert 2D array to a single string
        StringBuilder asciiArt = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            asciiArt.append(new String(grid[i])).append("\n");
        }

        // 🟢 3. NEW PROMPT: Ask it to look at the ASCII art
        String prompt = "You are looking at a 50x50 pixel art grid represented by ASCII characters. " +
                "The character '█' represents a colored pixel, and '.' is empty space.\n\n" +
                asciiArt.toString() + "\n\n" +
                "Identify the main object or shape. Is it a house? A face? A tree? " +
                "Ignore stray pixels. Be confident. Max 10 words.";

        String response = geminiService.getCommentary(prompt);

        // Clean up response
        this.lastCommentary = response.trim();
        log.info("AI Says: {}", this.lastCommentary);

        messagingTemplate.convertAndSend("/topic/commentary", this.lastCommentary);
    }
}