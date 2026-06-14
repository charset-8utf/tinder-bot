package com.tinderbot.telegram.controller;

import com.tinderbot.telegram.dto.LoginResponse;
import com.tinderbot.telegram.exception.GlobalExceptionHandler;
import com.tinderbot.telegram.service.auth.ApiAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthRestController.class)
@Import(GlobalExceptionHandler.class)
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiAuthService apiAuthService;

    @Test
    void login_returnsJwt() throws Exception {
        when(apiAuthService.login("demo", "password"))
                .thenReturn(Optional.of(new LoginResponse("jwt-token", "Bearer")));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(apiAuthService.login(anyString(), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"demo","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
