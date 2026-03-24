package com.example.outfitcreator.feature.outfit.repository;

import com.example.outfitcreator.core.entity.Outfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Persistence access for {@link Outfit} entities, including queries by owner and by contained clothing item.
 */
public interface FeatureOutfitRepository extends JpaRepository<Outfit, Long> {

    List<Outfit> findByUserId(Long userId);

    Page<Outfit> findByUserId(Long userId, Pageable pageable);

    /**
     * Outfits that reference the given closet item (e.g. for cascade or score updates).
     */
    List<Outfit> findByItemsClothingItemId(Long clothingItemId);
}
