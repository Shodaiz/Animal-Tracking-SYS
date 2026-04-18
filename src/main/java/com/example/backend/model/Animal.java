package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "animals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfid_tag", unique = true, nullable = false, length = 50)
    private String rfidTag;

    @Column(length = 50)
    private String species;
    // Ovin, Bovin, Caprin

    @Column(length = 100)
    private String breed;

    @Column(length = 10)
    private String gender;
    // Mâle, Femelle

    @Column(length = 20)
    private String status = "Active";
    // Active, Sold, Quarantined, Lost, Dead

    @Column(length = 50)
    private String color;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_place", length = 100)
    private String birthPlace;

    @Column(name = "acquisition_place", length = 100)
    private String acquisitionPlace;

    @Column(name = "origin_type", length = 20)
    private String originType;
    // Born, Purchased, Imported

    @Column(name = "health_status", length = 20)
    private String healthStatus = "Healthy";
    // Healthy, Under_treatment, Critical

    @Column(name = "reproduction_status", length = 20)
    private String reproductionStatus = "None";
    // None, Pregnant, Breeding, Castrated

    @Column(name = "vaccination_status", length = 20)
    private String vaccinationStatus = "Not_vaccinated";
    // Vaccinated, Not_vaccinated, Partial

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id")
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(name = "mother_rfid", length = 50)
    private String motherRfid;

    @Column(name = "father_rfid", length = 50)
    private String fatherRfid;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HealthRecord> healthRecords;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}