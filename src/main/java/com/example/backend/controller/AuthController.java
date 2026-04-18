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

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    // ✅ Tous les champs DOIVENT être final pour que @RequiredArgsConstructor fonctionne
    //    C'est la cause de l'erreur "repo not initialized in default constructor"
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Identifiant ou mot de passe incorrect"));
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Compte désactivé"));
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());
        response.put("username", user.getUsername());
        response.put("userId", user.getId());
        // ✅ BD finale a first_name + last_name
        response.put("fullName", user.getFullName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username  = request.get("username");
        String password  = request.get("password");
        String email     = request.get("email");
        String firstName = request.get("firstName");
        String lastName  = request.get("lastName");
        String role      = request.get("role");
        String phone     = request.get("phone");

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Ce nom d'utilisateur est déjà pris"));
        }
        if (email != null && userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Cet email est déjà utilisé"));
        }

        // ✅ Rôles valides alignés sur l'ENUM de la BD finale
        List<String> validRoles = List.of("Farmer", "Veterinarian", "Inspector", "Administrator");
        if (role == null || !validRoles.contains(role)) {
            role = "Farmer";
        }

        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .firstName(firstName != null ? firstName : username)
                .lastName(lastName != null ? lastName : "")
                .role(role)
                .phone(phone)
                .isActive(true)
                .build();

        userRepository.save(newUser);

        // ✅ Si Farmer, créer une ferme avec owner (owner_id NOT NULL dans la BD)
        if (role.equals("Farmer")) {
            Farm farm = Farm.builder()
                    .name("Ferme de " + newUser.getFullName())
                    .location("À définir")
                    .owner(newUser)  // ✅ owner obligatoire
                    .build();
            farmRepository.save(farm);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Compte créé avec succès",
                "username", username,
                "role", role
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Veuillez fournir un identifiant ou email"));
        }

        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) userOpt = userRepository.findByEmail(identifier);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "Aucun compte trouvé"));
        }

        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "message", "Compte trouvé. Vous pouvez réinitialiser votre mot de passe.",
                "username", user.getUsername(),
                "email", user.getEmail() != null ? maskEmail(user.getEmail()) : "Non défini"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String identifier  = request.get("identifier");
        String newPassword = request.get("newPassword");

        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Le mot de passe doit contenir au moins 6 caractères"));
        }

        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) userOpt = userRepository.findByEmail(identifier);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Utilisateur non trouvé"));
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }

    private String maskEmail(String email) {
        if (!email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        return (local.length() <= 2 ? "***" : local.charAt(0) + "***") + "@" + parts[1];
    }
}