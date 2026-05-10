package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PetRestControllerTest {

    private final BackendService backendService = mock(BackendService.class);
    private final PetRestController controller = new PetRestController(backendService);

    @Test
    void getAllReturnsPets() {
        List<Map<String, Object>> pets = List.of(Map.of("id", 1, "name", "Luna"));
        when(backendService.getPets()).thenReturn(pets);

        ResponseEntity<List<Map<String, Object>>> response = controller.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pets, response.getBody());
    }

    @Test
    void getAvailableReturnsPets() {
        List<Map<String, Object>> pets = List.of(Map.of("status", "Available"));
        when(backendService.getAvailablePets()).thenReturn(pets);

        ResponseEntity<List<Map<String, Object>>> response = controller.getAvailable();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pets, response.getBody());
    }

    @Test
    void searchDelegatesAllFiltersToBackendService() {
        List<Map<String, Object>> pets = List.of(Map.of("species", "Dog"));
        when(backendService.searchPets("Dog", "Male", "Santiago", 3, "Available")).thenReturn(pets);

        ResponseEntity<List<Map<String, Object>>> response =
                controller.search("Dog", "Male", "Santiago", 3, "Available");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pets, response.getBody());
    }

    @Test
    void createReturnsUnauthorizedWithoutBearerToken() {
        ResponseEntity<Map<String, Object>> response = controller.create(null, Map.of("name", "Luna"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createReturnsCreatedWhenHeaderIsValid() {
        Map<String, Object> pet = Map.of("name", "Luna");
        when(backendService.createPet("jwt-token", pet)).thenReturn(pet);

        ResponseEntity<Map<String, Object>> response = controller.create("Bearer jwt-token", pet);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(pet, response.getBody());
    }

    @Test
    void updateReturnsUnauthorizedWithoutBearerToken() {
        ResponseEntity<Map<String, Object>> response = controller.update(4, "Token abc", Map.of("name", "Luna"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteReturnsUnauthorizedWithoutBearerToken() {
        ResponseEntity<Map<String, Object>> response = controller.delete(4, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deleteReturnsDeletedPetWhenHeaderIsValid() {
        Map<String, Object> deleted = Map.of("id", 4, "name", "Luna");
        when(backendService.deletePet("jwt-token", 4)).thenReturn(deleted);

        ResponseEntity<Map<String, Object>> response = controller.delete(4, "Bearer jwt-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deleted, response.getBody());
        verify(backendService).deletePet("jwt-token", 4);
    }
}
