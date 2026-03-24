package com.example.outfitcreator.feature.closet.repository;

import com.example.outfitcreator.core.entity.ClothingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Persistence access for {@link ClothingItem}, including user-scoped queries and outfit membership checks.
 */
public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long>, JpaSpecificationExecutor<ClothingItem> {

    List<ClothingItem> findByUserId(Long userId);

    Page<ClothingItem> findByUserId(Long userId, Pageable pageable);

    Optional<ClothingItem> findByIdAndUserId(Long id, Long userId);

    List<ClothingItem> findByUserIdAndCategory(Long userId, com.example.outfitcreator.core.enums.ClothingCategory category);

    List<ClothingItem> findByUserIdAndSeason(Long userId, com.example.outfitcreator.core.enums.Season season);

    List<ClothingItem> findByUserIdAndPrimaryColor(Long userId, String primaryColor);

    @Query("SELECT ci FROM ClothingItem ci WHERE ci.user.id = :userId AND ci.category = :category AND ci.season = :season")
    List<ClothingItem> findByUserIdCategoryAndSeason(@Param("userId") Long userId,
                                                      @Param("category") com.example.outfitcreator.core.enums.ClothingCategory category,
                                                      @Param("season") com.example.outfitcreator.core.enums.Season season);

    /**
     * @param itemId clothing item primary key
     * @return {@code true} if the item appears in at least one {@link com.example.outfitcreator.core.entity.OutfitItem}
     */
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OutfitItem oi WHERE oi.clothingItem.id = :itemId")
    boolean existsInOutfits(@Param("itemId") Long itemId);
}
