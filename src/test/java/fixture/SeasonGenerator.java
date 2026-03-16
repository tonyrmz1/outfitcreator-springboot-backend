package fixture;

import com.example.outfitcreator.core.enums.Season;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class SeasonGenerator {

    public static Arbitrary<Season> seasons() {
        return Arbitraries.of(Season.class);
    }

    public static Arbitrary<Season> optionalSeasons() {
        return Arbitraries.of(Season.class).injectNull(0.2);
    }

    public static Arbitrary<Season> specificSeasons() {
        return Arbitraries.of(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER);
    }
}
