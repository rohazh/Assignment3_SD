package routeplanner.model;

import java.util.Objects;

public record RouteRequest(Coordinate origin, Coordinate destination, String style) {

    public RouteRequest {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(style, "style");
        if (style.isBlank()) {
            throw new IllegalArgumentException("Planning style must not be blank");
        }
    }
}
