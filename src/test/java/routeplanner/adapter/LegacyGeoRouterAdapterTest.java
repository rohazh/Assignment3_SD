package routeplanner.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static routeplanner.testsupport.RouteFixtures.DESTINATION;
import static routeplanner.testsupport.RouteFixtures.ORIGIN;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import routeplanner.bridge.FastestRoutePlanner;
import routeplanner.bridge.ScenicRoutePlanner;
import routeplanner.error.InvalidLocationException;
import routeplanner.error.ProviderUnavailableException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.error.RoutingException;
import routeplanner.legacy.LegacyGeoRouter;
import routeplanner.legacy.LegacyLinkException;
import routeplanner.legacy.LegacyRoute;
import routeplanner.model.Coordinate;
import routeplanner.model.Region;
import routeplanner.model.Route;
import routeplanner.model.RouteOption;
import routeplanner.testsupport.StubLegacyRouter;

class LegacyGeoRouterAdapterTest {

    private static final Region COVERAGE = new Region("Test", 0, 90, 0, 180);
    private static final String WAYPOINTS = "71.45000,51.17000;73.00000,47.00000;76.89000,43.24000";

    private StubLegacyRouter router;

    @BeforeEach
    void setUp() {
        router = new StubLegacyRouter();
    }

    private LegacyGeoRouterAdapter adapter() {
        return new LegacyGeoRouterAdapter(router, COVERAGE);
    }

    @Test
    @DisplayName("Legacy units, coordinate order and grades are converted to the Implementor contract")
    void convertsUnitsCoordinateOrderAndGrades() throws Exception {
        router.withRoute(new LegacyRoute(WAYPOINTS, 100.0, 90, 'B'));

        List<RouteOption> options = adapter().findRoutes(ORIGIN, DESTINATION);

        assertEquals(1, options.size());
        RouteOption option = options.get(0);
        assertEquals(160.9344, option.distanceKm(), 1e-6);
        assertEquals(Duration.ofMinutes(90), option.duration());
        assertEquals(80, option.scenicScore());
        assertEquals(3, option.path().size());
        assertEquals(new Coordinate(51.17, 71.45), option.path().get(0));
        assertEquals(new Coordinate(43.24, 76.89), option.path().get(2));
    }

    @Test
    @DisplayName("The legacy router receives (longitude, latitude) arrays")
    void passesLongitudeFirstArraysToLegacyRouter() throws Exception {
        router.withRoute(new LegacyRoute(WAYPOINTS, 100.0, 90, 'B'));

        adapter().findRoutes(ORIGIN, DESTINATION);

        assertEquals(1, router.callCount());
        assertEquals(71.45, router.lastStart()[0], 1e-9);
        assertEquals(51.17, router.lastStart()[1], 1e-9);
        assertEquals(76.89, router.lastEnd()[0], 1e-9);
        assertEquals(43.24, router.lastEnd()[1], 1e-9);
    }

    @Test
    @DisplayName("Adapter reports the injected coverage and a stable provider name")
    void exposesCoverageAndName() {
        assertSame(COVERAGE, adapter().coverage());
        assertEquals("LegacyGeo", adapter().name());
    }

