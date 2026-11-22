package com.example.GardenersApp.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "google_accounts", indexes = @Index(name = "idx_google_account_session", columnList = "session_token_hash", unique = true))
public class GoogleAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 128)
    private String googleId;
    @Column(nullable = false, length = 320)
    private String email;
    @Column(length = 160)
    private String displayName;
    @Column(length = 2048)
    private String accessToken;
    @Column(length = 2048)
    private String refreshToken;
    @Column(name = "session_token_hash", nullable = false, length = 64)
    private String sessionTokenHash;
    @Column(nullable = false)
    private Instant sessionExpiresAt;
    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected GoogleAccount() {
    }

    public GoogleAccount(String googleId, String email, String displayName, String accessToken,
                         String refreshToken, String sessionTokenHash, Instant sessionExpiresAt) {
        this.googleId = googleId;
        this.email = email;
        this.displayName = displayName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.sessionTokenHash = sessionTokenHash;
        this.sessionExpiresAt = sessionExpiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getSessionTokenHash() {
        return sessionTokenHash;
    }

    public Instant getSessionExpiresAt() {
        return sessionExpiresAt;
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        if (refreshToken != null && !refreshToken.isBlank()) this.refreshToken = refreshToken;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
        this.updatedAt = Instant.now();
    }

    public void updateSession(String hash, Instant expiresAt) {
        this.sessionTokenHash = hash;
        this.sessionExpiresAt = expiresAt;
    }
}
