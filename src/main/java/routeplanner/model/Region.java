package routeplanner.model;

import java.util.Objects;

public record Region(String name, double minLatitude, double maxLatitude,
                     double minLongitude, double maxLongitude) {

    public Region {
        Objects.requireNonNull(name, "name");
        if (minLatitude > maxLatitude || minLongitude > maxLongitude) {
            throw new IllegalArgumentException("Region bounds are inverted: " + name);
        }
    }

    public static Region world() {
        return new Region("World", -90, 90, -180, 180);
    }

    public boolean contains(Coordinate point) {
        return point.latitude() >= minLatitude && point.latitude() <= maxLatitude
                && point.longitude() >= minLongitude && point.longitude() <= maxLongitude;
    }

    public double area() {
        return (maxLatitude - minLatitude) * (maxLongitude - minLongitude);
    }
}
