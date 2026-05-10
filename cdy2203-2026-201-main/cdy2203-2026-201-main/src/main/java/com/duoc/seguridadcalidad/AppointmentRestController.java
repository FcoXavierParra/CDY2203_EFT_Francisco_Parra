package com.duoc.seguridadcalidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentRestController.class);
    private final BackendService backendService;

    public AppointmentRestController(BackendService backendService) {
        this.backendService = backendService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        log.debug("GET /api/appointments Authorization={}", authorizationHeader);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("GET /api/appointments missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authorizationHeader.substring(7);
        List<Map<String, Object>> appointments = backendService.getAppointments(token);
        log.debug("GET /api/appointments returning {} appointments", appointments.size());
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                             @RequestBody Map<String, Object> appointment) {
        log.debug("POST /api/appointments Authorization={} payload={}", authorizationHeader, appointment);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("POST /api/appointments missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = authorizationHeader.substring(7);
        Map<String, Object> saved = backendService.createAppointment(token, appointment);
        log.debug("POST /api/appointments saved={}", saved);
        return ResponseEntity.ok(saved);
    }
}
