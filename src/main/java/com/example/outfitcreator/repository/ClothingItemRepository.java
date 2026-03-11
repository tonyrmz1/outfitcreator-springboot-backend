package com.example.outfitcreator.repository;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for clothing items.
 */
public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long>, JpaSpecificationExecutor<ClothingItem> {

    List<ClothingItem> findByUserId(Long userId);

    Page<ClothingItem> findByUserId(Long userId, Pageable pageable);

    Optional<ClothingItem> findByIdAndUserId(Long id, Long userId);

    List<ClothingItem> findByUserIdAndCategory(Long userId, ClothingCategory category);

    List<ClothingItem> findByUserIdAndSeason(Long userId, Season season);

    List<ClothingItem> findByUserIdAndPrimaryColor(Long userId, String primaryColor);

    @Query("SELECT ci FROM ClothingItem ci WHERE ci.user.id = :userId AND ci.category = :category AND ci.season = :season")
    List<ClothingItem> findByUserIdCategoryAndSeason(@Param("userId") Long userId,
                                                     @Param("category") ClothingCategory category,
                                                     @Param("season") Season season);

    @Query("SELECT COUNT(oi) > 0 FROM OutfitItem oi WHERE oi.clothingItem.id = :itemId")
    boolean existsInOutfits(@Param("itemId") Long itemId);
}
