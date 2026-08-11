package com.busgo.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    @Value("${app.jwt.refresh_expiration_ms:86400000}")
    private long refreshTtlMs;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    public RefreshToken createToken(UUID userId) {
        RefreshToken t = new RefreshToken();
        t.setId(UUID.randomUUID());
        t.setToken(UUID.randomUUID().toString());
        t.setUserId(userId);
        t.setExpiry(OffsetDateTime.now().plusSeconds(refreshTtlMs / 1000));
        repo.deleteByUserId(userId); // remove old tokens
        return repo.save(t);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    public void revoke(UUID userId) {
        repo.deleteByUserId(userId);
    }

    public void revokeByToken(String token) {
        repo.deleteByToken(token);
    }

    /**
     * Rotate an existing refresh token: create a new token for the same user and delete the old one.
     * Returns the newly created RefreshToken.
     */
    public Optional<RefreshToken> rotateToken(String oldToken) {
        Optional<RefreshToken> ot = repo.findByToken(oldToken);
        if (ot.isEmpty()) return Optional.empty();
        RefreshToken existing = ot.get();
        if (existing.getExpiry().isBefore(OffsetDateTime.now())) {
            // expired; remove it
            repo.deleteByToken(oldToken);
            return Optional.empty();
        }
        // create new token
        RefreshToken t = new RefreshToken();
        t.setId(UUID.randomUUID());
        t.setToken(UUID.randomUUID().toString());
        t.setUserId(existing.getUserId());
        t.setExpiry(OffsetDateTime.now().plusSeconds(refreshTtlMs / 1000));
        // persist new and delete old
        repo.deleteByToken(oldToken);
        return Optional.of(repo.save(t));
    }
}
