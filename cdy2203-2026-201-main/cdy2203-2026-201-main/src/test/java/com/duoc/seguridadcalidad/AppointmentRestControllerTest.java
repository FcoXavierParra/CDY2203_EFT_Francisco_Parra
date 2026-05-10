package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppointmentRestControllerTest {

    private final BackendService backendService = mock(BackendService.class);
    private final AppointmentRestController controller = new AppointmentRestController(backendService);

    @Test
    void getAllReturnsUnauthorizedWithoutBearerToken() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getAll(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getAllReturnsAppointmentsWhenHeaderIsValid() {
        List<Map<String, Object>> appointments = List.of(Map.of("id", 1L, "reason", "Checkup"));
        when(backendService.getAppointments("jwt-token")).thenReturn(appointments);

        ResponseEntity<List<Map<String, Object>>> response = controller.getAll("Bearer jwt-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(appointments, response.getBody());
    }

    @Test
    void createReturnsUnauthorizedWithoutBearerToken() {
        ResponseEntity<Map<String, Object>> response = controller.create("Token 123", Map.of("reason", "Checkup"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createReturnsSavedAppointmentWhenHeaderIsValid() {
        Map<String, Object> appointment = Map.of("reason", "Checkup");
        when(backendService.createAppointment("jwt-token", appointment)).thenReturn(appointment);

        ResponseEntity<Map<String, Object>> response = controller.create("Bearer jwt-token", appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(appointment, response.getBody());
    }
}
