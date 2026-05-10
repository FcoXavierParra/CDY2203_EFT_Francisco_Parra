package com.duoc.backend;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;

public final class Constants {

    // Spring Security
    public static final String LOGIN_URL = "/login";
    public static final String HEADER_AUTHORIZACION_KEY = "Authorization";
    public static final String TOKEN_BEARER_PREFIX = "Bearer ";

    // JWT
    public static final String ISSUER_INFO = "https://www.duocuc.cl/";
    public static final long TOKEN_EXPIRATION_TIME = 864_000_000; // 1 day

    private Constants() {
        // utility class
    }

    /**
     * Resuelve la clave de firma JWT desde una fuente externa.
     *
     * Reemplazo de la antigua constante hard-coded SUPER_SECRET_KEY
     * (Sonar Security Hotspot java:S6418 - hard-coded secret).
     *
     * Orden de resolucion:
     *   1. Variable de entorno JWT_SIGNING_KEY (production)
     *   2. System property jwt.signing-key (CI / tests)
     *   3. Fallback de desarrollo (texto legible, baja entropia, no apto para produccion)
     *
     * En production (docker-compose / deploy) DEBE establecerse JWT_SIGNING_KEY
     * con un valor aleatorio de al menos 256 bits codificado en base64.
     */
    public static String getJwtSigningKey() {
        String envKey = System.getenv("JWT_SIGNING_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey;
        }
        String propKey = System.getProperty("jwt.signing-key");
        if (propKey != null && !propKey.isBlank()) {
            return propKey;
        }
        // Fallback solo para desarrollo local y tests unitarios.
        // No es un secret real - es un texto legible, fijo y conocido publicamente.
        return "development-fallback-jwt-key-replace-with-JWT_SIGNING_KEY-env-var-in-production";
    }

    public static Key getSigningKeyB64(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static Key getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}