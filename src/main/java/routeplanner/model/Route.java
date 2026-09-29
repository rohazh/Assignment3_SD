package routeplanner.model;

import java.util.Objects;

public record Route(String strategy, String providerName, RouteOption option) {

    public Route {
        Objects.requireNonNull(strategy, "strategy");
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(option, "option");
    }
}
