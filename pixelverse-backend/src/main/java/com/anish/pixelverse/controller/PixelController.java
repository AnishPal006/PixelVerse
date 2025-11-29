package com.anish.pixelverse.controller;

import com.anish.pixelverse.model.PixelEvent;
import com.anish.pixelverse.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/pixels")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow React to call this API
public class PixelController {

    private final KafkaTemplate<String, PixelEvent> kafkaTemplate;
    private final PixelRepository pixelRepository;

    // 1. Get the full board state (when user first loads the page)
    @GetMapping
    public List<PixelEvent> getBoard() {
        return pixelRepository.findAll();
    }

    // 2. User paints a pixel
    @PostMapping
    public void paintPixel(@RequestBody PixelRequest request) {
        // Create the event
        PixelEvent event = new PixelEvent(
                request.x() + "-" + request.y(), // Unique ID per coordinate
                request.x(),
                request.y(),
                request.color(),
                request.userId(),
                java.time.Instant.now().toString()
        );

        // Send to Kafka "High Speed Pipe"
        // Topic: t.pixel.updates
        kafkaTemplate.send("t.pixel.updates", event.id(), event);
    }

    // A small DTO record just for the input
    public record PixelRequest(int x, int y, String color, String userId) {}
}