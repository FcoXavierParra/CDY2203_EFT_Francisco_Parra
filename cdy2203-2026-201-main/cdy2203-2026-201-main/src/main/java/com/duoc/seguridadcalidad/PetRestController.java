package com.duoc.seguridadcalidad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
public class PetRestController {

    private static final Logger log = LoggerFactory.getLogger(PetRestController.class);
    private final BackendService backendService;

    public PetRestController(BackendService backendService) {
        this.backendService = backendService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        log.debug("GET /api/pets");
        List<Map<String, Object>> pets = backendService.getPets();
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailable() {
        log.debug("GET /api/pets/available");
        List<Map<String, Object>> pets = backendService.getAvailablePets();
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String status
    ) {
        log.debug("GET /api/pets/search species={} gender={} location={} age={} status={}", species, gender, location, age, status);
        List<Map<String, Object>> pets = backendService.searchPets(species, gender, location, age, status);
        return ResponseEntity.ok(pets);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Object> pet
    ) {
        log.debug("POST /api/pets Authorization={} payload={}", authorizationHeader, pet);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("POST /api/pets missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7);
        Map<String, Object> saved = backendService.createPet(token, pet);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Object> pet
    ) {
        log.debug("PUT /api/pets/{} Authorization={} payload={}", id, authorizationHeader, pet);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("PUT /api/pets/{} missing or invalid Authorization header", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7);
        Map<String, Object> updated = backendService.updatePet(token, id, pet);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        log.debug("DELETE /api/pets/{} Authorization={}", id, authorizationHeader);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("DELETE /api/pets/{} missing or invalid Authorization header", id);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authorizationHeader.substring(7);
        Map<String, Object> deleted = backendService.deletePet(token, id);
        return ResponseEntity.ok(deleted);
    }
}
