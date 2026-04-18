package com.example.backend.controller;

import com.example.backend.model.Farm;
import com.example.backend.model.User;
import com.example.backend.repository.FarmRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final FarmRepository farmRepository;

    // ─── LOGIN ──────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Identifiant ou mot de passe incorrect"));
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());
        response.put("username", user.getUsername());
        response.put("userId", user.getId());
        response.put("fullName", user.getFullName() != null ? user.getFullName() : user.getUsername());

        if (user.getFarm() != null) {
            response.put("farmId", user.getFarm().getId());
            response.put("farmName", user.getFarm().getName());
        }

        return ResponseEntity.ok(response);
    }

    // ─── INSCRIPTION ─────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username  = request.get("username");
        String password  = request.get("password");
        String email     = request.get("email");
        String fullName  = request.get("fullName");
        String role      = request.get("role");
        String phone     = request.get("phone");

        // Vérifier si username existe déjà
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Ce nom d'utilisateur est déjà pris"));
        }

        // Vérifier si email existe déjà
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Cet email est déjà utilisé"));
        }

        // Valider le rôle
        if (role == null || (!role.equals("ROLE_FARMER") &&
                !role.equals("ROLE_VET") && !role.equals("ROLE_CONTROLLER"))) {
            role = "ROLE_FARMER"; // Rôle par défaut
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRole(role);
        newUser.setPhone(phone);

        // Si fermier, créer une ferme automatiquement
        if (role.equals("ROLE_FARMER")) {
            Farm farm = new Farm();
            farm.setName("Ferme de " + (fullName != null ? fullName : username));
            farm.setLocation("À définir");
            farmRepository.save(farm);
            newUser.setFarm(farm);
        }

        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of(
                "message", "Compte créé avec succès",
                "username", username,
                "role", role
        ));
    }

    // ─── MOT DE PASSE OUBLIÉ ─────────────────────
    // Vérifie si username OU email existe
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier"); // username ou email

        if (identifier == null || identifier.trim().isEmpty()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Veuillez fournir un identifiant ou email"));
        }

        // Chercher par username OU email
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(identifier);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Aucun compte trouvé avec cet identifiant ou email"));
        }

        User user = userOpt.get();

        return ResponseEntity.ok(Map.of(
                "message", "Compte trouvé. Vous pouvez réinitialiser votre mot de passe.",
                "username", user.getUsername(),
                "email", user.getEmail() != null ? maskEmail(user.getEmail()) : "Non défini"
        ));
    }

    // ─── RÉINITIALISATION MOT DE PASSE ──────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String identifier   = request.get("identifier"); // username ou email
        String newPassword  = request.get("newPassword");

        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Le mot de passe doit contenir au moins 6 caractères"));
        }

        // Chercher par username OU email
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(identifier);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Utilisateur non trouvé"));
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }

    // Masquer l'email pour la sécurité (ex: a***@gmail.com)
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return "***@" + domain;
        return local.charAt(0) + "***@" + domain;
    }
}