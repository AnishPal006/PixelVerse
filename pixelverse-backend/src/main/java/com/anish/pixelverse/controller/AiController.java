package com.anish.pixelverse.controller;

import com.anish.pixelverse.service.AiCommentator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiController {

    private final AiCommentator aiCommentator;

    @GetMapping("/latest")
    public Map<String, String> getLatestCommentary() {
        return Map.of("commentary", aiCommentator.getLastCommentary());
    }
}