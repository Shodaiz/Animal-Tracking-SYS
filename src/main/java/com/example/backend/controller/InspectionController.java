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
@RequestMapping("/api/inspection")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InspectionController {

    private final InspectionRepository inspectionRepository;
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;

    // Créer une inspection
    @PostMapping("/declare")
    public ResponseEntity<?> declareInspection(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        User inspector = getAuthenticatedUser(authentication);
        if (inspector == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Non authentifié"));
        }

        String description = request.getOrDefault("description", "").toString();
        if (description.isBlank()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "La description est obligatoire"));
        }

        Inspection inspection = new Inspection();
        inspection.setInspector(inspector);
        inspection.setDescription(description);
        inspection.setConstatType(request.getOrDefault("constatType", "General").toString());
        inspection.setResult(request.getOrDefault("result", "Pending").toString());

        // Animal optionnel
        if (request.get("animalId") != null) {
            Long animalId = Long.valueOf(request.get("animalId").toString());
            animalRepository.findById(animalId).ifPresent(inspection::setAnimal);
        }

        inspectionRepository.save(inspection);

        return ResponseEntity.ok(Map.of(
                "message", "Inspection déclarée avec succès",
                "inspectionId", inspection.getId()
        ));
    }

    // Liste toutes les inspections
    @GetMapping("/list")
    public ResponseEntity<?> getAllInspections() {
        return ResponseEntity.ok(inspectionRepository.findAll());
    }

    // Mes inspections (inspector connecté)
    @GetMapping("/my")
    public ResponseEntity<?> getMyInspections(Authentication authentication) {
        User inspector = getAuthenticatedUser(authentication);
        if (inspector == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(inspectionRepository.findByInspector(inspector));
    }

    // Vérification UHF — compare les tags scannés avec la base
    @PostMapping("/verify-scan")
    public ResponseEntity<?> verifyScan(@RequestBody Map<String, Object> request) {

        Long farmId = Long.valueOf(request.get("farmId").toString());
        @SuppressWarnings("unchecked")
        List<String> scannedCodes = (List<String>) request.get("scannedTags");

        Farm farm = farmRepository.findById(farmId).orElse(null);
        if (farm == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Ferme non trouvée"));
        }

        // Tags enregistrés dans la BD pour cette ferme
        List<Animal> registeredAnimals = animalRepository.findByFarm(farm);
        List<String> registeredCodes = registeredAnimals.stream()
                .filter(a -> a.getRfidTag() != null)
                .map(a -> a.getRfidTag().getRfidCode())
                .collect(Collectors.toList());

        List<String> unknownTags = scannedCodes.stream()
                .filter(tag -> !registeredCodes.contains(tag))
                .collect(Collectors.toList());

        List<String> missingTags = registeredCodes.stream()
                .filter(tag -> !scannedCodes.contains(tag))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "farmName", farm.getName(),
                "registeredCount", registeredCodes.size(),
                "scannedCount", scannedCodes.size(),
                "difference", registeredCodes.size() - scannedCodes.size(),
                "unknownTags", unknownTags,
                "missingTags", missingTags,
                "isConsistent", unknownTags.isEmpty() && missingTags.isEmpty()
        ));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}