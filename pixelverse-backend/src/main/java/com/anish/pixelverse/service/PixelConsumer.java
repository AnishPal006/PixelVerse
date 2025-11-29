package com.anish.pixelverse.service;

import com.anish.pixelverse.model.PixelEvent;
import com.anish.pixelverse.repository.PixelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PixelConsumer {

    private final PixelRepository pixelRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "t.pixel.updates", groupId = "pixel-group")
    public void consume(PixelEvent event) {
        log.info("Pixel Update: [{}, {}] -> {}", event.x(), event.y(), event.color());

        // 1. Save to Persistence (MongoDB)
        pixelRepository.save(event);

        // 2. Broadcast to Real-Time Clients (React)
        messagingTemplate.convertAndSend("/topic/board", event);
    }
}