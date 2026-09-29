package routeplanner.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import routeplanner.bridge.MapProvider;
import routeplanner.error.ProviderUnavailableException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.error.RoutingException;
import routeplanner.legacy.LegacyGeoRouter;
import routeplanner.legacy.LegacyLinkException;
import routeplanner.legacy.LegacyRouteSet;
import routeplanner.model.Coordinate;
import routeplanner.model.Region;
import routeplanner.model.RouteOption;

public final class LegacyGeoRouterAdapter implements MapProvider {

    private final LegacyGeoRouter router;
    private final Region coverage;
    private final LegacyRouteConverter converter = new LegacyRouteConverter();
    private final LegacyStatusTranslator translator = new LegacyStatusTranslator();

    public LegacyGeoRouterAdapter(LegacyGeoRouter router, Region coverage) {
        this.router = Objects.requireNonNull(router, "router");
        this.coverage = Objects.requireNonNull(coverage, "coverage");
    }

    @Override
    public String name() {
        return "LegacyGeo";
    }

    @Override
    public Region coverage() {
        return coverage;
    }

    @Override
    public List<RouteOption> findRoutes(Coordinate origin, Coordinate destination) throws RoutingException {
        LegacyRouteSet result = new LegacyRouteSet();
        int status = callRouter(origin, destination, result);
        translator.requireSuccess(status);
        return convertAll(result);
    }

    private int callRouter(Coordinate origin, Coordinate destination, LegacyRouteSet result)
            throws ProviderUnavailableException {
        try {
            return router.solve(lonLat(origin), lonLat(destination), result);
        } catch (LegacyLinkException e) {
            throw new ProviderUnavailableException("Routing link is down");
        } catch (RuntimeException e) {
            throw new ProviderUnavailableException("Provider failed unexpectedly");
        }
    }

    private List<RouteOption> convertAll(LegacyRouteSet result) throws RoutingException {
        if (result.size() == 0) {
            throw new RouteNotFoundException("Provider returned no routes");
        }
        try {
            List<RouteOption> options = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                options.add(converter.convert(result.get(i)));
            }
            return options;
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ProviderUnavailableException("Provider returned malformed route data");
        }
    }

    private static double[] lonLat(Coordinate point) {
        return new double[]{point.longitude(), point.latitude()};
    }
}
