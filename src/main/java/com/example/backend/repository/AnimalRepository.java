package com.example.backend.repository;

import com.example.backend.model.Animal;
import com.example.backend.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByRfidTag(String rfidTag);
    List<Animal> findByFarm(Farm farm);
}