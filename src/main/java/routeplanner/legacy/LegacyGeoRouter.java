package routeplanner.legacy;

import java.util.Locale;

public class LegacyGeoRouter {

    public static final int STATUS_OK = 0;
    public static final int STATUS_NO_ROAD = 17;
    public static final int STATUS_BAD_COORD = 22;
    public static final int STATUS_BUSY = 503;

    private static final double MILES_PER_KM = 0.621371;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double MAX_ROAD_KM = 6000;

    private boolean linked = true;
    private boolean busy = false;

    public void link() {
        linked = true;
    }

    public void unlink() {
        linked = false;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public int solve(double[] start, double[] end, LegacyRouteSet out) {
        if (!linked) {
            throw new LegacyLinkException("routing index link lost");
        }
        if (busy) {
            return STATUS_BUSY;
        }
        if (!validPoint(start) || !validPoint(end)) {
            return STATUS_BAD_COORD;
        }
        double km = distanceKm(start, end);
        if (km > MAX_ROAD_KM) {
            return STATUS_NO_ROAD;
        }
        out.clear();
        out.add(new LegacyRoute(waypoints(start, end, false), km * 1.12 * MILES_PER_KM,
                minutes(km * 1.12 * MILES_PER_KM, 55), 'D'));
        out.add(new LegacyRoute(waypoints(start, end, true), km * 1.30 * MILES_PER_KM,
                minutes(km * 1.30 * MILES_PER_KM, 45), 'B'));
        return STATUS_OK;
    }

    private static boolean validPoint(double[] point) {
        return point != null && point.length == 2
                && point[0] >= -180 && point[0] <= 180
                && point[1] >= -90 && point[1] <= 90;
    }

    private static int minutes(double miles, double mph) {
        return (int) Math.round(miles / mph * 60);
    }

    private static double distanceKm(double[] a, double[] b) {
        double dLat = Math.toRadians(b[1] - a[1]);
        double dLon = Math.toRadians(b[0] - a[0]);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(a[1])) * Math.cos(Math.toRadians(b[1]))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1, Math.sqrt(h)));
    }

    private static String waypoints(double[] start, double[] end, boolean viaBend) {
        StringBuilder sb = new StringBuilder(format(start));
        if (viaBend) {
            double lon = (start[0] + end[0]) / 2 + (end[1] - start[1]) * 0.06;
            double lat = (start[1] + end[1]) / 2 - (end[0] - start[0]) * 0.06;
            sb.append(';').append(format(new double[]{
                    Math.max(-180, Math.min(180, lon)), Math.max(-90, Math.min(90, lat))}));
        }
        return sb.append(';').append(format(end)).toString();
    }

    private static String format(double[] point) {
        return String.format(Locale.ROOT, "%.5f,%.5f", point[0], point[1]);
    }
}
