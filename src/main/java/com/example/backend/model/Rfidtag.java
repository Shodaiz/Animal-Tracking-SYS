package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rfid_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfidTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfid_code", unique = true, nullable = false, length = 100)
    private String rfidCode;

    // UHF = longue portée (PDA), NFC = courte portée (téléphone)
    @Column(name = "tag_type", length = 10)
    private String tagType = "UHF";

    // InStock, Assigned, Defective, Lost
    @Column(name = "tag_status", length = 20)
    private String tagStatus = "InStock";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
