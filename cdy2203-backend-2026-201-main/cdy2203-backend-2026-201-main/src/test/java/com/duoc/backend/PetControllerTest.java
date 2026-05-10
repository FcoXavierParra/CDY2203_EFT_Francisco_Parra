package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PetControllerTest {

    private final PetRepository petRepository = mock(PetRepository.class);
    private final PetController controller = new PetController();

    PetControllerTest() {
        ReflectionTestUtils.setField(controller, "petRepository", petRepository);
    }

    @Test
    void createPetReturnsCreatedWhenSaveSucceeds() {
        Pet pet = new Pet();
        pet.setName("Luna");

        ResponseEntity<?> response = controller.createPet(pet);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(pet, response.getBody());
        verify(petRepository).save(pet);
    }

    @Test
    void createPetReturnsBadRequestWhenSaveFails() {
        Pet pet = new Pet();
        doThrow(new RuntimeException("db error")).when(petRepository).save(pet);

        ResponseEntity<?> response = controller.createPet(pet);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al registrar mascota: db error", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void getAvailablePetsReturnsRepositoryResult() {
        List<Pet> pets = List.of(new Pet());
        when(petRepository.findByStatus("available")).thenReturn(pets);

        ResponseEntity<List<Pet>> response = controller.getAvailablePets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pets, response.getBody());
    }

    @Test
    void getPetByIdReturnsNotFoundWhenMissing() {
        when(petRepository.findById(4L)).thenReturn(Optional.empty());

        ResponseEntity<Pet> response = controller.getPetById(4L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updatePetReturnsUpdatedPetWhenFound() {
        Pet existing = new Pet();
        existing.setName("Old");
        existing.setSpecies("Dog");
        existing.setStatus("available");
        when(petRepository.findById(2L)).thenReturn(Optional.of(existing));

        Pet update = new Pet();
        update.setName("New");
        update.setLocation("Santiago");
        update.setStatus("adopted");

        ResponseEntity<?> response = controller.updatePet(2L, update);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Pet updated = (Pet) response.getBody();
        assertEquals("New", updated.getName());
        assertEquals("Santiago", updated.getLocation());
        assertEquals("adopted", updated.getStatus());
        verify(petRepository).save(existing);
    }

    @Test
    void deletePetReturnsBadRequestWhenRepositoryThrows() {
        when(petRepository.findById(7L)).thenReturn(Optional.of(new Pet()));
        doThrow(new RuntimeException("locked")).when(petRepository).deleteById(7L);

        ResponseEntity<?> response = controller.deletePet(7L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al eliminar mascota: locked", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void searchPetsUsesAllCriteriaBranch() {
        List<Pet> pets = List.of(new Pet());
        when(petRepository.findBySpeciesAndGenderAndLocationAndAgeAndStatus("Dog", "Male", "Santiago", 3, "available"))
                .thenReturn(pets);

        ResponseEntity<List<Pet>> response = controller.searchPets("Dog", "Male", "Santiago", 3, "available");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pets, response.getBody());
    }

    @Test
    void searchPetsUsesSpeciesAndStatusBranch() {
        List<Pet> pets = List.of(new Pet());
        when(petRepository.findBySpeciesAndStatus("Cat", "available")).thenReturn(pets);

        ResponseEntity<List<Pet>> response = controller.searchPets("Cat", null, null, null, "available");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pets, response.getBody());
    }

    @Test
    void searchPetsUsesDefaultStatusBranch() {
        List<Pet> pets = List.of(new Pet());
        when(petRepository.findByStatus("available")).thenReturn(pets);

        ResponseEntity<List<Pet>> response = controller.searchPets(null, null, null, null, "available");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pets, response.getBody());
    }

    @Test
    void searchPetsReturnsServerErrorWhenRepositoryFails() {
        when(petRepository.findByStatus("available")).thenThrow(new RuntimeException("db error"));

        ResponseEntity<List<Pet>> response = controller.searchPets(null, null, null, null, "available");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
