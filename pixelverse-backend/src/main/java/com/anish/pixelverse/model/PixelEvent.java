package com.anish.pixelverse.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "pixels")
public record PixelEvent(
        @Id String id,       // We will format this as "x-y" (e.g., "5-10")
        int x,
        int y,
        String color,
        String userId,
        Instant timestamp
) {}