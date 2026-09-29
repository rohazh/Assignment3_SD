package routeplanner.adapter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import routeplanner.legacy.LegacyRoute;
import routeplanner.model.Coordinate;
import routeplanner.model.RouteOption;

final class LegacyRouteConverter {

    private static final double KM_PER_MILE = 1.609344;

    RouteOption convert(LegacyRoute route) {
        return new RouteOption(
                parsePath(route.getWaypoints()),
                route.getMiles() * KM_PER_MILE,
                Duration.ofMinutes(route.getMinutes()),
                scenicScore(route.getGrade()));
    }

    private List<Coordinate> parsePath(String waypoints) {
        List<Coordinate> path = new ArrayList<>();
        for (String point : waypoints.split(";")) {
            String[] parts = point.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Malformed waypoint");
            }
            double longitude = Double.parseDouble(parts[0].trim());
            double latitude = Double.parseDouble(parts[1].trim());
            path.add(new Coordinate(latitude, longitude));
        }
        return path;
    }

    private int scenicScore(char grade) {
        return switch (Character.toUpperCase(grade)) {
            case 'A' -> 100;
            case 'B' -> 80;
            case 'C' -> 60;
            case 'D' -> 40;
            case 'E' -> 20;
            default -> throw new IllegalArgumentException("Unknown scenery grade");
        };
    }
}
