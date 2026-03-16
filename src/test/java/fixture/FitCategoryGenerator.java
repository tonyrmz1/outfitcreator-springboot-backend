package fixture;

import com.example.outfitcreator.core.enums.FitCategory;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class FitCategoryGenerator {

    public static Arbitrary<FitCategory> fitCategories() {
        return Arbitraries.of(FitCategory.class);
    }

    public static Arbitrary<FitCategory> optionalFitCategories() {
        return Arbitraries.of(FitCategory.class).injectNull(0.2);
    }

    public static Arbitrary<FitCategory> tightFit() {
        return Arbitraries.just(FitCategory.TIGHT);
    }

    public static Arbitrary<FitCategory> looseFit() {
        return Arbitraries.of(FitCategory.LOOSE, FitCategory.OVERSIZED);
    }

    public static Arbitrary<FitCategory> regularFit() {
        return Arbitraries.just(FitCategory.REGULAR);
    }
}
