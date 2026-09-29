package routeplanner.provider;

import java.time.Duration;
import java.util.List;
import routeplanner.model.Coordinate;
import routeplanner.model.RouteOption;

final class RouteEstimator {

    private RouteEstimator() {
    }

    static RouteOption estimate(List<Coordinate> path, double distanceKm, double speedKmh, int scenicScore) {
        long seconds = Math.round(distanceKm / speedKmh * 3600);
        return new RouteOption(path, distanceKm, Duration.ofSeconds(seconds), scenicScore);
    }
}
