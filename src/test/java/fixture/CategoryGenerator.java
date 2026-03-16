package fixture;

import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.ItemPosition;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class CategoryGenerator {

    public static Arbitrary<ClothingCategory> clothingCategories() {
        return Arbitraries.of(ClothingCategory.class);
    }

    public static Arbitrary<ItemPosition> itemPositions() {
        return Arbitraries.of(ItemPosition.class);
    }

    public static Arbitrary<ClothingCategory> topCategories() {
        return Arbitraries.of(ClothingCategory.TOP, ClothingCategory.OUTERWEAR);
    }

    public static Arbitrary<ClothingCategory> bottomCategories() {
        return Arbitraries.just(ClothingCategory.BOTTOM);
    }

    public static Arbitrary<ClothingCategory> footwearCategories() {
        return Arbitraries.just(ClothingCategory.FOOTWEAR);
    }

    public static Arbitrary<ClothingCategory> accessoryCategories() {
        return Arbitraries.just(ClothingCategory.ACCESSORIES);
    }
}
