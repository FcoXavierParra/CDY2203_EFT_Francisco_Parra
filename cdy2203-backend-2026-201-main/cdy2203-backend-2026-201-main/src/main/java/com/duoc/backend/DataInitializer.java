package com.duoc.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        ensureUserExists("user", "password", "user@example.com");
        ensureUserExists("admin", "password", "admin@example.com");
        ensureUserExists("manager", "password", "manager@example.com");

        if (patientRepository.count() == 0) {
            patientRepository.save(new Patient("can", "Perro", "quiltr0", 1, "Jhon Wick", "jhon.wick@example.com", "+56991234567"));
            patientRepository.save(new Patient("miau", "Gato", "siamés", 4, "Lara Croft", "lara.croft@example.com", "+56999887766"));
            patientRepository.save(new Patient("fido", "Perro", "Labrador", 3, "Tony Stark", "tony.stark@example.com", "+56911122233"));
            patientRepository.save(new Patient("luna", "Gato", "Bengal", 2, "Natasha Romanoff", "natasha.romanoff@example.com", "+56944455566"));
        }
    }

    private void ensureUserExists(String username, String rawPassword, String email) {
        if (userRepository.findByUsername(username) != null) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        userRepository.save(user);
    }
}
