package routeplanner.model;

public record Coordinate(double latitude, double longitude) {

    public Coordinate {
        if (Double.isNaN(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude out of range: " + latitude);
        }
        if (Double.isNaN(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude out of range: " + longitude);
        }
    }
}
