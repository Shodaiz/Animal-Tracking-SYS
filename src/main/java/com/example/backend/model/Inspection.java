package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    private User inspector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @Column(name = "inspection_date")
    private LocalDateTime inspectionDate;

    @Column(name = "constat_type", length = 50)
    private String constatType = "General";

    // Compliant, Fraud, Suspicious, Pending
    @Column(length = 20)
    private String result = "Pending";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "scanned_count")
    private Integer scannedCount = 0;

    @Column(name = "registered_count")
    private Integer registeredCount = 0;

    @Column(insertable = false, updatable = false)
    private Integer difference;

    @Column(name = "geo_latitude", precision = 10, scale = 8)
    private BigDecimal geoLatitude;

    @Column(name = "geo_longitude", precision = 11, scale = 8)
    private BigDecimal geoLongitude;

    // Pending, UnderReview, Resolved, Rejected
    @Column(length = 20)
    private String status = "Pending";

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (inspectionDate == null) inspectionDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}