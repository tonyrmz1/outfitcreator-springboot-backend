package com.example.outfitcreator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Outfit entity representing a curated combination of clothing items.
 */
@Entity
@Table(name = "outfits",
        indexes = {
            @Index(name = "idx_outfits_user_id", columnList = "user_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outfit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "outfit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OutfitItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isComplete = true;

    @Column
    private Double colorCompatibilityScore;

    @Column
    private Double fitCompatibilityScore;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
