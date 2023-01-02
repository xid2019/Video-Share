package com.videoshare.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.videoshare.service.UserRegistrationService;
import com.videoshare.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

  private final UserRegistrationService userRegistrationService;
  private final UserService userService;

  @GetMapping("/register")
  @ResponseStatus(HttpStatus.OK)
  public String register(Authentication authentication) {
    Jwt jwt = (Jwt)authentication.getPrincipal();

    return userRegistrationService.registerUser(jwt.getTokenValue());
  }

  @PostMapping("subscribe/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public boolean subscribeUser(@PathVariable String userId) {
    userService.subscribeUser(userId);
    return true;
  }

  @PostMapping("unSubscribe/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public boolean unSubscribeUser(@PathVariable String userId) {
    userService.unSubscribeUser(userId);
    return true;
  }

  @GetMapping("/{userId}/history")
  @ResponseStatus(HttpStatus.OK)
  public Set<String> userHistory(@PathVariable String userId) {
    return userService.userHistory(userId);
  }
}
