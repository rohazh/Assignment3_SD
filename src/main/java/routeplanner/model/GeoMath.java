package routeplanner.model;

public final class GeoMath {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private GeoMath() {
    }

    public static double distanceKm(Coordinate from, Coordinate to) {
        double dLat = Math.toRadians(to.latitude() - from.latitude());
        double dLon = Math.toRadians(to.longitude() - from.longitude());
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(from.latitude()))
                * Math.cos(Math.toRadians(to.latitude()))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    public static Coordinate bend(Coordinate from, Coordinate to, double factor) {
        double dLat = to.latitude() - from.latitude();
        double dLon = to.longitude() - from.longitude();
        double lat = (from.latitude() + to.latitude()) / 2 - dLon * factor;
        double lon = (from.longitude() + to.longitude()) / 2 + dLat * factor;
        return new Coordinate(clamp(lat, -90, 90), clamp(lon, -180, 180));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
