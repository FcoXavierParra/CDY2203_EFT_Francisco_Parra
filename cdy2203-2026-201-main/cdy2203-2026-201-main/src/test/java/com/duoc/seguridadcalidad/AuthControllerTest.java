package com.duoc.seguridadcalidad;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private final BackendService backendService = mock(BackendService.class);
    private final AuthController controller = new AuthController(backendService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAuthenticationTokenStoresSecurityContextAndReturnsToken() {
        AuthRequest requestBody = new AuthRequest();
        requestBody.setUsername("alice");
        requestBody.setPassword("secret");
        when(backendService.login(requestBody)).thenReturn(new AuthResponse("jwt-token"));

        MockHttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<?> response = controller.createAuthenticationToken(requestBody, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);
        assertEquals("jwt-token", ((AuthResponse) response.getBody()).getToken());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("alice", SecurityContextHolder.getContext().getAuthentication().getName());

        HttpSession session = request.getSession(false);
        assertNotNull(session);
        assertNotNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
    }

    @Test
    void createAuthenticationTokenReturnsBackendStatusOnHttpError() {
        AuthRequest requestBody = new AuthRequest();
        requestBody.setUsername("alice");
        requestBody.setPassword("secret");
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                null,
                "Invalid credentials".getBytes(),
                null
        );
        when(backendService.login(requestBody)).thenThrow(exception);

        ResponseEntity<?> response = controller.createAuthenticationToken(requestBody, new MockHttpServletRequest());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void createAuthenticationTokenReturnsServiceUnavailableWhenBackendIsDown() {
        AuthRequest requestBody = new AuthRequest();
        requestBody.setUsername("alice");
        requestBody.setPassword("secret");
        when(backendService.login(requestBody)).thenThrow(new ResourceAccessException("timeout"));

        ResponseEntity<?> response = controller.createAuthenticationToken(requestBody, new MockHttpServletRequest());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("El servicio de backend no esta disponible.", response.getBody());
    }

    @Test
    void createAuthenticationTokenReturnsInternalServerErrorOnUnexpectedException() {
        AuthRequest requestBody = new AuthRequest();
        requestBody.setUsername("alice");
        requestBody.setPassword("secret");
        when(backendService.login(requestBody)).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = controller.createAuthenticationToken(requestBody, new MockHttpServletRequest());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error interno del servidor", response.getBody());
    }

    @Test
    void logoutInvalidatesSessionAndClearsSecurityContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("alice", null)
        );

        ResponseEntity<Void> response = controller.logout(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(session.isInvalid());
    }
}
