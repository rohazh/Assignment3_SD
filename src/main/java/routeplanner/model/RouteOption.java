package routeplanner.model;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public record RouteOption(List<Coordinate> path, double distanceKm, Duration duration, int scenicScore) {

    public RouteOption {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(duration, "duration");
        if (path.size() < 2) {
            throw new IllegalArgumentException("A route needs at least two points");
        }
        if (Double.isNaN(distanceKm) || distanceKm < 0) {
            throw new IllegalArgumentException("Distance must not be negative: " + distanceKm);
        }
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Duration must not be negative");
        }
        if (scenicScore < 0 || scenicScore > 100) {
            throw new IllegalArgumentException("Scenic score must be within 0..100: " + scenicScore);
        }
        path = List.copyOf(path);
    }
}
