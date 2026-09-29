package routeplanner.testsupport;

import java.util.ArrayList;
import java.util.List;
import routeplanner.bridge.MapProvider;
import routeplanner.error.RoutingException;
import routeplanner.model.Coordinate;
import routeplanner.model.Region;
import routeplanner.model.RouteOption;

public final class StubMapProvider implements MapProvider {

    private final String name;
    private final Region coverage;
    private final List<RouteOption> options;
    private final RoutingException failure;
    private final List<Coordinate> origins = new ArrayList<>();
    private final List<Coordinate> destinations = new ArrayList<>();

    private StubMapProvider(String name, Region coverage, List<RouteOption> options, RoutingException failure) {
        this.name = name;
        this.coverage = coverage;
        this.options = options;
        this.failure = failure;
    }

    public static StubMapProvider returning(String name, RouteOption... options) {
        return new StubMapProvider(name, Region.world(), List.of(options), null);
    }

    public static StubMapProvider failing(String name, RoutingException failure) {
        return new StubMapProvider(name, Region.world(), List.of(), failure);
    }

    public StubMapProvider covering(Region region) {
        return new StubMapProvider(name, region, options, failure);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Region coverage() {
        return coverage;
    }

    @Override
    public List<RouteOption> findRoutes(Coordinate origin, Coordinate destination) throws RoutingException {
        origins.add(origin);
        destinations.add(destination);
        if (failure != null) {
            throw failure;
        }
        return options;
    }

    public int callCount() {
        return origins.size();
    }

    public Coordinate lastOrigin() {
        return origins.get(origins.size() - 1);
    }

    public Coordinate lastDestination() {
        return destinations.get(destinations.size() - 1);
    }
}
