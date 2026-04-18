package com.example.backend.controller;

import com.example.backend.model.Animal;
import com.example.backend.model.Farm;
import com.example.backend.model.User;
import com.example.backend.repository.AnimalRepository;
import com.example.backend.repository.FarmRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FarmerController {

    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;

    // Scan RFID — retourne les infos de l'animal
    @GetMapping("/scan/{rfidCode}")
    public ResponseEntity<?> scanAnimal(
            @PathVariable String rfidCode,
            Authentication authentication) {

        User farmer = getAuthenticatedUser(authentication);
        if (farmer == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Non authentifié"));
        }

        //Cherche via la relation rfidTag.rfidCode
        Animal animal = animalRepository.findByRfidTagRfidCode(rfidCode).orElse(null);

        if (animal == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Animal non trouvé avec le tag : " + rfidCode));
        }

        // Vérifier que l'animal appartient bien à ce farmer
        if (!animal.getOwner().getId().equals(farmer.getId())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Accès refusé : cet animal ne vous appartient pas"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", animal.getId());
        response.put("rfidCode", animal.getRfidTag() != null ? animal.getRfidTag().getRfidCode() : null);
        response.put("species", animal.getSpecies());
        response.put("breed", animal.getBreed());
        response.put("gender", animal.getGender());
        response.put("lifeStatus", animal.getLifeStatus());
        response.put("healthStatus", animal.getHealthStatus());
        response.put("farmName", animal.getFarm().getName());
        response.put("farmLocation", animal.getFarm().getLocation());
        return ResponseEntity.ok(response);
    }

    // Liste tous les animaux du farmer connecté
    @GetMapping("/animals")
    public ResponseEntity<?> getMyAnimals(Authentication authentication) {
        User farmer = getAuthenticatedUser(authentication);
        if (farmer == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Non authentifié"));
        }

        // ✅ Cherche par owner
        List<Animal> animals = animalRepository.findByOwner(farmer);
        return ResponseEntity.ok(animals);
    }

    // Liste les fermes du farmer connecté
    @GetMapping("/farms")
    public ResponseEntity<?> getMyFarms(Authentication authentication) {
        User farmer = getAuthenticatedUser(authentication);
        if (farmer == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Non authentifié"));
        }

        List<Farm> farms = farmRepository.findByOwner(farmer);
        return ResponseEntity.ok(farms);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}