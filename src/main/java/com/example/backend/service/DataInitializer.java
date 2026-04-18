package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final AnimalRepository animalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        Farm farm1 = new Farm();
        farm1.setName("Ferme El Baraka");
        farm1.setLocation("Alger");
        farmRepository.save(farm1);

        Farm farm2 = new Farm();
        farm2.setName("Ferme Ouled Djellal");
        farm2.setLocation("Biskra");
        farmRepository.save(farm2);

        User farmer = new User();
        farmer.setUsername("fermier1");
        farmer.setPassword(passwordEncoder.encode("password123"));
        farmer.setRole("ROLE_FARMER");
        farmer.setFarm(farm1);
        userRepository.save(farmer);

        User vet = new User();
        vet.setUsername("docteur1");
        vet.setPassword(passwordEncoder.encode("password123"));
        vet.setRole("ROLE_VET");
        userRepository.save(vet);

        User controller = new User();
        controller.setUsername("controleur1");
        controller.setPassword(passwordEncoder.encode("password123"));
        controller.setRole("ROLE_CONTROLLER");
        userRepository.save(controller);

        Animal animal1 = new Animal();
        animal1.setRfidTag("DZ-0007");
        animal1.setSpecies("Ovin");
        animal1.setBreed("Ouled Djellal");
        animal1.setGender("Mâle");
        animal1.setStatus("Active");
        animal1.setFarm(farm1);
        animalRepository.save(animal1);

        Animal animal2 = new Animal();
        animal2.setRfidTag("DZ-0008");
        animal2.setSpecies("Bovin");
        animal2.setBreed("Charolais");
        animal2.setGender("Femelle");
        animal2.setStatus("Active");
        animal2.setFarm(farm1);
        animalRepository.save(animal2);

        Animal animal3 = new Animal();
        animal3.setRfidTag("DZ-0009");
        animal3.setSpecies("Ovin");
        animal3.setBreed("Hamra");
        animal3.setGender("Femelle");
        animal3.setStatus("Active");
        animal3.setFarm(farm2);
        animalRepository.save(animal3);

        System.out.println("✅ Données de test insérées avec succès !");
    }
}