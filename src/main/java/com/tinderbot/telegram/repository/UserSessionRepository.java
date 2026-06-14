package com.tinderbot.telegram.repository;

import com.tinderbot.telegram.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, Long> {
}
