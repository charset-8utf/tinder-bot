package com.tinderbot.telegram.service.session;

import com.tinderbot.telegram.entity.UserSessionEntity;
import com.tinderbot.telegram.mapper.UserSessionMapper;
import com.tinderbot.telegram.model.UserSession;
import com.tinderbot.telegram.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
class SessionPersistence {

    private final UserSessionRepository repository;
    private final UserSessionMapper sessionMapper;
    private final Map<Long, UserSession> memory = new ConcurrentHashMap<>();

    void evictMemoryCache() {
        memory.clear();
    }

    UserSession getOrCreate(Long chatId) {
        return memory.computeIfAbsent(chatId, id ->
                repository.findById(id)
                        .map(entity -> sessionMapper.toDomain(entity.getPayload()))
                        .orElseGet(UserSession::new));
    }

    <T> T read(Long chatId, Function<UserSession, T> reader) {
        return reader.apply(getOrCreate(chatId));
    }

    void write(Long chatId, Consumer<UserSession> updater) {
        UserSession session = getOrCreate(chatId);
        updater.accept(session);
        flush(chatId);
    }

    void mutateIfPresent(Long chatId, Consumer<UserSession> updater) {
        loadExisting(chatId).ifPresent(session -> {
            updater.accept(session);
            flush(chatId);
        });
    }

    void delete(Long chatId) {
        memory.remove(chatId);
        repository.deleteById(chatId);
    }

    private Optional<UserSession> loadExisting(Long chatId) {
        UserSession cached = memory.get(chatId);
        if (cached != null) {
            return Optional.of(cached);
        }
        return repository.findById(chatId)
                .map(entity -> {
                    UserSession session = sessionMapper.toDomain(entity.getPayload());
                    memory.put(chatId, session);
                    return session;
                });
    }

    private void flush(Long chatId) {
        UserSession session = memory.get(chatId);
        if (session == null) {
            return;
        }
        UserSessionEntity entity = repository.findById(chatId).orElseGet(UserSessionEntity::new);
        entity.setTelegramUserId(chatId);
        entity.setPayload(sessionMapper.toPayload(session));
        repository.save(entity);
    }
}
