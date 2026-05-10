package com.duoc.seguridadcalidad;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelsTest {

    @Test
    void authRequestStoresUsernameAndPassword() {
        AuthRequest request = new AuthRequest();

        request.setUsername("alice");
        request.setPassword("secret");

        assertEquals("alice", request.getUsername());
        assertEquals("secret", request.getPassword());
    }

    @Test
    void appointmentStoresAllFields() {
        Appointment appointment = new Appointment();
        LocalDate date = LocalDate.of(2026, 5, 2);
        LocalTime time = LocalTime.of(14, 30);

        appointment.setId(10L);
        appointment.setPetId(5L);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason("Checkup");
        appointment.setVeterinarian("Dr. Ana");

        assertEquals(10L, appointment.getId());
        assertEquals(5L, appointment.getPetId());
        assertEquals(date, appointment.getDate());
        assertEquals(time, appointment.getTime());
        assertEquals("Checkup", appointment.getReason());
        assertEquals("Dr. Ana", appointment.getVeterinarian());
    }

    @Test
    void petConstructorAndSettersExposeExpectedValues() {
        Pet pet = new Pet(1L, "Luna", "Dog", "Beagle", 3, "Female", "Santiago", List.of("img1"), "available", "Ana");

        pet.setBreed("Mix");
        pet.setStatus("adopted");

        assertEquals(1L, pet.getId());
        assertEquals("Luna", pet.getName());
        assertEquals("Dog", pet.getSpecies());
        assertEquals("Mix", pet.getBreed());
        assertEquals(3, pet.getAge());
        assertEquals("Female", pet.getGender());
        assertEquals("Santiago", pet.getLocation());
        assertEquals(List.of("img1"), pet.getPhotos());
        assertEquals("adopted", pet.getStatus());
        assertEquals("Ana", pet.getOwner());
    }
}
