package com.tinderbot.telegram.service.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiUnauthorizedResponseWriter {

    private static final String UNAUTHORIZED_DETAIL = "Отсутствует или неверный API-ключ / JWT";
    private static final String UNAUTHORIZED_TITLE = "Требуется авторизация";

    private final byte[] unauthorizedBody;

    public ApiUnauthorizedResponseWriter(JsonMapper jsonMapper) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, UNAUTHORIZED_DETAIL);
        problem.setTitle(UNAUTHORIZED_TITLE);
        this.unauthorizedBody = jsonMapper.writeValueAsBytes(problem);
    }

    public void write(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setContentLength(unauthorizedBody.length);
        response.getOutputStream().write(unauthorizedBody);
    }
}
