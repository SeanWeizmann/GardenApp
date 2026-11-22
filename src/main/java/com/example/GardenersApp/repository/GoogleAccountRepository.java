package com.example.GardenersApp.repository;

import com.example.GardenersApp.domain.GoogleAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface GoogleAccountRepository extends JpaRepository<GoogleAccount, Long> {
    Optional<GoogleAccount> findByGoogleId(String googleId);

    Optional<GoogleAccount> findFirstByOrderByIdAsc();

    Optional<GoogleAccount> findBySessionTokenHashAndSessionExpiresAtAfter(String hash, Instant now);
}
