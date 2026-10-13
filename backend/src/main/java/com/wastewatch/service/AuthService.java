package com.wastewatch.service;

import com.wastewatch.dto.auth.AuthResponseDto;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.auth.UserProfileDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.UserRepository;
import com.wastewatch.security.JwtTokenProvider;
import com.wastewatch.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       AuditLogRepository auditLogRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public AuthResponseDto login(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Phase 14: Record login audit event (safe: no passwords/tokens stored)
        AppUser user = userRepository.findById(userPrincipal.getId()).orElse(null);
        AuditLog auditLog = new AuditLog(
                user,
                "LOGIN",
                "AppUser",
                userPrincipal.getId().toString(),
                null,
                String.format("User '%s' logged in successfully with role %s",
                        userPrincipal.getUsername(), userPrincipal.getRole())
        );
        auditLogRepository.save(auditLog);

        return new AuthResponseDto(
                jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                userPrincipal.getFullName(),
                userPrincipal.getRole()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus()
        );
    }
}
