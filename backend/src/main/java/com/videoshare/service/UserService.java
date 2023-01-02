package com.videoshare.service;

import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.videoshare.model.User;
import com.videoshare.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public User getCurrentUser() {
    String sub = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaim("sub");
    return userRepository.findBySub(sub).orElseThrow(
      () -> new IllegalArgumentException("Cannot find user with sub -" + sub)
    );
  }

  public void addToLikedVideos(String videoId) {
    User currentUser = getCurrentUser();
    currentUser.addToLikeVideos(videoId);
    userRepository.save(currentUser);
  }

  public boolean ifLikedVideo(String videoId) {
    return getCurrentUser().getLikedVideos().stream().anyMatch(likedVideo -> likedVideo.equals(videoId));
  }

  public boolean ifDisLikedVideo(String videoId) {
    return getCurrentUser().getDisLikedVideos().stream().anyMatch(disLikedVideo -> disLikedVideo.equals(videoId));
  }

  public void removeFromLikedVideos(String videoId) {
    User currentUser = getCurrentUser();
    currentUser.removeFromLikedVideos(videoId);
    userRepository.save(currentUser);
  }

  public void removeFromDisLikedVideos(String videoId) {
    User currentUser = getCurrentUser();
    currentUser.removeFromDisLikedVideos(videoId);
    userRepository.save(currentUser);
  }

  public void addToDisLikedVideos(String videoId) {
    User currentUser = getCurrentUser();
    currentUser.addToDisLikeVideos(videoId);
    userRepository.save(currentUser);
  }

  public void addVideoToHistory(String videoId) {
    User currentUser = getCurrentUser();
    currentUser.addToVideoHistory(videoId);
    userRepository.save(currentUser);
  }

  public void subscribeUser(String userId) {
    User currentUser = getCurrentUser();
    currentUser.addToSubscribedToUsers(userId);
    User user = userRepository.findById(userId).orElseThrow(
      () -> new IllegalArgumentException("Cannot find user with userId " + userId
    ));
    user.addToSubscribers(userId);

    userRepository.save(currentUser);
    userRepository.save(user);
  }

  public void unSubscribeUser(String userId) {
    User currentUser = getCurrentUser();
    currentUser.removeFromSubscribedToUsers(userId);
    User user = getUserById(userId);
    user.removeFromSubscribers(userId);

    userRepository.save(currentUser);
    userRepository.save(user);
  }

  public Set<String> userHistory(String userId) {
    User user = getUserById(userId);
    return user.getVideoHistory();
  }

  private User getUserById(String userId) {
    return userRepository.findById(userId).orElseThrow(
      () -> new IllegalArgumentException("Cannot find user with userId " + userId
    ));
  }
}
