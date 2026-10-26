package com.wastewatch.service;

import com.wastewatch.dto.auth.AuthResponseDto;
import com.wastewatch.dto.auth.LoginRequestDto;
import com.wastewatch.dto.auth.RegisterRequestDto;
import com.wastewatch.dto.auth.UserProfileDto;
import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.AuditLog;
import com.wastewatch.entity.Notification;
import com.wastewatch.entity.enums.NotificationType;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.entity.enums.UserStatus;
import com.wastewatch.exception.DuplicateResourceException;
import com.wastewatch.exception.InvalidRequestException;
import com.wastewatch.exception.ResourceNotFoundException;
import com.wastewatch.repository.AuditLogRepository;
import com.wastewatch.repository.NotificationRepository;
import com.wastewatch.repository.UserRepository;
import com.wastewatch.security.JwtTokenProvider;
import com.wastewatch.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       AuditLogRepository auditLogRepository,
                       NotificationRepository notificationRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto login(LoginRequestDto loginRequest) {
        // Pre-validate account verification status
        AppUser existingUser = userRepository.findByUsernameOrEmail(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail())
                .orElse(null);

        if (existingUser != null) {
            if (existingUser.getStatus() == UserStatus.PENDING) {
                throw new AccessDeniedException(
                        "Your volunteer registration is pending administrator verification. Please wait for an administrator to review and approve your application.");
            }
            if (existingUser.getStatus() == UserStatus.REJECTED) {
                throw new AccessDeniedException(
                        "Your volunteer registration application was not approved. Please contact your municipal administrator for assistance.");
            }
            if (existingUser.getStatus() == UserStatus.INACTIVE || existingUser.getStatus() == UserStatus.SUSPENDED) {
                throw new AccessDeniedException(
                        "Your account is currently disabled or suspended. Please contact the administrator.");
            }
        }

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
                user.getStatus(),
                user.getCity(),
                user.getState()
        );
    }

    @Transactional
    public UserProfileDto registerVolunteer(RegisterRequestDto dto) {
        // 1. Password confirmation check
        if (dto.getConfirmPassword() != null && !dto.getConfirmPassword().isBlank()
                && !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }

        // 2. Duplicate checks
        String cleanUsername = dto.getUsername().trim();
        String cleanEmail = dto.getEmail().trim().toLowerCase();
        String cleanPhone = dto.getPhoneNumber().trim().replaceAll("[^0-9]", "");
        if (cleanPhone.length() > 10) {
            cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
        }

        if (userRepository.existsByUsername(cleanUsername)) {
            throw new DuplicateResourceException("Username '" + cleanUsername + "' is already taken");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateResourceException("An account with email '" + cleanEmail + "' already exists");
        }
        if (userRepository.existsByPhoneNumber(cleanPhone)) {
            throw new DuplicateResourceException("An account with mobile number '" + cleanPhone + "' already exists");
        }

        // 3. Save entity with strict volunteer role and PENDING status
        AppUser user = new AppUser();
        user.setUsername(cleanUsername);
        user.setEmail(cleanEmail);
        user.setFullName(dto.getFullName().trim());
        user.setPhoneNumber(cleanPhone);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(UserRole.VOLUNTEER); // Always assign VOLUNTEER role
        user.setStatus(UserStatus.PENDING); // Always enters PENDING state
        user.setCity(dto.getCity() != null ? dto.getCity().trim() : null);
        user.setState(dto.getState() != null ? dto.getState().trim() : null);
        user.setAddress(dto.getAddress() != null ? dto.getAddress().trim() : null);
        user.setReason(dto.getReason() != null ? dto.getReason().trim() : null);

        AppUser saved = userRepository.save(user);

        // 4. Record Governance Audit Log
        AuditLog auditLog = new AuditLog(
                saved,
                "VOLUNTEER_REGISTERED",
                "AppUser",
                saved.getId().toString(),
                null,
                String.format("New volunteer application submitted by '%s' (%s, %s) - Pending Admin Verification",
                        saved.getFullName(), saved.getUsername(), saved.getCity() != null ? saved.getCity() : "India")
        );
        auditLogRepository.save(auditLog);

        // 5. Send notification to administrators
        List<AppUser> admins = userRepository.findByRole(UserRole.ADMIN);
        for (AppUser admin : admins) {
            Notification notif = new Notification(
                    admin,
                    "New Volunteer Application: " + saved.getFullName(),
                    "Volunteer applicant " + saved.getFullName() +
                            " (" + (saved.getCity() != null ? saved.getCity() : "India") + ") submitted registration for review.",
                    NotificationType.SYSTEM
            );
            notificationRepository.save(notif);
        }

        return new UserProfileDto(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getPhoneNumber(),
                saved.getRole(),
                saved.getStatus(),
                saved.getCity(),
                saved.getState()
        );
    }
}
