package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "health_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id")
    private User veterinarian;

    // Vaccination, Treatment, Disease, Checkup, Surgery, LabTest, Injury
    @Column(name = "record_type", nullable = false, length = 30)
    private String recordType;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    // ✅ treatment → treatment_plan (nom réel dans la BD)
    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    // ✅ LocalDate → LocalDateTime (BD déclare DATETIME NOT NULL)
    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    // ✅ Champs manquants dans l'ancienne version
    @Column(name = "is_validated")
    private Boolean isValidated = false;

    @Column(name = "geo_latitude", precision = 10, scale = 8)
    private BigDecimal geoLatitude;

    @Column(name = "geo_longitude", precision = 11, scale = 8)
    private BigDecimal geoLongitude;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "healthRecord", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Vaccination> vaccinations;

    @PrePersist
    protected void onCreate() {
        if (visitDate == null) visitDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}