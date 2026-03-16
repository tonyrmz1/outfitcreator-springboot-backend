package fixture;

import com.example.outfitcreator.core.entity.ClothingItem;
import com.example.outfitcreator.core.entity.Outfit;
import com.example.outfitcreator.core.entity.User;
import com.example.outfitcreator.core.enums.ClothingCategory;
import com.example.outfitcreator.core.enums.FitCategory;
import com.example.outfitcreator.core.enums.Season;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratorsTest {

    @Property
    void userGeneratorProducesValidUsers(@ForAll("users") User user) {
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isNotNull().contains("@");
        assertThat(user.getPassword()).isNotNull().hasSizeGreaterThanOrEqualTo(8);
    }

    @Property
    void clothingItemGeneratorProducesValidItems(@ForAll("clothingItems") ClothingItem item) {
        assertThat(item).isNotNull();
        assertThat(item.getName()).isNotNull().isNotEmpty();
        assertThat(item.getPrimaryColor()).isNotNull().isNotEmpty();
        assertThat(item.getCategory()).isNotNull();
        assertThat(item.getWearCount()).isNotNull().isGreaterThanOrEqualTo(0);
    }

    @Property
    void clothingItemWithCategoryProducesCorrectCategory(@ForAll("topsOnly") ClothingItem item) {
        assertThat(item.getCategory()).isEqualTo(ClothingCategory.TOP);
    }

    @Property
    void outfitGeneratorProducesValidOutfits(@ForAll("outfits") Outfit outfit) {
        assertThat(outfit).isNotNull();
        assertThat(outfit.getName()).isNotNull().isNotEmpty();
        assertThat(outfit.getIsComplete()).isNotNull();
    }

    @Property
    void completeOutfitHasRequiredItems(@ForAll("completeOutfits") Outfit outfit) {
        assertThat(outfit).isNotNull();
        assertThat(outfit.getItems()).isNotEmpty();
        assertThat(outfit.getItems()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Property
    void colorGeneratorProducesValidColors(@ForAll("colors") String color) {
        assertThat(color).isNotNull().isNotEmpty();
        assertThat(ColorGenerator.validColors().sample()).isIn(
                "red", "orange", "yellow", "lime", "green", "cyan",
                "blue", "purple", "magenta", "pink", "brown",
                "white", "black", "gray", "beige"
        );
    }

    @Property
    void seasonGeneratorProducesValidSeasons(@ForAll("seasons") Season season) {
        assertThat(season).isNotNull();
        assertThat(season).isIn(Season.values());
    }

    @Property
    void fitCategoryGeneratorProducesValidFitCategories(@ForAll("fitCategories") FitCategory fitCategory) {
        assertThat(fitCategory).isNotNull();
        assertThat(fitCategory).isIn(FitCategory.values());
    }

    @Property
    void digitalClosetHasMultipleItems(@ForAll("digitalClosets") List<ClothingItem> closet) {
        assertThat(closet).isNotNull();
        assertThat(closet).hasSizeGreaterThanOrEqualTo(5);
        assertThat(closet).hasSizeLessThanOrEqualTo(50);
    }

    @Property
    void balancedClosetHasAllCategories(@ForAll("balancedClosets") List<ClothingItem> closet) {
        assertThat(closet).isNotNull();

        boolean hasTops = closet.stream().anyMatch(item -> item.getCategory() == ClothingCategory.TOP);
        boolean hasBottoms = closet.stream().anyMatch(item -> item.getCategory() == ClothingCategory.BOTTOM);
        boolean hasFootwear = closet.stream().anyMatch(item -> item.getCategory() == ClothingCategory.FOOTWEAR);

        assertThat(hasTops).isTrue();
        assertThat(hasBottoms).isTrue();
        assertThat(hasFootwear).isTrue();
    }

    @Provide
    net.jqwik.api.Arbitrary<User> users() {
        return UserGenerator.users();
    }

    @Provide
    net.jqwik.api.Arbitrary<ClothingItem> clothingItems() {
        return ClothingItemGenerator.clothingItems();
    }

    @Provide
    net.jqwik.api.Arbitrary<ClothingItem> topsOnly() {
        return ClothingItemGenerator.clothingItemsWithCategory(ClothingCategory.TOP);
    }

    @Provide
    net.jqwik.api.Arbitrary<Outfit> outfits() {
        return OutfitGenerator.outfits();
    }

    @Provide
    net.jqwik.api.Arbitrary<Outfit> completeOutfits() {
        return OutfitGenerator.completeOutfits();
    }

    @Provide
    net.jqwik.api.Arbitrary<String> colors() {
        return ColorGenerator.validColors();
    }

    @Provide
    net.jqwik.api.Arbitrary<Season> seasons() {
        return SeasonGenerator.seasons();
    }

    @Provide
    net.jqwik.api.Arbitrary<FitCategory> fitCategories() {
        return FitCategoryGenerator.fitCategories();
    }

    @Provide
    net.jqwik.api.Arbitrary<List<ClothingItem>> digitalClosets() {
        return ClothingItemGenerator.digitalCloset();
    }

    @Provide
    net.jqwik.api.Arbitrary<List<ClothingItem>> balancedClosets() {
        return ClothingItemGenerator.balancedDigitalCloset();
    }
}
