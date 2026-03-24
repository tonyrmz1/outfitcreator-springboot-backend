package com.example.outfitcreator.feature.outfit.repository;

import com.example.outfitcreator.core.entity.OutfitItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for {@link OutfitItem} join rows between outfits and clothing items.
 */
public interface OutfitItemRepository extends JpaRepository<OutfitItem, Long> {
}
