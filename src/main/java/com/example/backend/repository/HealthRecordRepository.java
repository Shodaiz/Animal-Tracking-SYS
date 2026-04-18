package com.example.backend.repository;


import com.example.backend.model.Animal;
import com.example.backend.model.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByAnimal(Animal animal);
}