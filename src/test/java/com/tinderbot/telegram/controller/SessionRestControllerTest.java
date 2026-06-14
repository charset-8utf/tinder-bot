package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.dto.SessionResponse;
import com.tinderbot.telegram.exception.GlobalExceptionHandler;
import com.tinderbot.telegram.exception.SessionNotFoundException;
import com.tinderbot.telegram.model.DialogMode;
import com.tinderbot.telegram.service.dialog.DialogRestService;
import com.tinderbot.telegram.service.session.SessionLifecycleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SessionRestController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class SessionRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionLifecycleService sessionLifecycleService;

    @MockitoBean
    private DialogRestService dialogRestService;

    @Test
    void getSession_returnsOk() throws Exception {
        when(sessionLifecycleService.getSession(42L))
                .thenReturn(new SessionResponse(42L, DialogMode.MAIN, 0, 0, null, 0, 0, 0));

        mockMvc.perform(get("/api/v1/sessions/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").value(42))
                .andExpect(jsonPath("$.currentMode").value("MAIN"));
    }

    @Test
    void updateMode_returnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/v1/sessions/42/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mode":"GPT"}
                                """))
                .andExpect(status().isNoContent());

        verify(dialogRestService).updateMode(42L, DialogMode.GPT);
    }

    @Test
    void deleteSession_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/sessions/42"))
                .andExpect(status().isNoContent());

        verify(sessionLifecycleService).deleteSession(42L);
    }

    @Test
    void deleteSession_whenMissing_returnsNotFound() throws Exception {
        doThrow(new SessionNotFoundException(99L)).when(sessionLifecycleService).deleteSession(99L);

        mockMvc.perform(delete("/api/v1/sessions/99").accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound());
    }
}
