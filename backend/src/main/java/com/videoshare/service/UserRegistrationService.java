package com.videoshare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoshare.dto.UserInfoDto;
import com.videoshare.model.User;
import com.videoshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserRegistrationService {

  @Value("${auth0.userInfoEndpoint}")
  private String userInfoEndpoint;

  private final UserRepository userRepository;

  public String registerUser(String tokenValue) {
    HttpRequest httpRequest = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create(userInfoEndpoint))
      .setHeader( "Authorization", String.format("Bearer %s", tokenValue))
      .build();

    HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build();

    try {
      HttpResponse<String> responseString = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      String body = responseString.body();

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      UserInfoDto userInfoDto =objectMapper.readValue(body, UserInfoDto.class);

      Optional<User> userBySubject = userRepository.findBySub(userInfoDto.getSub());
      User user = new User();
      if(userBySubject.isPresent()) {
        return userBySubject.get().getId();
      } else {
        user.setFirstName(userInfoDto.getGivenName());
        user.setLastName(userInfoDto.getFamilyName());
        user.setFullName(userInfoDto.getName());
        user.setEmailAddress(userInfoDto.getEmail());
        user.setSub(userInfoDto.getSub());
      }

      return userRepository.save(user).getId();
    } catch (Exception exception) {
      throw new RuntimeException("Exception occurred while registering user", exception);
    }
    
  }
}
