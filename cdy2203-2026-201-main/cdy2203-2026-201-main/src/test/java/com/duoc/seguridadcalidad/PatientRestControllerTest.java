package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientRestControllerTest {

    private final BackendService backendService = mock(BackendService.class);
    private final PatientRestController controller = new PatientRestController(backendService);

    @Test
    void getAllReturnsUnauthorizedWithoutBearerToken() {
        ResponseEntity<List<Map<String, Object>>> response = controller.getAll(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAllReturnsPatientsWhenHeaderIsValid() {
        List<Map<String, Object>> patients = List.of(Map.of("id", 1L, "name", "Milo"));
        when(backendService.getPatients("jwt-token")).thenReturn(patients);

        ResponseEntity<List<Map<String, Object>>> response = controller.getAll("Bearer jwt-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(patients, response.getBody());
    }

    @Test
    void getByIdReturnsNotFoundWhenBackendReturnsNull() {
        when(backendService.getPatientById("jwt-token", 5L)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.getById("Bearer jwt-token", 5L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createReturnsUnauthorizedWithoutBearerToken() {
        ResponseEntity<Map<String, Object>> response = controller.create("Basic 123", Map.of("name", "Milo"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createReturnsSavedPatientWhenHeaderIsValid() {
        Map<String, Object> patient = Map.of("name", "Milo");
        when(backendService.createPatient("jwt-token", patient)).thenReturn(patient);

        ResponseEntity<Map<String, Object>> response = controller.create("Bearer jwt-token", patient);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(patient, response.getBody());
    }

    @Test
    void deleteReturnsServerErrorWhenBackendFails() {
        doThrow(new RuntimeException("backend error")).when(backendService).deletePatient("jwt-token", 9L);

        ResponseEntity<String> response = controller.delete("Bearer jwt-token", 9L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error deleting patient", response.getBody());
    }

    @Test
    void deleteReturnsOkWhenDeletionSucceeds() {
        ResponseEntity<String> response = controller.delete("Bearer jwt-token", 9L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Patient deleted", response.getBody());
        verify(backendService).deletePatient("jwt-token", 9L);
    }
}
