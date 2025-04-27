package com.trustline.trustline.appuser.repository;

import com.trustline.trustline.appuser.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);
    Optional<User> findByGoogleId(String googleId);
}
