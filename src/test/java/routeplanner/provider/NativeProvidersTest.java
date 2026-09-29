package routeplanner.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static routeplanner.testsupport.RouteFixtures.LYON;
import static routeplanner.testsupport.RouteFixtures.OSAKA;
import static routeplanner.testsupport.RouteFixtures.PARIS;
import static routeplanner.testsupport.RouteFixtures.TOKYO;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import routeplanner.error.InvalidLocationException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.model.Coordinate;
import routeplanner.model.RouteOption;

class NativeProvidersTest {

    @Test
    @DisplayName("Atlas returns a fast highway and a slower but more scenic road")
    void atlasReturnsTwoDistinctOptions() throws Exception {
        List<RouteOption> options = new AtlasMapProvider().findRoutes(TOKYO, OSAKA);

        assertEquals(2, options.size());
        assertTrue(options.get(0).duration().compareTo(options.get(1).duration()) < 0);
        assertTrue(options.get(0).scenicScore() < options.get(1).scenicScore());
    }

    @Test
    @DisplayName("Atlas reports RouteNotFoundException for unreachable distances")
    void atlasRejectsUnreachableDistances() {
        assertThrows(RouteNotFoundException.class,
                () -> new AtlasMapProvider().findRoutes(new Coordinate(0, 0), new Coordinate(0, 120)));
    }

    @Test
    @DisplayName("Alpine returns three options inside Europe")
    void alpineReturnsThreeOptions() throws Exception {
        List<RouteOption> options = new AlpineMapProvider().findRoutes(PARIS, LYON);

        assertEquals(3, options.size());
    }

    @Test
    @DisplayName("Alpine reports InvalidLocationException outside Europe")
    void alpineRejectsPointsOutsideEurope() {
        assertThrows(InvalidLocationException.class, () -> new AlpineMapProvider().findRoutes(TOKYO, OSAKA));
    }
}
