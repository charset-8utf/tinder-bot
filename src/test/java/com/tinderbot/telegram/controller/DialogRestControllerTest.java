package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.exception.GlobalExceptionHandler;
import com.tinderbot.telegram.mapper.DateChatApiMapperImpl;
import com.tinderbot.telegram.mapper.TextGenerationApiMapperImpl;
import com.tinderbot.telegram.service.dialog.DialogRestService;
import com.tinderbot.telegram.model.DateChatResult;
import com.tinderbot.telegram.model.DateMessageResult;
import com.tinderbot.telegram.model.TextGenerationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DialogRestController.class)
@Import({TextGenerationApiMapperImpl.class, DateChatApiMapperImpl.class, GlobalExceptionHandler.class})
class DialogRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DialogRestService dialogRestService;

    @Test
    void askGpt_returnsGeneratedText() throws Exception {
        when(dialogRestService.askGpt(eq(1L), eq("Hello?")))
                .thenReturn(new TextGenerationResult(Optional.of("GPT answer"), false, false));

        mockMvc.perform(post("/api/v1/sessions/1/gpt/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"Hello?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENERATED"))
                .andExpect(jsonPath("$.generatedText").value("GPT answer"));
    }

    @Test
    void generateNextMessage_returnsEmptyHistoryStatus() throws Exception {
        when(dialogRestService.generateNextMessage(1L))
                .thenReturn(new TextGenerationResult(Optional.empty(), false, true));

        mockMvc.perform(post("/api/v1/sessions/1/messages/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENERATION_EMPTY_HISTORY"));
    }

    @Test
    void askGpt_blankQuestion_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/1/gpt/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendDateMessage_returnsReply() throws Exception {
        when(dialogRestService.sendDateMessage(1L, "Hi", "date_grande"))
                .thenReturn(new DateMessageResult(
                        new DateChatResult(false, "typing", new TextGenerationResult(java.util.Optional.of("Hi back"), false, false)),
                        1,
                        5));

        mockMvc.perform(post("/api/v1/sessions/1/date/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Hi","starKey":"date_grande"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPLIED"))
                .andExpect(jsonPath("$.reply").value("Hi back"))
                .andExpect(jsonPath("$.messagesUsed").value(1))
                .andExpect(jsonPath("$.messagesLimit").value(5));
    }
}
