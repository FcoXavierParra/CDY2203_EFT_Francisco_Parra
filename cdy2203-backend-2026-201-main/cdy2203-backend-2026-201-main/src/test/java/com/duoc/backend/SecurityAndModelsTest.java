package com.duoc.backend;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityAndModelsTest {

    @Test
    void jwtAuthenticationConfigGeneratesBearerTokenWithUsernameSubject() {
        JWTAuthenticationConfig config = new JWTAuthenticationConfig();

        String token = config.getJWTToken("alice");

        assertTrue(token.startsWith("Bearer "));
        String compact = token.substring(7);
        String subject = Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) Constants.getSigningKey(Constants.getJwtSigningKey()))
                .build()
                .parseSignedClaims(compact)
                .getPayload()
                .getSubject();
        assertEquals("alice", subject);
    }

    @Test
    void constantsExposeExpectedSecurityValuesAndSigningKeys() {
        assertEquals("/login", Constants.LOGIN_URL);
        assertEquals("Authorization", Constants.HEADER_AUTHORIZACION_KEY);
        assertEquals("Bearer ", Constants.TOKEN_BEARER_PREFIX);
        assertEquals("https://www.duocuc.cl/", Constants.ISSUER_INFO);
        Key utf8Key = Constants.getSigningKey(Constants.getJwtSigningKey());
        // getSigningKeyB64 espera base64 valido. Usamos un literal base64 fijo para
        // este test - el fallback de getJwtSigningKey() es texto legible (no base64).
        Key b64Key = Constants.getSigningKeyB64("ZGV2ZWxvcG1lbnRGYWxsYmFja0p3dEtleVJlcGxhY2VXaXRoSnd0U2lnbmluZ0tleUVudlZhcg==");
        assertNotNull(utf8Key);
        assertNotNull(b64Key);
    }

    @Test
    void backendModelsStoreExpectedValues() {
        Patient patient = new Patient("Milo", "Dog", "Mix", 4, "Ana", "ana@mail.com", "123");
        patient.setId(2L);

        Appointment appointment = new Appointment(2L, LocalDate.of(2026, 5, 2), LocalTime.NOON, "Checkup", "Dr. Ana");
        appointment.setId(3L);

        Pet pet = new Pet("Luna", "Dog", "Beagle", 3, "Female", "Santiago", List.of("img1"));
        pet.setId(4L);
        pet.setStatus("adopted");

        User user = new User();
        user.setId(5);
        user.setUsername("alice");
        user.setEmail("alice@mail.com");
        user.setPassword("secret");

        LoginRequest loginRequest = new LoginRequest("alice", "secret");

        assertEquals(2L, patient.getId());
        assertEquals("Milo", patient.getName());
        assertEquals(3L, appointment.getId());
        assertEquals(2L, appointment.getPatientId());
        assertEquals("Checkup", appointment.getReason());
        assertEquals(4L, pet.getId());
        assertEquals("adopted", pet.getStatus());
        assertEquals(5, user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("alice@mail.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals("alice", loginRequest.getUsername());
        assertEquals("secret", loginRequest.getPassword());
    }
}
