package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginControllerTest {

    private final JWTAuthenticationConfig jwtAuthenticationConfig = mock(JWTAuthenticationConfig.class);
    private final MyUserDetailsService userDetailsService = mock(MyUserDetailsService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final LoginController controller = new LoginController();

    LoginControllerTest() {
        ReflectionTestUtils.setField(controller, "jwtAuthenticationConfig", jwtAuthenticationConfig);
        ReflectionTestUtils.setField(controller, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(controller, "passwordEncoder", passwordEncoder);
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("alice", "secret");
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(jwtAuthenticationConfig.getJWTToken("alice")).thenReturn("Bearer token");

        ResponseEntity<String> response = controller.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bearer token", response.getBody());
    }

    @Test
    void loginReturnsBadRequestWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("alice", "wrong");
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        ResponseEntity<String> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void loginReturnsBadRequestWhenServiceThrowsException() {
        LoginRequest request = new LoginRequest("alice", "secret");
        when(userDetailsService.loadUserByUsername("alice")).thenThrow(new RuntimeException("missing user"));

        ResponseEntity<String> response = controller.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Login failed: missing user", response.getBody());
    }
}
