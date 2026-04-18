package com.example.backend.controller;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/constat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConstatController {

    private final ConstatRepository constatRepository;
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;

    // Déclarer un constat (Contrôleur uniquement)
    @PostMapping("/declare")
    public ResponseEntity<?> declareConstat(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String username = authentication.getName();
        User controller = userRepository.findByUsername(username).orElse(null);

        Long farmId = Long.valueOf(request.get("farmId").toString());
        Farm farm = farmRepository.findById(farmId).orElse(null);

        if (farm == null || controller == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Données invalides"));
        }

        Constat constat = new Constat();
        constat.setController(controller);
        constat.setFarm(farm);
        constat.setDescription(request.get("description").toString());
        constatRepository.save(constat);

        return ResponseEntity.ok(Map.of(
                "message", "Constat déclaré avec succès",
                "constatId", constat.getId()
        ));
    }

    // Liste tous les constats (Contrôleur uniquement)
    @GetMapping("/list")
    public ResponseEntity<?> getAllConstats() {
        List<Constat> constats = constatRepository.findAll();
        return ResponseEntity.ok(constats);
    }
}