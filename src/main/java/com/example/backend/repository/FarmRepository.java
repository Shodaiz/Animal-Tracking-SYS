package com.example.backend.repository;

import com.example.backend.model.Farm;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    // ✅ findByOwner — la relation est maintenant farms.owner (FK vers users)
    List<Farm> findByOwner(User owner);
    List<Farm> findByStatus(String status);
}