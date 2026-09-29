package routeplanner.bridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static routeplanner.testsupport.RouteFixtures.DESTINATION;
import static routeplanner.testsupport.RouteFixtures.ORIGIN;
import static routeplanner.testsupport.RouteFixtures.option;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import routeplanner.model.Route;
import routeplanner.model.RouteOption;
import routeplanner.testsupport.StubMapProvider;

class RefinedPlannersDelegationTest {

    @Test
    @DisplayName("FastestRoutePlanner delegates to the provider and picks the minimum duration")
    void fastestPlannerDelegatesAndPicksMinimumDuration() throws Exception {
        RouteOption slow = option(900, 600, 90);
        RouteOption quick = option(1100, 420, 20);
        StubMapProvider provider = StubMapProvider.returning("stub", slow, quick);

        Route route = new FastestRoutePlanner(provider).plan(ORIGIN, DESTINATION);

        assertEquals(1, provider.callCount());
        assertEquals(ORIGIN, provider.lastOrigin());
        assertEquals(DESTINATION, provider.lastDestination());
        assertSame(quick, route.option());
        assertEquals("fastest", route.strategy());
        assertEquals("stub", route.providerName());
    }

    @Test
    @DisplayName("ShortestRoutePlanner delegates to the provider and picks the minimum distance")
    void shortestPlannerDelegatesAndPicksMinimumDistance() throws Exception {
        RouteOption longer = option(1100, 420, 20);
        RouteOption shorter = option(900, 600, 90);
        StubMapProvider provider = StubMapProvider.returning("stub", longer, shorter);

        Route route = new ShortestRoutePlanner(provider).plan(ORIGIN, DESTINATION);

        assertEquals(1, provider.callCount());
        assertSame(shorter, route.option());
        assertEquals("shortest", route.strategy());
    }

    @Test
    @DisplayName("ScenicRoutePlanner picks the most scenic option within the allowed detour")
    void scenicPlannerPicksMostScenicWithinDetour() throws Exception {
        RouteOption quick = option(1000, 400, 20);
        RouteOption scenic = option(1200, 560, 80);
        RouteOption tooSlow = option(1500, 900, 95);
        StubMapProvider provider = StubMapProvider.returning("stub", quick, scenic, tooSlow);

        Route route = new ScenicRoutePlanner(provider).plan(ORIGIN, DESTINATION);

        assertEquals(1, provider.callCount());
        assertSame(scenic, route.option());
        assertEquals("scenic", route.strategy());
    }

    @Test
    @DisplayName("ScenicRoutePlanner with detour factor 1.0 degrades to the fastest option")
    void scenicPlannerWithNoDetourPicksFastest() throws Exception {
        RouteOption quick = option(1000, 400, 20);
        RouteOption scenic = option(1200, 560, 80);
        StubMapProvider provider = StubMapProvider.returning("stub", quick, scenic);

        Route route = new ScenicRoutePlanner(provider, 1.0).plan(ORIGIN, DESTINATION);

        assertSame(quick, route.option());
    }

    @Test
    @DisplayName("ScenicRoutePlanner rejects a detour factor below 1.0")
    void scenicPlannerRejectsInvalidDetourFactor() {
        StubMapProvider provider = StubMapProvider.returning("stub", option(1000, 400, 20));

        assertThrows(IllegalArgumentException.class, () -> new ScenicRoutePlanner(provider, 0.9));
    }
}
