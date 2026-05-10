package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendServiceTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final BackendService backendService = new BackendService(restTemplate, "http://backend.test");

    @Test
    void loginStripsBearerPrefixFromToken() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("alice");
        authRequest.setPassword("secret");
        when(restTemplate.postForEntity("http://backend.test/login", authRequest, String.class))
                .thenReturn(ResponseEntity.ok("Bearer jwt-token"));

        AuthResponse response = backendService.login(authRequest);

        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void getPetsReturnsEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(
                eq("http://backend.test/pets"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        List<Map<String, Object>> pets = backendService.getPets("jwt-token");

        assertTrue(pets.isEmpty());
    }

    @Test
    void getPatientsReturnsEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(
                eq("http://backend.test/patients"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        List<Map<String, Object>> patients = backendService.getPatients("jwt-token");

        assertTrue(patients.isEmpty());
    }

    @Test
    void getPatientByIdReturnsNullOnNotFound() {
        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                new byte[0],
                null
        );
        when(restTemplate.exchange(
                eq("http://backend.test/patients/5"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenThrow(notFound);

        Map<String, Object> patient = backendService.getPatientById("jwt-token", 5L);

        assertNull(patient);
    }

    @Test
    void createPatientReturnsSavedMap() {
        Map<String, Object> patient = Map.of("name", "Milo");
        when(restTemplate.postForObject(eq("http://backend.test/patients"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(patient);

        Map<String, Object> result = backendService.createPatient("jwt-token", patient);

        assertEquals(patient, result);
    }

    @Test
    void updatePatientReturnsUpdatedMap() {
        Map<String, Object> patient = Map.of("name", "Milo");
        when(restTemplate.exchange(
                eq("http://backend.test/patients/7"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(ResponseEntity.ok(patient));

        Map<String, Object> result = backendService.updatePatient("jwt-token", 7L, patient);

        assertEquals(patient, result);
    }

    @Test
    void deletePatientCallsBackendDeleteEndpoint() {
        when(restTemplate.exchange(
                eq("http://backend.test/patients/7"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)))
                .thenReturn(ResponseEntity.noContent().build());

        backendService.deletePatient("jwt-token", 7L);

        verify(restTemplate).exchange(
                eq("http://backend.test/patients/7"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void createPetReturnsSavedMap() {
        Map<String, Object> pet = Map.of("name", "Luna");
        when(restTemplate.postForObject(eq("http://backend.test/pets"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(pet);

        Map<String, Object> result = backendService.createPet("jwt-token", pet);

        assertEquals(pet, result);
    }

    @Test
    void getAppointmentsReturnsEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(
                eq("http://backend.test/appointments"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        List<Map<String, Object>> appointments = backendService.getAppointments("jwt-token");

        assertTrue(appointments.isEmpty());
    }

    @Test
    void createAppointmentReturnsSavedMap() {
        Map<String, Object> appointment = Map.of("reason", "Checkup");
        when(restTemplate.postForObject(eq("http://backend.test/appointments"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(appointment);

        Map<String, Object> result = backendService.createAppointment("jwt-token", appointment);

        assertEquals(appointment, result);
    }

    @Test
    void publicGetPetsReturnsResponseBodyAsList() {
        when(restTemplate.exchange(
                eq("http://backend.test/pets"),
                eq(HttpMethod.GET),
                eq(null),
                eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[] {Map.of("id", 1)}));

        List<Map<String, Object>> pets = backendService.getPets();

        assertEquals(1, pets.size());
        assertEquals(1, pets.get(0).get("id"));
    }

    @Test
    void getAvailablePetsReturnsEmptyListWhenBodyIsNull() {
        when(restTemplate.exchange(
                eq("http://backend.test/pets/available"),
                eq(HttpMethod.GET),
                eq(null),
                eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(null));

        List<Map<String, Object>> pets = backendService.getAvailablePets();

        assertTrue(pets.isEmpty());
    }

    @Test
    void searchPetsBuildsUriWithOnlyProvidedFilters() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), eq(null), eq(Map[].class)))
                .thenReturn(ResponseEntity.ok(new Map[] {Map.of("id", 1)}));

        List<Map<String, Object>> pets = backendService.searchPets("Dog", null, "Santiago", null, "Available");

        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.GET), eq(null), eq(Map[].class));
        String builtUri = uriCaptor.getValue();

        assertTrue(builtUri.startsWith("http://backend.test/pets/search"));
        assertTrue(builtUri.contains("species=Dog"));
        assertTrue(builtUri.contains("location=Santiago"));
        assertTrue(builtUri.contains("status=Available"));
        assertTrue(!builtUri.contains("gender="));
        assertTrue(!builtUri.contains("age="));
        assertEquals(1, pets.size());
    }

    @Test
    void updatePetReturnsUpdatedMap() {
        Map<String, Object> pet = Map.of("name", "Luna");
        when(restTemplate.exchange(
                eq("http://backend.test/pets/4"),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(ResponseEntity.ok(pet));

        Map<String, Object> result = backendService.updatePet("jwt-token", 4, pet);

        assertEquals(pet, result);
    }

    @Test
    void deletePetReturnsDeletedMap() {
        Map<String, Object> pet = Map.of("deleted", true);
        when(restTemplate.exchange(
                eq("http://backend.test/pets/4"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Map.class)))
                .thenReturn(ResponseEntity.ok(pet));

        Map<String, Object> result = backendService.deletePet("jwt-token", 4);

        assertEquals(pet, result);
    }

    @Test
    void loginReturnsTokenEvenWhenBackendDoesNotUseBearerPrefix() {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("alice");
        authRequest.setPassword("secret");
        when(restTemplate.postForEntity("http://backend.test/login", authRequest, String.class))
                .thenReturn(ResponseEntity.ok("plain-token"));

        AuthResponse response = backendService.login(authRequest);

        assertNotNull(response);
        assertEquals("plain-token", response.getToken());
    }
}
