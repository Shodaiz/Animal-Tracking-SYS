package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "constats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Constat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = false)
    private User controller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "constat_type", length = 50)
    private String constatType = "General";
    // General, Missing_animal, Fraud, Health_issue

    @Column(length = 20)
    private String severity = "Normal";
    // Normal, Warning, Critical

    @Column(length = 20)
    private String status = "Pending";
    // Pending, Reviewed, Resolved, Rejected

    @Column(name = "scanned_count")
    private Integer scannedCount = 0;

    @Column(name = "registered_count")
    private Integer registeredCount = 0;

    @Column()
    private Integer difference = 0;

    @Column(name = "missing_tags", columnDefinition = "TEXT")
    private String missingTags;

    @Column(name = "unknown_tags", columnDefinition = "TEXT")
    private String unknownTags;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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