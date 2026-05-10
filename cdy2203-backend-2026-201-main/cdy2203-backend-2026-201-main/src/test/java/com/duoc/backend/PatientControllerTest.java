package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientControllerTest {

    private final PatientRepository patientRepository = mock(PatientRepository.class);
    private final PatientController controller = new PatientController();

    PatientControllerTest() {
        ReflectionTestUtils.setField(controller, "patientRepository", patientRepository);
    }

    @Test
    void createPatientReturnsCreatedWhenSaveSucceeds() {
        Patient patient = new Patient("Milo", "Dog", "Labrador", 4, "Ana", "ana@mail.com", "123");

        ResponseEntity<?> response = controller.createPatient(patient);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(patient, response.getBody());
        verify(patientRepository).save(patient);
    }

    @Test
    void createPatientReturnsBadRequestWhenSaveFails() {
        Patient patient = new Patient();
        doThrow(new RuntimeException("db error")).when(patientRepository).save(patient);

        ResponseEntity<?> response = controller.createPatient(patient);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al registrar: db error", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void getAllPatientsReturnsOkWhenRepositoryResponds() {
        List<Patient> patients = List.of(new Patient(), new Patient());
        when(patientRepository.findAll()).thenReturn(patients);

        ResponseEntity<Iterable<Patient>> response = controller.getAllPatients();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(patients, response.getBody());
    }

    @Test
    void getPatientByIdReturnsNotFoundWhenMissing() {
        when(patientRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseEntity<Patient> response = controller.getPatientById(7L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deletePatientReturnsNotFoundWhenPatientDoesNotExist() {
        when(patientRepository.existsById(4L)).thenReturn(false);

        ResponseEntity<String> response = controller.deletePatient(4L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deletePatientReturnsServerErrorWhenDeletionFails() {
        when(patientRepository.existsById(4L)).thenReturn(true);
        doThrow(new RuntimeException("locked")).when(patientRepository).deleteById(4L);

        ResponseEntity<String> response = controller.deletePatient(4L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error deleting patient: locked", response.getBody());
    }
}
