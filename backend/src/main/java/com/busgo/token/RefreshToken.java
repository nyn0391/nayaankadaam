package com.busgo.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private OffsetDateTime expiry;

    public RefreshToken() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public OffsetDateTime getExpiry() { return expiry; }
    public void setExpiry(OffsetDateTime expiry) { this.expiry = expiry; }
}
