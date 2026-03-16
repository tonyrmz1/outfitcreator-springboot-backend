package com.example.outfitcreator.feature.outfit.repository;

import com.example.outfitcreator.core.entity.OutfitItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutfitItemRepository extends JpaRepository<OutfitItem, Long> {
}
