package routeplanner.bridge;

import java.util.List;
import java.util.Objects;
import routeplanner.error.InvalidLocationException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.error.RoutingException;
import routeplanner.model.Coordinate;
import routeplanner.model.Route;
import routeplanner.model.RouteOption;

public abstract class RoutePlanner {

    private final MapProvider provider;

    protected RoutePlanner(MapProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    public final Route plan(Coordinate origin, Coordinate destination) throws RoutingException {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        if (origin.equals(destination)) {
            throw new InvalidLocationException("Origin and destination must differ");
        }
        List<RouteOption> options = provider.findRoutes(origin, destination);
        if (options == null || options.isEmpty()) {
            throw new RouteNotFoundException(provider.name() + " returned no routes");
        }
        return new Route(strategyName(), provider.name(), select(options));
    }

    public abstract String strategyName();

    protected abstract RouteOption select(List<RouteOption> options);
}
