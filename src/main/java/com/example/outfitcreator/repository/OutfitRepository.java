package com.example.outfitcreator.repository;

import com.example.outfitcreator.entity.Outfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for outfits.
 */
public interface OutfitRepository extends JpaRepository<Outfit, Long> {

    List<Outfit> findByUserId(Long userId);
    
    Page<Outfit> findByUserId(Long userId, Pageable pageable);
}
