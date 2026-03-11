package com.example.outfitcreator.generators;

import com.example.outfitcreator.entity.ClothingItem;
import com.example.outfitcreator.entity.Outfit;
import com.example.outfitcreator.entity.OutfitItem;
import com.example.outfitcreator.entity.User;
import com.example.outfitcreator.enums.ClothingCategory;
import com.example.outfitcreator.enums.ItemPosition;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * jqwik generator for Outfit entities with valid item references.
 * Ensures outfits contain valid combinations of clothing items with proper positions.
 */
public class OutfitGenerator {
    
    /**
     * Generate valid Outfit entities with clothing items.
     */
    public static Arbitrary<Outfit> outfits() {
        return Combinators.combine(
            outfitNames(),
            optionalNotes(),
            Arbitraries.defaultFor(Boolean.class)
        ).as((name, notes, isComplete) -> 
            Outfit.builder()
                .name(name)
                .notes(notes)
                .isComplete(isComplete)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
    }
    
    /**
     * Generate Outfit entities with a specific user.
     */
    public static Arbitrary<Outfit> outfitsForUser(User user) {
        return outfits().map(outfit -> {
            outfit.setUser(user);
            return outfit;
        });
    }
    
    /**
     * Generate complete outfits with valid clothing items and positions.
     * Includes at least a top and bottom, optionally footwear and outerwear.
     */
    public static Arbitrary<Outfit> completeOutfits() {
        return Combinators.combine(
            outfitNames(),
            optionalNotes(),
            ClothingItemGenerator.clothingItemsWithCategory(ClothingCategory.TOP),
            ClothingItemGenerator.clothingItemsWithCategory(ClothingCategory.BOTTOM),
            ClothingItemGenerator.clothingItemsWithCategory(ClothingCategory.FOOTWEAR).injectNull(0.3),
            ClothingItemGenerator.clothingItemsWithCategory(ClothingCategory.OUTERWEAR).injectNull(0.5)
        ).as((name, notes, top, bottom, footwear, outerwear) -> {
            Outfit outfit = Outfit.builder()
                .name(name)
                .notes(notes)
                .isComplete(true)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            // Add top
            OutfitItem topItem = OutfitItem.builder()
                .outfit(outfit)
                .clothingItem(top)
                .position(ItemPosition.TOP)
                .build();
            outfit.getItems().add(topItem);
            
            // Add bottom
            OutfitItem bottomItem = OutfitItem.builder()
                .outfit(outfit)
                .clothingItem(bottom)
                .position(ItemPosition.BOTTOM)
                .build();
            outfit.getItems().add(bottomItem);
            
            // Add footwear if present
            if (footwear != null) {
                OutfitItem footwearItem = OutfitItem.builder()
                    .outfit(outfit)
                    .clothingItem(footwear)
                    .position(ItemPosition.FOOTWEAR)
                    .build();
                outfit.getItems().add(footwearItem);
            }
            
            // Add outerwear if present
            if (outerwear != null) {
                OutfitItem outerwearItem = OutfitItem.builder()
                    .outfit(outfit)
                    .clothingItem(outerwear)
                    .position(ItemPosition.OUTERWEAR)
                    .build();
                outfit.getItems().add(outerwearItem);
            }
            
            return outfit;
        });
    }
    
    /**
     * Generate outfits with items from a specific digital closet.
     * Ensures all outfit items reference valid items from the provided closet.
     */
    public static Arbitrary<Outfit> outfitsFromCloset(List<ClothingItem> closet) {
        // Filter closet by category
        List<ClothingItem> tops = closet.stream()
            .filter(item -> item.getCategory() == ClothingCategory.TOP)
            .toList();
        List<ClothingItem> bottoms = closet.stream()
            .filter(item -> item.getCategory() == ClothingCategory.BOTTOM)
            .toList();
        List<ClothingItem> footwear = closet.stream()
            .filter(item -> item.getCategory() == ClothingCategory.FOOTWEAR)
            .toList();
        List<ClothingItem> outerwear = closet.stream()
            .filter(item -> item.getCategory() == ClothingCategory.OUTERWEAR)
            .toList();
        
        // Need at least a top and bottom
        if (tops.isEmpty() || bottoms.isEmpty()) {
            return outfits(); // Return empty outfit if closet doesn't have required items
        }
        
        return Combinators.combine(
            outfitNames(),
            optionalNotes(),
            Arbitraries.of(tops),
            Arbitraries.of(bottoms),
            footwear.isEmpty() ? Arbitraries.just((ClothingItem) null) : 
                Arbitraries.of(footwear).injectNull(0.3),
            outerwear.isEmpty() ? Arbitraries.just((ClothingItem) null) : 
                Arbitraries.of(outerwear).injectNull(0.5)
        ).as((name, notes, top, bottom, shoe, jacket) -> {
            Outfit outfit = Outfit.builder()
                .name(name)
                .notes(notes)
                .isComplete(true)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            
            // Add items
            outfit.getItems().add(OutfitItem.builder()
                .outfit(outfit)
                .clothingItem(top)
                .position(ItemPosition.TOP)
                .build());
            
            outfit.getItems().add(OutfitItem.builder()
                .outfit(outfit)
                .clothingItem(bottom)
                .position(ItemPosition.BOTTOM)
                .build());
            
            if (shoe != null) {
                outfit.getItems().add(OutfitItem.builder()
                    .outfit(outfit)
                    .clothingItem(shoe)
                    .position(ItemPosition.FOOTWEAR)
                    .build());
            }
            
            if (jacket != null) {
                outfit.getItems().add(OutfitItem.builder()
                    .outfit(outfit)
                    .clothingItem(jacket)
                    .position(ItemPosition.OUTERWEAR)
                    .build());
            }
            
            return outfit;
        });
    }
    
    /**
     * Generate incomplete outfits (isComplete = false).
     */
    public static Arbitrary<Outfit> incompleteOutfits() {
        return outfits().map(outfit -> {
            outfit.setIsComplete(false);
            return outfit;
        });
    }
    
    /**
     * Generate realistic outfit names.
     */
    private static Arbitrary<String> outfitNames() {
        return Arbitraries.of(
            "Casual Friday", "Business Meeting", "Date Night", "Weekend Brunch",
            "Gym Workout", "Beach Day", "Formal Event", "Office Casual",
            "Summer Picnic", "Winter Layers", "Spring Fresh", "Autumn Vibes",
            "Night Out", "Coffee Run", "Travel Comfort", "Smart Casual",
            "Sporty Look", "Elegant Evening", "Relaxed Weekend", "Professional"
        );
    }
    
    /**
     * Generate optional outfit notes (can be null).
     */
    private static Arbitrary<String> optionalNotes() {
        return Arbitraries.of(
            "Perfect for warm weather",
            "Great color combination",
            "Comfortable and stylish",
            "Professional yet approachable",
            "My favorite outfit",
            "Needs accessories",
            "Good for outdoor events",
            "Classic and timeless"
        ).injectNull(0.5);
    }
    
    /**
     * Generate a list of outfits.
     */
    public static Arbitrary<List<Outfit>> outfitList() {
        return outfits().list().ofMinSize(1).ofMaxSize(20);
    }
}
