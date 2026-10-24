package com.wastewatch.repository;

import com.wastewatch.entity.AppUser;
import com.wastewatch.entity.enums.UserRole;
import com.wastewatch.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    List<AppUser> findByRole(UserRole role);
    List<AppUser> findByRoleAndStatus(UserRole role, UserStatus status);
    long countByRoleAndStatus(UserRole role, UserStatus status);
}
