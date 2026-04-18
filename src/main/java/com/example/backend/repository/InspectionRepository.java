// ─── InspectionRepository.java ───────────────────────────────────────────────
package com.example.backend.repository;

import com.example.backend.model.Inspection;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    List<Inspection> findByInspector(User inspector);
    List<Inspection> findByResult(String result);
    List<Inspection> findByStatus(String status);
}