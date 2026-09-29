package routeplanner.bridge;

import java.util.List;
import routeplanner.error.RoutingException;
import routeplanner.model.Coordinate;
import routeplanner.model.Region;
import routeplanner.model.RouteOption;

public interface MapProvider {

    String name();

    Region coverage();

    List<RouteOption> findRoutes(Coordinate origin, Coordinate destination) throws RoutingException;
}
