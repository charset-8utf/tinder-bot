package com.tinderbot.telegram.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
public class UserSessionEntity {

    @Id
    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private UserSessionPayload payload;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void touchTimestamp() {
        updatedAt = Instant.now();
    }
}
