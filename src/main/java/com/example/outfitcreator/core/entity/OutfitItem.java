package com.example.outfitcreator.core.entity;

import com.example.outfitcreator.core.enums.ItemPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outfit item entity representing a clothing item in an outfit with its position.
 */
@Entity
@Table(name = "outfit_items",
        indexes = {
            @Index(name = "idx_outfit_items_outfit_id", columnList = "outfit_id"),
            @Index(name = "idx_outfit_items_clothing_item_id", columnList = "clothing_item_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutfitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    private Outfit outfit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothing_item_id")
    private ClothingItem clothingItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemPosition position;
}
