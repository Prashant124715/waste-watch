package com.wastewatch.controller;

import com.wastewatch.dto.auth.AuthResponseDto;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.auth.UserProfileDto;
import com.wastewatch.security.UserPrincipal;
import com.wastewatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        AuthResponseDto response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("User is not authenticated");
        }
        UserProfileDto profile = authService.getCurrentUserProfile(currentUser.getUsername());
        return ResponseEntity.ok(profile);
    }
}
