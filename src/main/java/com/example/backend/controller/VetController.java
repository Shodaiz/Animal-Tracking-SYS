package com.example.backend.controller;

import com.example.backend.model.Animal;
import com.example.backend.model.HealthRecord;
import com.example.backend.repository.AnimalRepository;
import com.example.backend.repository.HealthRecordRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VetController {

    private final AnimalRepository animalRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    // Scan RFID - Affiche la fiche technique complète
    @GetMapping("/scan/{rfidTag}")
    public ResponseEntity<?> scanAnimalHealth(
            @PathVariable String rfidTag) {

        Animal animal = animalRepository.findByRfidTag(rfidTag).orElse(null);

        if (animal == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Animal non trouvé avec le tag : " + rfidTag);
            return ResponseEntity.status(404).body(error);
        }

        // Récupérer tous les dossiers de santé
        List<HealthRecord> healthRecords =
                healthRecordRepository.findByAnimal(animal);

        // Construire la fiche complète
        Map<String, Object> response = new HashMap<>();
        response.put("rfidTag", animal.getRfidTag());
        response.put("species", animal.getSpecies());
        response.put("breed", animal.getBreed());
        response.put("gender", animal.getGender());
        response.put("status", animal.getStatus());
        response.put("farmName", animal.getFarm().getName());
        response.put("healthRecords", healthRecords);

        return ResponseEntity.ok(response);
    }

    // Ajouter un dossier de santé (vaccination, maladie, etc.)
    @PostMapping("/health-record")
    public ResponseEntity<?> addHealthRecord(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String rfidTag = request.get("rfidTag");
        Animal animal = animalRepository.findByRfidTag(rfidTag).orElse(null);

        if (animal == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Animal non trouvé");
            return ResponseEntity.status(404).body(error);
        }

        String username = authentication.getName();
        var vet = userRepository.findByUsername(username).orElse(null);

        HealthRecord record = new HealthRecord();
        record.setAnimal(animal);
        record.setRecordType(request.get("recordType"));
        record.setDiagnosis(request.get("diagnosis"));
        record.setTreatment(request.get("treatment"));
        record.setVisitDate(LocalDate.now());
        record.setVeterinarian(vet);

        healthRecordRepository.save(record);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Dossier médical ajouté avec succès");
        return ResponseEntity.ok(response);
    }
}