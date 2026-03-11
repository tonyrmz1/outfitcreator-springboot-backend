# jqwik Generators for OutfitCreator Domain Objects

This package contains jqwik generators for creating realistic test data for property-based testing.

## Overview

The generators produce valid domain objects with realistic attribute combinations, ensuring comprehensive test coverage across a wide range of inputs.

## Available Generators

### ColorGenerator

Generates valid color names recognized by the ColorWheel utility.

**Methods:**
- `validColors()` - Any valid color (red, blue, green, etc.)
- `nonNeutralColors()` - Colors with hue values (excludes white, black, gray, beige)
- `neutralColors()` - Neutral colors only (white, black, gray, beige)
- `optionalSecondaryColors()` - Optional secondary colors (30% null probability)

**Example:**
```java
@Property
void testColorCompatibility(@ForAll("colors") String color1, 
                           @ForAll("colors") String color2) {
    double score = colorWheel.calculateCompatibility(color1, color2);
    assertThat(score).isBetween(0.0, 100.0);
}

@Provide
Arbitrary<String> colors() {
    return ColorGenerator.validColors();
}
```

### SeasonGenerator

Generates Season enum values.

**Methods:**
- `seasons()` - Any Season value
- `optionalSeasons()` - Optional Season (20% null probability)
- `specificSeasons()` - Specific seasons only (excludes ALL_SEASON)

### FitCategoryGenerator

Generates FitCategory enum values.

**Methods:**
- `fitCategories()` - Any FitCategory value
- `optionalFitCategories()` - Optional FitCategory (20% null probability)
- `tightFit()` - TIGHT fit only
- `looseFit()` - LOOSE or OVERSIZED fit
- `regularFit()` - REGULAR fit only

### CategoryGenerator

Generates ClothingCategory and ItemPosition enum values.

**Methods:**
- `clothingCategories()` - Any ClothingCategory value
- `itemPositions()` - Any ItemPosition value
- `topCategories()` - TOP or OUTERWEAR
- `bottomCategories()` - BOTTOM only
- `footwearCategories()` - FOOTWEAR only
- `accessoryCategories()` - ACCESSORIES only

### UserGenerator

Generates User entities with valid email and password formats.

**Methods:**
- `users()` - Complete User entities
- `validEmails()` - Valid email addresses (username@domain.tld)
- `validPasswords()` - Valid passwords (8-20 characters, alphanumeric with special chars)
- `optionalFirstNames()` - Optional first names (20% null probability)
- `optionalLastNames()` - Optional last names (20% null probability)
- `usersWithEmail(String email)` - Users with specific email

**Example:**
```java
@Property
void testUserRegistration(@ForAll("users") User user) {
    assertThat(user.getEmail()).contains("@");
    assertThat(user.getPassword()).hasSizeGreaterThanOrEqualTo(8);
}

@Provide
Arbitrary<User> users() {
    return UserGenerator.users();
}
```

### ClothingItemGenerator

Generates ClothingItem entities with valid attribute combinations.

**Methods:**
- `clothingItems()` - Complete ClothingItem entities
- `clothingItemsForUser(User user)` - Items for a specific user
- `clothingItemsWithCategory(ClothingCategory category)` - Items of specific category
- `clothingItemsWithFit(FitCategory fitCategory)` - Items with specific fit
- `clothingItemsWithSeason(Season season)` - Items for specific season
- `clothingItemsWithColor(String color)` - Items with specific primary color
- `digitalCloset()` - List of 5-50 clothing items
- `balancedDigitalCloset()` - Balanced closet with items from all categories

**Features:**
- Realistic item names based on category
- Optional brands (30% null probability)
- Valid color combinations
- Optional sizes (20% null probability)
- Purchase dates within last 5 years (30% null probability)
- Optional photo paths (40% null probability)
- Wear counts between 0-100

**Example:**
```java
@Property
void testClothingItemCreation(@ForAll("clothingItems") ClothingItem item) {
    assertThat(item.getName()).isNotEmpty();
    assertThat(item.getPrimaryColor()).isNotNull();
    assertThat(item.getCategory()).isNotNull();
}

@Provide
Arbitrary<ClothingItem> clothingItems() {
    return ClothingItemGenerator.clothingItems();
}
```

