package com.videoshare.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.videoshare.dto.CommentDto;
import com.videoshare.dto.UploadVideoResponse;
import com.videoshare.dto.VideoDto;
import com.videoshare.model.Comment;
import com.videoshare.model.Video;
import com.videoshare.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {

  private final S3Service s3Service;
  private final VideoRepository videoRepository;
  private final UserService userService;

  public UploadVideoResponse uploadVideo(MultipartFile multipartFile){
    String videoUrl = s3Service.uploadFile(multipartFile);
    var video = new Video();
    video.setVideoUrl(videoUrl);
    
    var savedVideo = videoRepository.save(video);

    return new UploadVideoResponse(savedVideo.getId(), savedVideo.getVideoUrl());
  }

  private VideoDto mapTovideoDto(Video videoById) {
    VideoDto videoDto = new VideoDto();
    videoDto.setVideoUrl(videoById.getVideoUrl());
    videoDto.setThumbnailUrl(videoById.getThumbnailUrl());
    videoDto.setId(videoById.getId());
    videoDto.setTitle(videoById.getTitle());
    videoDto.setDescription(videoById.getDescription());
    videoDto.setTags(videoById.getTags());
    videoDto.setVideoStatus(videoById.getVideoStatus());
    videoDto.setLikeCount(videoById.getLikes().get());
    videoDto.setDislikeCount(videoById.getDisLikes().get());
    videoDto.setViewCount(videoById.getViewCount().get());

    return videoDto;
  }
  public VideoDto editVideo(VideoDto videoDto){

    var savedVideo = getVideoById(videoDto.getId());
    savedVideo.setTitle(videoDto.getTitle());
    savedVideo.setDescription(videoDto.getDescription());
    savedVideo.setTags(videoDto.getTags());
    savedVideo.setThumbnailUrl(videoDto.getThumbnailUrl());
    savedVideo.setVideoStatus(videoDto.getVideoStatus());

    videoRepository.save(savedVideo);
    return videoDto;
  }

  public String uploadThumbnail(MultipartFile file, String videoId){
    var savedVideo = getVideoById(videoId);

    String thumbnailUrl = s3Service.uploadFile(file);

    savedVideo.setThumbnailUrl(thumbnailUrl);

    videoRepository.save(savedVideo);
    return thumbnailUrl;
  }

  Video getVideoById(String videoId){
    return videoRepository.findById(videoId)
    .orElseThrow(
      () -> new IllegalArgumentException("Cannot find video by id - " + videoId)
    );
  }

  private void increseVideoCount(Video savedVideo) {
    savedVideo.incrementViewCount();
    videoRepository.save(savedVideo);
  }

  public VideoDto getVideoDetails(String videoId) {
    Video savedVideo = getVideoById(videoId);

    increseVideoCount(savedVideo);
    userService.addVideoToHistory(videoId);

    VideoDto videoDto = mapTovideoDto(savedVideo);

    return videoDto;
  }

  public VideoDto likeVideo(String videoId) {
    Video videoById = getVideoById(videoId);

    if (userService.ifLikedVideo(videoId)) {
      videoById.decrementLike();
      userService.removeFromLikedVideos(videoId);
    } else if (userService.ifDisLikedVideo(videoId)){
      videoById.decrementDisLike();
      userService.removeFromDisLikedVideos(videoId);
      videoById.incrementLikes();
      userService.addToLikedVideos(videoId);
    } else {
      videoById.incrementLikes();
      userService.addToLikedVideos(videoId);
    }
    
    videoRepository.save(videoById);

    VideoDto videoDto = mapTovideoDto(videoById);

    return videoDto;
  }

  public VideoDto dislikeVideo(String videoId) {
    Video videoById = getVideoById(videoId);

    if (userService.ifDisLikedVideo(videoId)) {
      videoById.decrementDisLike();
      userService.removeFromDisLikedVideos(videoId);
    } else if (userService.ifLikedVideo(videoId)){
      videoById.decrementLike();
      userService.removeFromLikedVideos(videoId);
      videoById.incrementDisLikes();
      userService.addToDisLikedVideos(videoId);
    } else {
      videoById.incrementDisLikes();
      userService.addToDisLikedVideos(videoId);
    }
    
    videoRepository.save(videoById);

    VideoDto videoDto = mapTovideoDto(videoById);

    return videoDto;
  }

  public void addComment(String videoId, CommentDto commentDto) {
    Video video = getVideoById(videoId);
    Comment comment = new Comment();
    comment.setText(commentDto.getCommentText());
    comment.setAuthorId(commentDto.getAuthorId());
    video.addComment(comment);

    videoRepository.save(video);
  }

  public List<CommentDto> getAllComments(String videoId) {
    Video video = getVideoById(videoId);
    List<Comment> commentList = video.getCommentList();

    return commentList.stream().map(this::mapToCommentDto).toList();
  }

  private CommentDto mapToCommentDto(Comment comment) {
    CommentDto commentDto = new CommentDto();
    commentDto.setCommentText(comment.getText());
    commentDto.setAuthorId(comment.getAuthorId());
    return commentDto;
  }

  public List<VideoDto> getAllVideos() {
    return videoRepository.findAll().stream().map(this::mapTovideoDto).toList();
  }
}
