package routeplanner.provider;

import java.util.List;
import routeplanner.bridge.MapProvider;
import routeplanner.error.RouteNotFoundException;
import routeplanner.model.Coordinate;
import routeplanner.model.GeoMath;
import routeplanner.model.Region;
import routeplanner.model.RouteOption;

public final class AtlasMapProvider implements MapProvider {

    private static final double MAX_ROAD_DISTANCE_KM = 12000;

    @Override
    public String name() {
        return "Atlas";
    }

    @Override
    public Region coverage() {
        return Region.world();
    }

    @Override
    public List<RouteOption> findRoutes(Coordinate origin, Coordinate destination) throws RouteNotFoundException {
        double direct = GeoMath.distanceKm(origin, destination);
        if (direct > MAX_ROAD_DISTANCE_KM) {
            throw new RouteNotFoundException("Atlas found no road connection over " + Math.round(direct) + " km");
        }
        RouteOption highway = RouteEstimator.estimate(
                List.of(origin, destination), direct * 1.15, 90, 25);
        RouteOption countryRoad = RouteEstimator.estimate(
                List.of(origin, GeoMath.bend(origin, destination, 0.08), destination), direct * 1.30, 70, 70);
        return List.of(highway, countryRoad);
    }
}
