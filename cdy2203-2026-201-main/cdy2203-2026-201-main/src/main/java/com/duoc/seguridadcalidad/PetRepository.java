package com.duoc.seguridadcalidad;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PetRepository {

    private final List<Pet> pets = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong sequence = new AtomicLong(1);

    public List<Pet> findAll() {
        return new ArrayList<>(pets);
    }

    public Pet save(Pet pet) {
        if (pet.getId() == null) {
            pet.setId(sequence.getAndIncrement());
        }
        pets.add(pet);
        return pet;
    }
}
