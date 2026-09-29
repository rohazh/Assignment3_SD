package routeplanner.testsupport;

import java.time.Duration;
import java.util.List;
import routeplanner.model.Coordinate;
import routeplanner.model.RouteOption;

public final class RouteFixtures {

    public static final Coordinate ORIGIN = new Coordinate(51.17, 71.45);
    public static final Coordinate DESTINATION = new Coordinate(43.24, 76.89);
    public static final Coordinate PARIS = new Coordinate(48.8566, 2.3522);
    public static final Coordinate LYON = new Coordinate(45.7640, 4.8357);
    public static final Coordinate TOKYO = new Coordinate(35.6762, 139.6503);
    public static final Coordinate OSAKA = new Coordinate(34.6937, 135.5023);

    private RouteFixtures() {
    }

    public static RouteOption option(double distanceKm, long minutes, int scenicScore) {
        return new RouteOption(List.of(ORIGIN, DESTINATION), distanceKm, Duration.ofMinutes(minutes), scenicScore);
    }
}
