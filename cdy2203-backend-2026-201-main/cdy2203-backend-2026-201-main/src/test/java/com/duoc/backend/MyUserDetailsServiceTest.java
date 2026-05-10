package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MyUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final MyUserDetailsService service = new MyUserDetailsService();

    MyUserDetailsServiceTest() {
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
    }

    @Test
    void loadUserByUsernameReturnsUserWhenFound() {
        User user = new User();
        user.setUsername("alice");
        when(userRepository.findByUsername("alice")).thenReturn(user);

        org.springframework.security.core.userdetails.UserDetails result = service.loadUserByUsername("alice");

        assertSame(user, result);
    }

    @Test
    void loadUserByUsernameThrowsWhenUserIsMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
    }

    @Test
    void passwordEncoderReturnsBCryptImplementation() {
        PasswordEncoder encoder = service.passwordEncoder();

        assertInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class, encoder);
    }
}
