package com.example.outfitcreator.feature.closet.repository;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.Season;
import com.example.outfitcreator.feature.closet.dto.response.ClothingItemFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds JPA {@link Specification} instances for dynamic closet filtering (category, season, color, text search).
 */
public class ClothingItemSpecification {

    private ClothingItemSpecification() {}

    /**
     * Restricts results to the given user and applies optional filter criteria from {@link ClothingItemFilter}.
     *
     * @param userId owner user id (required)
     * @param filter optional filter; {@code null} means no extra predicates beyond user scope
     */
    public static Specification<ClothingItem> withFilters(Long userId, ClothingItemFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (filter != null) {
                ClothingCategory category = filter.getCategory();
                if (category != null) {
                    predicates.add(cb.equal(root.get("category"), category));
                }

                Season season = filter.getSeason();
                if (season != null) {
                    predicates.add(cb.equal(root.get("season"), season));
                }

                String color = filter.getColor();
                if (color != null && !color.isBlank()) {
                    predicates.add(cb.like(cb.lower(root.get("primaryColor")), "%" + color.toLowerCase() + "%"));
                }

                String searchQuery = filter.getSearchQuery();
                if (searchQuery != null && !searchQuery.isBlank()) {
                    String pattern = "%" + searchQuery.toLowerCase() + "%";
                    Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                    Predicate brandMatch = cb.like(cb.lower(root.get("brand")), pattern);
                    predicates.add(cb.or(nameMatch, brandMatch));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
