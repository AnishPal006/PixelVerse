package com.anish.pixelverse.repository;

import com.anish.pixelverse.model.PixelEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PixelRepository extends MongoRepository<PixelEvent, String> {
}