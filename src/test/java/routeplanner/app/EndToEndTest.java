package routeplanner.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static routeplanner.testsupport.RouteFixtures.DESTINATION;
import static routeplanner.testsupport.RouteFixtures.LYON;
import static routeplanner.testsupport.RouteFixtures.ORIGIN;
import static routeplanner.testsupport.RouteFixtures.OSAKA;
import static routeplanner.testsupport.RouteFixtures.PARIS;
import static routeplanner.testsupport.RouteFixtures.TOKYO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import routeplanner.legacy.LegacyGeoRouter;
import routeplanner.model.Route;
import routeplanner.model.RouteRequest;
import routeplanner.service.RoutingService;

class EndToEndTest {

    private LegacyGeoRouter legacyRouter;
    private RoutingService service;

    @BeforeEach
    void setUp() {
        legacyRouter = new LegacyGeoRouter();
        service = RoutingConfig.defaultService(legacyRouter);
    }

    @Test
    @DisplayName("Central Asian input is routed through the adapted legacy provider")
    void centralAsiaUsesLegacyProvider() throws Exception {
        Route fastest = service.route(new RouteRequest(ORIGIN, DESTINATION, "fastest"));
        Route scenic = service.route(new RouteRequest(ORIGIN, DESTINATION, "scenic"));

        assertEquals("LegacyGeo", fastest.providerName());
        assertEquals(40, fastest.option().scenicScore());
        assertEquals("LegacyGeo", scenic.providerName());
        assertEquals(80, scenic.option().scenicScore());
    }

    @Test
    @DisplayName("European input is routed through the Alpine provider")
    void europeUsesAlpineProvider() throws Exception {
        Route scenic = service.route(new RouteRequest(PARIS, LYON, "scenic"));

        assertEquals("Alpine", scenic.providerName());
        assertEquals(90, scenic.option().scenicScore());
    }

    @Test
    @DisplayName("Any other input is routed through the Atlas provider")
    void restOfTheWorldUsesAtlasProvider() throws Exception {
        Route shortest = service.route(new RouteRequest(TOKYO, OSAKA, "shortest"));

        assertEquals("Atlas", shortest.providerName());
    }

    @Test
    @DisplayName("When the legacy link is down the request falls back to Atlas")
    void legacyLinkFailureFallsBackToAtlas() throws Exception {
        legacyRouter.unlink();

        Route route = service.route(new RouteRequest(ORIGIN, DESTINATION, "fastest"));

        assertEquals("Atlas", route.providerName());
    }

    @Test
    @DisplayName("When the legacy router is busy the request falls back to Atlas")
    void legacyBusyFallsBackToAtlas() throws Exception {
        legacyRouter.setBusy(true);

        Route route = service.route(new RouteRequest(ORIGIN, DESTINATION, "scenic"));

        assertEquals("Atlas", route.providerName());
    }
}
