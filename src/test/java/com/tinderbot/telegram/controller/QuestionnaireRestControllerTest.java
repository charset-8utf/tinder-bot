package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.exception.GlobalExceptionHandler;
import com.tinderbot.telegram.mapper.QuestionnaireApiMapperImpl;
import com.tinderbot.telegram.mapper.TextGenerationApiMapperImpl;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireGenerationService;
import com.tinderbot.telegram.service.questionnaire.QuestionnaireService;
import com.tinderbot.telegram.service.session.RestSessionAccessService;
import com.tinderbot.telegram.model.QuestionnaireProgress;
import com.tinderbot.telegram.model.QuestionnaireType;
import com.tinderbot.telegram.model.TextGenerationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuestionnaireRestController.class)
@Import({QuestionnaireApiMapperImpl.class, TextGenerationApiMapperImpl.class, GlobalExceptionHandler.class})
class QuestionnaireRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionnaireService questionnaireService;

    @MockitoBean
    private QuestionnaireGenerationService generationService;

    @MockitoBean
    private RestSessionAccessService sessionAccessService;

    @Test
    void startQuestionnaire_returnsStartedStatus() throws Exception {
        when(questionnaireService.start(1L, QuestionnaireType.PROFILE))
                .thenReturn(new QuestionnaireProgress.Started("intro", "profile", "Question?"));

        mockMvc.perform(post("/api/v1/sessions/1/questionnaires/PROFILE/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.question").value("Question?"));
    }

    @Test
    void submitAnswer_returnsNextQuestion() throws Exception {
        when(questionnaireService.submitAnswer(eq(1L), eq(QuestionnaireType.OPENER), anyString()))
                .thenReturn(new QuestionnaireProgress.NextQuestion("Next?"));

        mockMvc.perform(post("/api/v1/sessions/1/questionnaires/OPENER/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"answer":"Anna"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEXT_QUESTION"))
                .andExpect(jsonPath("$.question").value("Next?"));
    }

    @Test
    void generate_withoutCompletedQuestionnaire_returns409() throws Exception {
        when(questionnaireService.completedState(1L, QuestionnaireType.PROFILE))
                .thenThrow(new IllegalStateException("Опросник не заполнен"));

        mockMvc.perform(post("/api/v1/sessions/1/questionnaires/PROFILE/generate")
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void generate_returnsGeneratedText() throws Exception {
        when(questionnaireService.completedState(1L, QuestionnaireType.PROFILE))
                .thenReturn(new QuestionnaireProgress.Completed("thinking", "prompt", "data"));
        when(generationService.generate(anyLong(), eq(QuestionnaireType.PROFILE), anyString(), anyString()))
                .thenReturn(new TextGenerationResult(Optional.of("Generated profile"), false, false));

        mockMvc.perform(post("/api/v1/sessions/1/questionnaires/PROFILE/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENERATED"))
                .andExpect(jsonPath("$.generatedText").value("Generated profile"));
    }
}
