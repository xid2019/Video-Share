package com.videoshare.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.videoshare.model.Video;

public interface VideoRepository extends MongoRepository<Video, String>{
  
}