    @Test
    @DisplayName("Status 'no road' becomes RouteNotFoundException")
    void noRoadStatusBecomesRouteNotFound() {
        router.withStatus(LegacyGeoRouter.STATUS_NO_ROAD);

        assertThrows(RouteNotFoundException.class, () -> adapter().findRoutes(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("Status 'bad coordinates' becomes InvalidLocationException")
    void badCoordinateStatusBecomesInvalidLocation() {
        router.withStatus(LegacyGeoRouter.STATUS_BAD_COORD);

        assertThrows(InvalidLocationException.class, () -> adapter().findRoutes(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("Status 'busy' becomes ProviderUnavailableException")
    void busyStatusBecomesProviderUnavailable() {
        router.withStatus(LegacyGeoRouter.STATUS_BUSY);

        assertThrows(ProviderUnavailableException.class, () -> adapter().findRoutes(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("An unknown status code becomes ProviderUnavailableException")
    void unknownStatusBecomesProviderUnavailable() {
        router.withStatus(999);

        assertThrows(ProviderUnavailableException.class, () -> adapter().findRoutes(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("A legacy link exception becomes ProviderUnavailableException")
    void legacyLinkExceptionBecomesProviderUnavailable() {
        router.throwing(new LegacyLinkException("link lost"));

        assertThrows(ProviderUnavailableException.class, () -> adapter().findRoutes(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("Any other runtime failure of the legacy router becomes ProviderUnavailableException")
    void unexpectedRuntimeFailureBecomesProviderUnavailable() {
        router.throwing(new IllegalStateException("boom"));

        assertThrows(ProviderUnavailableException.class, () -> adapter().findRoutes(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("Success status with no routes becomes RouteNotFoundException")
    void successWithoutRoutesBecomesRouteNotFound() {
        assertThrows(RouteNotFoundException.class, () -> adapter().findRoutes(ORIGIN, DESTINATION));
    }

    @Test
    @DisplayName("Malformed waypoints and unknown grades become ProviderUnavailableException")
    void malformedDataBecomesProviderUnavailable() {
        StubLegacyRouter garbledPath = new StubLegacyRouter().withRoute(new LegacyRoute("abc", 10, 10, 'A'));
        StubLegacyRouter badNumber = new StubLegacyRouter().withRoute(new LegacyRoute("x,y;1,2", 10, 10, 'A'));
        StubLegacyRouter badRange = new StubLegacyRouter().withRoute(new LegacyRoute("10,95;20,30", 10, 10, 'A'));
        StubLegacyRouter badGrade = new StubLegacyRouter().withRoute(new LegacyRoute(WAYPOINTS, 10, 10, 'Z'));
        StubLegacyRouter nullPath = new StubLegacyRouter().withRoute(new LegacyRoute(null, 10, 10, 'A'));
        StubLegacyRouter negative = new StubLegacyRouter().withRoute(new LegacyRoute(WAYPOINTS, -5, 10, 'A'));

        for (StubLegacyRouter broken : List.of(garbledPath, badNumber, badRange, badGrade, nullPath, negative)) {
            LegacyGeoRouterAdapter adapter = new LegacyGeoRouterAdapter(broken, COVERAGE);
            assertThrows(ProviderUnavailableException.class, () -> adapter.findRoutes(ORIGIN, DESTINATION));
        }
    }

    @Test
    @DisplayName("No legacy-specific type, cause or wording leaks through any failure")
    void noLegacyDetailsLeakThroughFailures() {
        List<StubLegacyRouter> failing = List.of(
                new StubLegacyRouter().withStatus(LegacyGeoRouter.STATUS_NO_ROAD),
                new StubLegacyRouter().withStatus(LegacyGeoRouter.STATUS_BAD_COORD),
                new StubLegacyRouter().withStatus(LegacyGeoRouter.STATUS_BUSY),
                new StubLegacyRouter().withStatus(12345),
                new StubLegacyRouter().throwing(new LegacyLinkException("link lost")),
                new StubLegacyRouter().throwing(new IllegalStateException("boom")),
                new StubLegacyRouter().withRoute(new LegacyRoute("abc", 1, 1, 'A')));

        for (StubLegacyRouter stub : failing) {
            LegacyGeoRouterAdapter adapter = new LegacyGeoRouterAdapter(stub, COVERAGE);
            RoutingException thrown = assertThrows(RoutingException.class,
                    () -> adapter.findRoutes(ORIGIN, DESTINATION));
            assertTrue(thrown.getClass().getName().startsWith("routeplanner.error."));
            assertNull(thrown.getCause());
            String message = thrown.getMessage().toLowerCase();
            assertTrue(!message.contains("legacy") && !message.contains("status")
                    && !message.contains("17") && !message.contains("503"));
        }
    }

    @Test
    @DisplayName("Adapter works as the Implementor behind two different refined abstractions")
    void adapterServesTwoRefinedAbstractions() throws Exception {
        router.withRoute(new LegacyRoute(WAYPOINTS, 400.0, 500, 'D'))
                .withRoute(new LegacyRoute(WAYPOINTS, 460.0, 640, 'A'));
        LegacyGeoRouterAdapter adapter = adapter();

        Route fastest = new FastestRoutePlanner(adapter).plan(ORIGIN, DESTINATION);
        Route scenic = new ScenicRoutePlanner(adapter).plan(ORIGIN, DESTINATION);

        assertEquals("LegacyGeo", fastest.providerName());
        assertEquals(Duration.ofMinutes(500), fastest.option().duration());
        assertEquals(Duration.ofMinutes(640), scenic.option().duration());
        assertEquals(100, scenic.option().scenicScore());
    }
}
