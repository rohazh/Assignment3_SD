package routeplanner.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static routeplanner.testsupport.RouteFixtures.DESTINATION;
import static routeplanner.testsupport.RouteFixtures.ORIGIN;
import static routeplanner.testsupport.RouteFixtures.option;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import routeplanner.error.InvalidLocationException;
import routeplanner.error.ProviderUnavailableException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.model.Route;
import routeplanner.model.RouteOption;
import routeplanner.testsupport.StubMapProvider;

class RoutePlannerContractTest {

    private static final class LongestRoutePlanner extends RoutePlanner {

        LongestRoutePlanner(MapProvider provider) {
            super(provider);
        }

        @Override
        public String strategyName() {
            return "longest";
        }

        @Override
        protected RouteOption select(List<RouteOption> options) {
            return Collections.max(options, Comparator.comparingDouble(RouteOption::distanceKm));
        }
    }

    @Test
    @DisplayName("Identical origin and destination are rejected before the provider is called")
    void identicalPointsAreRejectedWithoutCallingProvider() {
        StubMapProvider provider = StubMapProvider.returning("stub", option(1000, 400, 20));
        RoutePlanner planner = new FastestRoutePlanner(provider);

        assertThrows(InvalidLocationException.class, () -> planner.plan(ORIGIN, ORIGIN));
        assertEquals(0, provider.callCount());
    }

    @Test
    @DisplayName("A provider returning no options results in RouteNotFoundException")
    void emptyOptionListMeansRouteNotFound() {
        StubMapProvider provider = StubMapProvider.returning("stub");

        assertThrows(RouteNotFoundException.class,
                () -> new FastestRoutePlanner(provider).plan(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("Provider failures propagate through the abstraction unchanged")
    void providerFailuresPropagateUnchanged() {
        ProviderUnavailableException failure = new ProviderUnavailableException("down");
        StubMapProvider provider = StubMapProvider.failing("stub", failure);

        ProviderUnavailableException thrown = assertThrows(ProviderUnavailableException.class,
                () -> new ScenicRoutePlanner(provider).plan(ORIGIN, DESTINATION));

        assertSame(failure, thrown);
    }

    @Test
    @DisplayName("A new refined abstraction works with any provider without changing existing classes")
    void newRefinedAbstractionNeedsNoChangesToExistingClasses() throws Exception {
        RouteOption shorter = option(900, 600, 90);
        RouteOption longer = option(1100, 420, 20);
        StubMapProvider provider = StubMapProvider.returning("stub", shorter, longer);

        Route route = new LongestRoutePlanner(provider).plan(ORIGIN, DESTINATION);

        assertSame(longer, route.option());
        assertEquals("longest", route.strategy());
    }
}
