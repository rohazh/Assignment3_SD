package routeplanner.provider;

import java.util.List;
import routeplanner.bridge.MapProvider;
import routeplanner.error.InvalidLocationException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.model.Coordinate;
import routeplanner.model.GeoMath;
import routeplanner.model.Region;
import routeplanner.model.RouteOption;

public final class AlpineMapProvider implements MapProvider {

    private static final Region EUROPE = new Region("Europe", 34, 72, -11, 45);
    private static final double MAX_ROAD_DISTANCE_KM = 4500;

    @Override
    public String name() {
        return "Alpine";
    }

    @Override
    public Region coverage() {
        return EUROPE;
    }

    @Override
    public List<RouteOption> findRoutes(Coordinate origin, Coordinate destination)
            throws InvalidLocationException, RouteNotFoundException {
        if (!EUROPE.contains(origin) || !EUROPE.contains(destination)) {
            throw new InvalidLocationException("Alpine covers Europe only");
        }
        double direct = GeoMath.distanceKm(origin, destination);
        if (direct > MAX_ROAD_DISTANCE_KM) {
            throw new RouteNotFoundException("Alpine found no road connection over " + Math.round(direct) + " km");
        }
        RouteOption motorway = RouteEstimator.estimate(
                List.of(origin, destination), direct * 1.10, 100, 15);
        RouteOption nationalRoad = RouteEstimator.estimate(
                List.of(origin, GeoMath.bend(origin, destination, 0.05), destination), direct * 1.18, 85, 55);
        RouteOption mountainPass = RouteEstimator.estimate(
                List.of(origin, GeoMath.bend(origin, destination, -0.12), destination), direct * 1.30, 75, 90);
        return List.of(motorway, nationalRoad, mountainPass);
    }
}
