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
        // 🔴 Logic Check: Is this a paint event or a clear signal?
        if (event.x() == -1 && event.y() == -1) {
            // It's a CLEAR signal. Don't save to DB (it's already cleared).
            // Just tell Frontend to wipe.
            log.info("📢 Broadcasting Board Reset!");
            messagingTemplate.convertAndSend("/topic/board", event);
        } else {
            // Normal Paint Event
            log.info("Pixel Update: [{}, {}]", event.x(), event.y());
            pixelRepository.save(event);
            messagingTemplate.convertAndSend("/topic/board", event);
        }
    }
}