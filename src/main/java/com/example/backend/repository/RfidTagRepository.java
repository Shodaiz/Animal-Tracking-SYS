// ─── RfidTagRepository.java ───────────────────────────────────────────────────
package com.example.backend.repository;

import com.example.backend.model.RfidTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RfidTagRepository extends JpaRepository<RfidTag, Long> {
    Optional<RfidTag> findByRfidCode(String rfidCode);
    boolean existsByRfidCode(String rfidCode);
}