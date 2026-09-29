package routeplanner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static routeplanner.testsupport.RouteFixtures.DESTINATION;
import static routeplanner.testsupport.RouteFixtures.LYON;
import static routeplanner.testsupport.RouteFixtures.ORIGIN;
import static routeplanner.testsupport.RouteFixtures.OSAKA;
import static routeplanner.testsupport.RouteFixtures.PARIS;
import static routeplanner.testsupport.RouteFixtures.TOKYO;
import static routeplanner.testsupport.RouteFixtures.option;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import routeplanner.bridge.MapProvider;
import routeplanner.error.InvalidLocationException;
import routeplanner.model.Region;
import routeplanner.testsupport.StubMapProvider;

class ProviderSelectorTest {

    private final StubMapProvider world = StubMapProvider.returning("world", option(1, 1, 1));
    private final StubMapProvider europe = StubMapProvider.returning("europe", option(1, 1, 1))
            .covering(new Region("Europe", 34, 72, -11, 45));
    private final StubMapProvider asia = StubMapProvider.returning("asia", option(1, 1, 1))
            .covering(new Region("Central Asia", 35, 56, 46, 88));

    @Test
    @DisplayName("Providers are chosen from the coordinates, most specific coverage first")
    void choosesProvidersFromInputCoordinates() throws Exception {
        ProviderSelector selector = new ProviderSelector(List.of(world, europe, asia));

        List<MapProvider> central = selector.candidatesFor(ORIGIN, DESTINATION);
        List<MapProvider> french = selector.candidatesFor(PARIS, LYON);
        List<MapProvider> japanese = selector.candidatesFor(TOKYO, OSAKA);

        assertEquals(2, central.size());
        assertSame(asia, central.get(0));
        assertSame(world, central.get(1));
        assertEquals(2, french.size());
        assertSame(europe, french.get(0));
        assertEquals(1, japanese.size());
        assertSame(world, japanese.get(0));
    }

    @Test
    @DisplayName("The order in which providers were registered does not matter")
    void registrationOrderIsIrrelevant() throws Exception {
        ProviderSelector selector = new ProviderSelector(List.of(asia, europe, world));

        assertSame(asia, selector.candidatesFor(ORIGIN, DESTINATION).get(0));
    }

    @Test
    @DisplayName("A route that is not fully covered by a provider skips that provider")
    void routeSpanningTwoRegionsSkipsRegionalProviders() throws Exception {
        ProviderSelector selector = new ProviderSelector(List.of(world, europe, asia));

        List<MapProvider> candidates = selector.candidatesFor(PARIS, ORIGIN);

        assertEquals(1, candidates.size());
        assertSame(world, candidates.get(0));
    }

    @Test
    @DisplayName("No covering provider results in InvalidLocationException")
    void noCoveringProviderIsReported() {
        ProviderSelector selector = new ProviderSelector(List.of(europe));

        assertThrows(InvalidLocationException.class, () -> selector.candidatesFor(TOKYO, OSAKA));
    }
}
