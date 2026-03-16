package com.example.outfitcreator.core.entity;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.Season;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clothing item entity representing a single piece of clothing in the user's digital closet.
 */
@Entity
@Table(name = "clothing_items",
        indexes = {
            @Index(name = "idx_clothing_items_user_id", columnList = "user_id"),
            @Index(name = "idx_clothing_items_category", columnList = "category"),
            @Index(name = "idx_clothing_items_season", columnList = "season"),
            @Index(name = "idx_clothing_items_primary_color", columnList = "primary_color")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String brand;

    @Column(nullable = false)
    private String primaryColor;

    private String secondaryColor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClothingCategory category;

    private String size;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Enumerated(EnumType.STRING)
    private FitCategory fitCategory;

    private LocalDate purchaseDate;

    private String photoPath;

    @Column(nullable = false)
    @Builder.Default
    private Integer wearCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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
