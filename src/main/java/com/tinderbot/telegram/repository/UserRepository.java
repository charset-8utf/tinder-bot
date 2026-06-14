package com.tinderbot.telegram.repository;

import com.tinderbot.telegram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.credentials WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithCredentials(@Param("username") String username);

    Optional<UserEntity> findByTelegramUserId(Long telegramUserId);

    boolean existsByUsername(String username);
}
