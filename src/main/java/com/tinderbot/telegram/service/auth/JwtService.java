package com.tinderbot.telegram.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class JwtService {

    private static final Pattern JWT_FORMAT = Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");

    private final JsonMapper jsonMapper;
    private final String secret;
    private final long expirationMinutes;

    public JwtService(
            JsonMapper jsonMapper,
            @Value("${tinderbot.api.security.jwt.secret}") String secret,
            @Value("${tinderbot.api.security.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.jsonMapper = jsonMapper;
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public record ParsedAuthorizationHeader(String bearerCredential, boolean jwtFormat) {
    }

    public String createAccessToken(String username) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + expirationMinutes * 60;

        ObjectNode header = jsonMapper.createObjectNode();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        ObjectNode payload = jsonMapper.createObjectNode();
        payload.put("sub", username);
        payload.put("iat", issuedAt);
        payload.put("exp", expiresAt);

        String encodedHeader = base64UrlEncode(jsonMapper.writeValueAsBytes(header));
        String encodedPayload = base64UrlEncode(jsonMapper.writeValueAsBytes(payload));
        String unsignedToken = encodedHeader + "." + encodedPayload;
        return unsignedToken + "." + base64UrlEncode(sign(unsignedToken));
    }

    public Optional<ParsedAuthorizationHeader> parseAuthorizationHeader(String authorizationHeader) {
        String bearer = extractBearerToken(authorizationHeader);
        if (bearer == null) {
            return Optional.empty();
        }
        return Optional.of(new ParsedAuthorizationHeader(bearer, JWT_FORMAT.matcher(bearer).matches()));
    }

    public Optional<String> validateAuthorizationHeader(String authorizationHeader) {
        String bearer = extractBearerToken(authorizationHeader);
        if (bearer == null || !JWT_FORMAT.matcher(bearer).matches()) {
            return Optional.empty();
        }
        return parseSignedToken(bearer);
    }

    public Optional<String> validateAndExtractUsername(String token) {
        if (token == null || token.isBlank() || !JWT_FORMAT.matcher(token).matches()) {
            return Optional.empty();
        }
        return parseSignedToken(token);
    }

    private Optional<String> parseSignedToken(String validatedJwt) {
        String[] parts = validatedJwt.split("\\.", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }

        try {
            String unsignedToken = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsignedToken), base64UrlDecode(parts[2]))) {
                return Optional.empty();
            }

            byte[] payloadBytes = base64UrlDecode(parts[1]);
            JsonNode claims = jsonMapper.readTree(payloadBytes);
            JsonNode subject = claims.get("sub");
            JsonNode expiration = claims.get("exp");
            if (subject == null || !subject.isString() || expiration == null || !expiration.isNumber()) {
                return Optional.empty();
            }
            String username = subject.stringValue();
            if (Instant.now().getEpochSecond() >= expiration.longValue()) {
                return Optional.empty();
            }
            return isPlausibleUsername(username) ? Optional.of(username) : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorizationHeader.substring(7).trim();
        return token.isBlank() ? null : token;
    }
    
    private boolean isPlausibleUsername(String username) {
        if (username.isBlank() || username.length() > 64) {
            return false;
        }
        if (username.contains("..") || username.indexOf('/') >= 0 || username.indexOf('\\') >= 0) {
            return false;
        }
        for (int i = 0; i < username.length(); i++) {
            if (username.charAt(i) < 32 || username.charAt(i) == 127) {
                return false;
            }
        }
        return true;
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not sign JWT", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
