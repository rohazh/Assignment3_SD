package routeplanner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static routeplanner.testsupport.RouteFixtures.DESTINATION;
import static routeplanner.testsupport.RouteFixtures.ORIGIN;
import static routeplanner.testsupport.RouteFixtures.option;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import routeplanner.bridge.FastestRoutePlanner;
import routeplanner.bridge.MapProvider;
import routeplanner.bridge.RoutePlanner;
import routeplanner.bridge.ScenicRoutePlanner;
import routeplanner.error.ProviderUnavailableException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.model.Region;
import routeplanner.model.Route;
import routeplanner.model.RouteOption;
import routeplanner.model.RouteRequest;
import routeplanner.testsupport.StubMapProvider;

class RoutingServiceTest {

    private static final Region NARROW = new Region("Narrow", 0, 90, 0, 180);

    private static Map<String, Function<MapProvider, RoutePlanner>> defaultPlanners() {
        Map<String, Function<MapProvider, RoutePlanner>> planners = new HashMap<>();
        planners.put("fastest", FastestRoutePlanner::new);
        planners.put("scenic", ScenicRoutePlanner::new);
        return planners;
    }

    private static RoutingService service(MapProvider... providers) {
        return new RoutingService(new ProviderSelector(List.of(providers)), defaultPlanners());
    }

    @Test
    @DisplayName("The refined abstraction is chosen from the request style")
    void styleInTheRequestSelectsTheRefinedAbstraction() throws Exception {
        StubMapProvider provider = StubMapProvider.returning("stub", option(1000, 400, 20), option(1200, 560, 80));
        RoutingService service = service(provider);

        Route fastest = service.route(new RouteRequest(ORIGIN, DESTINATION, "fastest"));
        Route scenic = service.route(new RouteRequest(ORIGIN, DESTINATION, "SCENIC"));

        assertEquals("fastest", fastest.strategy());
        assertEquals(20, fastest.option().scenicScore());
        assertEquals("scenic", scenic.strategy());
        assertEquals(80, scenic.option().scenicScore());
    }

    @Test
    @DisplayName("The most specific provider is used first")
    void mostSpecificProviderIsUsedFirst() throws Exception {
        StubMapProvider broad = StubMapProvider.returning("broad", option(1000, 400, 20));
        StubMapProvider narrow = StubMapProvider.returning("narrow", option(1000, 400, 20)).covering(NARROW);

        Route route = service(broad, narrow).route(new RouteRequest(ORIGIN, DESTINATION, "fastest"));

        assertEquals("narrow", route.providerName());
        assertEquals(0, broad.callCount());
    }

    @Test
    @DisplayName("An unavailable provider is skipped and the next candidate is used")
    void unavailableProviderFallsBackToNextCandidate() throws Exception {
        StubMapProvider down = StubMapProvider.failing("down", new ProviderUnavailableException("offline"))
                .covering(NARROW);
        StubMapProvider backup = StubMapProvider.returning("backup", option(1000, 400, 20));

        Route route = service(backup, down).route(new RouteRequest(ORIGIN, DESTINATION, "fastest"));

        assertEquals("backup", route.providerName());
        assertEquals(1, down.callCount());
        assertEquals(1, backup.callCount());
    }

    @Test
    @DisplayName("RouteNotFoundException is final and does not trigger a fallback")
    void routeNotFoundDoesNotFallBack() {
        RouteNotFoundException failure = new RouteNotFoundException("no road");
        StubMapProvider narrow = StubMapProvider.failing("narrow", failure).covering(NARROW);
        StubMapProvider broad = StubMapProvider.returning("broad", option(1000, 400, 20));

        RouteNotFoundException thrown = assertThrows(RouteNotFoundException.class,
                () -> service(broad, narrow).route(new RouteRequest(ORIGIN, DESTINATION, "fastest")));

        assertSame(failure, thrown);
        assertEquals(0, broad.callCount());
    }

    @Test
    @DisplayName("When every provider is unavailable the failure names all of them")
    void allProvidersUnavailable() {
        StubMapProvider first = StubMapProvider.failing("first", new ProviderUnavailableException("offline"))
                .covering(NARROW);
        StubMapProvider second = StubMapProvider.failing("second", new ProviderUnavailableException("busy"));

        ProviderUnavailableException thrown = assertThrows(ProviderUnavailableException.class,
                () -> service(first, second).route(new RouteRequest(ORIGIN, DESTINATION, "fastest")));

        assertTrue(thrown.getMessage().contains("first"));
        assertTrue(thrown.getMessage().contains("second"));
    }

    @Test
    @DisplayName("An unknown style is rejected")
    void unknownStyleIsRejected() {
        StubMapProvider provider = StubMapProvider.returning("stub", option(1000, 400, 20));

        assertThrows(IllegalArgumentException.class,
                () -> service(provider).route(new RouteRequest(ORIGIN, DESTINATION, "balanced")));
    }

    @Test
    @DisplayName("A new abstraction variant and a new implementor plug in without touching existing classes")
    void openForExtensionOnBothAxes() throws Exception {
        class LongestRoutePlanner extends RoutePlanner {
            LongestRoutePlanner(MapProvider provider) {
                super(provider);
            }

            @Override
            public String strategyName() {
                return "longest";
            }

            @Override
            protected RouteOption select(List<RouteOption> options) {
                return options.stream().max(Comparator.comparingDouble(RouteOption::distanceKm)).orElseThrow();
            }
        }
        StubMapProvider newImplementor = StubMapProvider.returning("brand-new", option(900, 600, 90), option(1100, 420, 20))
                .covering(NARROW);
        Map<String, Function<MapProvider, RoutePlanner>> planners = defaultPlanners();
        planners.put("longest", LongestRoutePlanner::new);
        RoutingService service = new RoutingService(new ProviderSelector(List.of(newImplementor)), planners);

        Route route = service.route(new RouteRequest(ORIGIN, DESTINATION, "longest"));

        assertEquals("longest", route.strategy());
        assertEquals("brand-new", route.providerName());
        assertEquals(1100, route.option().distanceKm(), 1e-9);
    }
}
