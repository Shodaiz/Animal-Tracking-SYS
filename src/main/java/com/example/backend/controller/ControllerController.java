package com.example.backend.controller;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/controller")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ControllerController {

    private final AnimalRepository animalRepository;
    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final ConstatRepository constatRepository;

    // Vérification UHF - Compare les tags scannés avec la base de données
    @PostMapping("/verify-scan")
    public ResponseEntity<?> verifyScan(
            @RequestBody Map<String, Object> request) {

        Long farmId = Long.valueOf(request.get("farmId").toString());
        List<String> scannedTags = (List<String>) request.get("scannedTags");

        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Ferme non trouvée");
            return ResponseEntity.status(404).body(error);
        }

        // Animaux enregistrés dans la base pour cette ferme
        List<Animal> registeredAnimals = animalRepository.findByFarm(farm);
        List<String> registeredTags = registeredAnimals.stream()
                .map(Animal::getRfidTag)
                .collect(Collectors.toList());

        // Animaux scannés mais pas dans la base (suspects)
        List<String> unknownTags = scannedTags.stream()
                .filter(tag -> !registeredTags.contains(tag))
                .collect(Collectors.toList());

        // Animaux dans la base mais pas scannés (manquants)
        List<String> missingTags = registeredTags.stream()
                .filter(tag -> !scannedTags.contains(tag))
                .collect(Collectors.toList());

        // Construire le rapport de vérification
        Map<String, Object> response = new HashMap<>();
        response.put("farmName", farm.getName());
        response.put("registeredCount", registeredTags.size());
        response.put("scannedCount", scannedTags.size());
        response.put("difference", registeredTags.size() - scannedTags.size());
        response.put("unknownTags", unknownTags);
        response.put("missingTags", missingTags);
        response.put("isConsistent", unknownTags.isEmpty() && missingTags.isEmpty());

        return ResponseEntity.ok(response);
    }

    // Confirmer l'inventaire (Bouton CHECK)
    @PostMapping("/check")
    public ResponseEntity<?> confirmCheck(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        Long farmId = Long.valueOf(request.get("farmId").toString());
        String username = authentication.getName();

        Farm farm = farmRepository.findById(farmId).orElse(null);
        User controller = userRepository.findByUsername(username).orElse(null);

        if (farm == null || controller == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Données invalides"));
        }

        // Sauvegarder le constat de vérification
        Constat constat = new Constat();
        constat.setController(controller);
        constat.setFarm(farm);
        constat.setDescription("✅ Inventaire confirmé par " +
                username + " - Effectif validé");
        constatRepository.save(constat);

        return ResponseEntity.ok(Map.of(
                "message", "Inventaire confirmé et synchronisé avec succès",
                "farmName", farm.getName()
        ));
    }
}