### OutfitGenerator

Generates Outfit entities with valid item references.

**Methods:**
- `outfits()` - Basic Outfit entities
- `outfitsForUser(User user)` - Outfits for specific user
- `completeOutfits()` - Complete outfits with top, bottom, and optional footwear/outerwear
- `outfitsFromCloset(List<ClothingItem> closet)` - Outfits using items from specific closet
- `incompleteOutfits()` - Outfits marked as incomplete
- `outfitList()` - List of 1-20 outfits

**Features:**
- Realistic outfit names (e.g., "Casual Friday", "Business Meeting")
- Optional notes (50% null probability)
- Complete outfits include at least top and bottom
- Proper ItemPosition assignments
- Valid outfit-item relationships

**Example:**
```java
@Property
void testOutfitCreation(@ForAll("completeOutfits") Outfit outfit) {
    assertThat(outfit.getItems()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(outfit.getIsComplete()).isTrue();
}

@Provide
Arbitrary<Outfit> completeOutfits() {
    return OutfitGenerator.completeOutfits();
}
```

## Usage in Property-Based Tests

### Basic Usage

```java
@PropertyDefaults(tries = 100)
public class MyPropertyTest {
    
    @Property
    void myProperty(@ForAll("clothingItems") ClothingItem item) {
        // Test logic here
    }
    
    @Provide
    Arbitrary<ClothingItem> clothingItems() {
        return ClothingItemGenerator.clothingItems();
    }
}
```

### Combining Generators

```java
@Property
void testRecommendationEngine(
        @ForAll("digitalClosets") List<ClothingItem> closet,
        @ForAll("seasons") Season season) {
    
    List<OutfitRecommendation> recommendations = 
        recommendationEngine.generate(closet, season);
    
    assertThat(recommendations).isNotEmpty();
}

@Provide
Arbitrary<List<ClothingItem>> digitalClosets() {
    return ClothingItemGenerator.balancedDigitalCloset();
}

@Provide
Arbitrary<Season> seasons() {
    return SeasonGenerator.seasons();
}
```

### Custom Generators

You can create custom generators by combining existing ones:

```java
@Provide
Arbitrary<ClothingItem> tightTops() {
    return ClothingItemGenerator.clothingItemsWithCategory(ClothingCategory.TOP)
        .map(item -> {
            item.setFitCategory(FitCategory.TIGHT);
            return item;
        });
}

@Provide
Arbitrary<Outfit> summerOutfits() {
    return OutfitGenerator.completeOutfits()
        .map(outfit -> {
            outfit.getItems().forEach(outfitItem -> 
                outfitItem.getClothingItem().setSeason(Season.SUMMER)
            );
            return outfit;
        });
}
```

## Configuration

All generators are configured to produce realistic test data:

- **Null probabilities**: Carefully tuned to match real-world data patterns
- **Value ranges**: Constrained to valid business logic ranges
- **Combinations**: Ensure valid attribute combinations (e.g., category-appropriate names)

## Testing the Generators

Run `GeneratorsTest` to verify all generators produce valid data:

```bash
mvn test -Dtest=GeneratorsTest
```

This test suite validates:
- All required fields are non-null
- All values are within valid ranges
- All enum values are valid
- All relationships are properly established
- All collections have appropriate sizes

## Best Practices

1. **Use specific generators** when testing specific scenarios (e.g., `tightFit()` for fit compatibility tests)
2. **Use balanced closets** when testing recommendation algorithms to ensure diverse item selection
3. **Configure try counts** appropriately - use `@Property(tries = 100)` for most tests
4. **Combine generators** to create complex test scenarios
5. **Document custom generators** in your test classes for clarity

## Integration with Property-Based Tests

These generators are designed to work seamlessly with the property-based testing framework defined in the design document. Each generator produces data that satisfies the constraints defined in the requirements and design specifications.

For more information on property-based testing strategy, see the design document section on "Testing Strategy".
