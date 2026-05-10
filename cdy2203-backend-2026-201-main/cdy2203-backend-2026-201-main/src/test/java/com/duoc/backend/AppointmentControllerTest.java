package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentControllerTest {

    private final AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
    private final PatientRepository patientRepository = mock(PatientRepository.class);
    private final AppointmentController controller = new AppointmentController();

    AppointmentControllerTest() {
        ReflectionTestUtils.setField(controller, "appointmentRepository", appointmentRepository);
        ReflectionTestUtils.setField(controller, "patientRepository", patientRepository);
    }

    @Test
    void createAppointmentReturnsBadRequestWhenPatientIsMissing() {
        Appointment appointment = new Appointment();
        appointment.setPatientId(5L);
        when(patientRepository.existsById(5L)).thenReturn(false);

        ResponseEntity<?> response = controller.createAppointment(appointment);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Patient not found for patientId: 5", response.getBody());
    }

    @Test
    void createAppointmentReturnsCreatedWhenPatientExists() {
        Appointment appointment = new Appointment(2L, LocalDate.now(), LocalTime.NOON, "Checkup", "Dr. Ana");
        when(patientRepository.existsById(2L)).thenReturn(true);

        ResponseEntity<?> response = controller.createAppointment(appointment);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(appointment, response.getBody());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void getAppointmentByIdReturnsNotFoundWhenMissing() {
        when(appointmentRepository.findById(4L)).thenReturn(Optional.empty());

        ResponseEntity<Appointment> response = controller.getAppointmentById(4L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAppointmentsForPatientReturnsNotFoundWhenPatientDoesNotExist() {
        when(patientRepository.existsById(3L)).thenReturn(false);

        ResponseEntity<List<Appointment>> response = controller.getAppointmentsForPatient(3L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateAppointmentReturnsBadRequestWhenPatientIsInvalid() {
        Appointment appointment = new Appointment();
        appointment.setPatientId(9L);
        when(appointmentRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.existsById(9L)).thenReturn(false);

        ResponseEntity<String> response = controller.updateAppointment(1L, appointment);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Patient not found for patientId: 9", response.getBody());
    }

    @Test
    void updateAppointmentReturnsOkWhenDataIsValid() {
        Appointment appointment = new Appointment();
        appointment.setPatientId(2L);
        when(appointmentRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.existsById(2L)).thenReturn(true);

        ResponseEntity<String> response = controller.updateAppointment(1L, appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Appointment updated successfully", response.getBody());
        assertEquals(1L, appointment.getId());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void deleteAppointmentReturnsServerErrorWhenDeletionFails() {
        when(appointmentRepository.existsById(8L)).thenReturn(true);
        doThrow(new RuntimeException("locked")).when(appointmentRepository).deleteById(8L);

        ResponseEntity<String> response = controller.deleteAppointment(8L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error deleting appointment: locked", response.getBody());
    }
}
