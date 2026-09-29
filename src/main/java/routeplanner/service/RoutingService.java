package routeplanner.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import routeplanner.bridge.MapProvider;
import routeplanner.bridge.RoutePlanner;
import routeplanner.error.ProviderUnavailableException;
import routeplanner.error.RoutingException;
import routeplanner.model.Route;
import routeplanner.model.RouteRequest;

public final class RoutingService {

    private final ProviderSelector selector;
    private final Map<String, Function<MapProvider, RoutePlanner>> plannerFactories;

    public RoutingService(ProviderSelector selector,
                          Map<String, Function<MapProvider, RoutePlanner>> plannerFactories) {
        this.selector = Objects.requireNonNull(selector, "selector");
        Map<String, Function<MapProvider, RoutePlanner>> normalized = new HashMap<>();
        plannerFactories.forEach((style, factory) -> normalized.put(style.toLowerCase(Locale.ROOT), factory));
        this.plannerFactories = Map.copyOf(normalized);
    }

    public Route route(RouteRequest request) throws RoutingException {
        Function<MapProvider, RoutePlanner> factory =
                plannerFactories.get(request.style().toLowerCase(Locale.ROOT));
        if (factory == null) {
            throw new IllegalArgumentException("Unknown planning style: " + request.style());
        }
        List<String> failures = new ArrayList<>();
        for (MapProvider provider : selector.candidatesFor(request.origin(), request.destination())) {
            try {
                return factory.apply(provider).plan(request.origin(), request.destination());
            } catch (ProviderUnavailableException e) {
                failures.add(provider.name() + ": " + e.getMessage());
            }
        }
        throw new ProviderUnavailableException("All providers failed - " + String.join("; ", failures));
    }
}
