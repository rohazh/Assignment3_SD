package routeplanner.legacy;

public final class LegacyRoute {

    private final String waypoints;
    private final double miles;
    private final int minutes;
    private final char grade;

    public LegacyRoute(String waypoints, double miles, int minutes, char grade) {
        this.waypoints = waypoints;
        this.miles = miles;
        this.minutes = minutes;
        this.grade = grade;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public double getMiles() {
        return miles;
    }

    public int getMinutes() {
        return minutes;
    }

    public char getGrade() {
        return grade;
    }
}
