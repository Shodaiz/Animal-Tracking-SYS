package com.example.backend.controller;

import com.example.backend.model.Animal;
import com.example.backend.model.User;
import com.example.backend.repository.AnimalRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FarmerController {

    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    // Scan RFID - Affiche les infos de l'animal
    // Le fermier ne peut voir que les animaux de SA ferme
    @GetMapping("/scan/{rfidTag}")
    public ResponseEntity<?> scanAnimal(
            @PathVariable String rfidTag,
            Authentication authentication) {

        // Récupérer le fermier connecté
        String username = authentication.getName();
        User farmer = userRepository.findByUsername(username).orElse(null);

        if (farmer == null || farmer.getFarm() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Fermier non associé à une ferme");
            return ResponseEntity.status(403).body(error);
        }

        // Chercher l'animal par tag RFID
        Animal animal = animalRepository.findByRfidTag(rfidTag).orElse(null);

        if (animal == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Animal non trouvé avec le tag : " + rfidTag);
            return ResponseEntity.status(404).body(error);
        }

        // Vérifier que l'animal appartient à la ferme du fermier connecté
        if (!animal.getFarm().getId().equals(farmer.getFarm().getId())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Accès refusé : cet animal n'appartient pas à votre ferme");
            return ResponseEntity.status(403).body(error);
        }

        // Construire la réponse avec les infos de l'animal
        Map<String, Object> response = new HashMap<>();
        response.put("id", animal.getId());
        response.put("rfidTag", animal.getRfidTag());
        response.put("species", animal.getSpecies());
        response.put("breed", animal.getBreed());
        response.put("gender", animal.getGender());
        response.put("status", animal.getStatus());
        response.put("farmName", animal.getFarm().getName());
        response.put("farmLocation", animal.getFarm().getLocation());

        return ResponseEntity.ok(response);
    }

    // Liste tous les animaux de la ferme du fermier connecté
    @GetMapping("/animals")
    public ResponseEntity<?> getMyAnimals(Authentication authentication) {

        String username = authentication.getName();
        User farmer = userRepository.findByUsername(username).orElse(null);

        if (farmer == null || farmer.getFarm() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Fermier non associé à une ferme");
            return ResponseEntity.status(403).body(error);
        }

        var animals = animalRepository.findByFarm(farmer.getFarm());

        return ResponseEntity.ok(animals);
    }
}