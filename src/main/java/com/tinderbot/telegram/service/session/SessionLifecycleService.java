package com.tinderbot.telegram.service.session;

import com.tinderbot.telegram.api.session.SessionStore;
import com.tinderbot.telegram.dto.SessionResponse;
import com.tinderbot.telegram.exception.SessionNotFoundException;
import com.tinderbot.telegram.mapper.SessionApiMapper;
import com.tinderbot.telegram.model.UserSession;
import com.tinderbot.telegram.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionLifecycleService {

    private final SessionStore sessionStore;
    private final UserSessionRepository repository;
    private final SessionApiMapper sessionApiMapper;
    private final RestSessionAccessService sessionAccessService;

    @Transactional(readOnly = true)
    public SessionResponse getSession(Long chatId) {
        sessionAccessService.ensureCanAccessSession(chatId);
        UserSession session = sessionStore.getOrCreate(chatId);
        return sessionApiMapper.toResponse(chatId, session);
    }

    @Transactional
    public void deleteSession(Long chatId) {
        sessionAccessService.ensureCanAccessSession(chatId);
        if (!repository.existsById(chatId)) {
            throw new SessionNotFoundException(chatId);
        }
        sessionStore.delete(chatId);
    }
}
