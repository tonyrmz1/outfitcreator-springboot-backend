package com.example.outfitcreator.feature.outfit.repository;

import com.example.outfitcreator.core.entity.Outfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeatureOutfitRepository extends JpaRepository<Outfit, Long> {

    List<Outfit> findByUserId(Long userId);
    
    Page<Outfit> findByUserId(Long userId, Pageable pageable);
}
