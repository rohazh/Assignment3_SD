package routeplanner.service;

import java.util.Comparator;
import java.util.List;
import routeplanner.bridge.MapProvider;
import routeplanner.error.InvalidLocationException;
import routeplanner.model.Coordinate;

public final class ProviderSelector {

    private final List<MapProvider> providers;

    public ProviderSelector(List<MapProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public List<MapProvider> candidatesFor(Coordinate origin, Coordinate destination)
            throws InvalidLocationException {
        List<MapProvider> candidates = providers.stream()
                .filter(provider -> provider.coverage().contains(origin)
                        && provider.coverage().contains(destination))
                .sorted(Comparator.comparingDouble((MapProvider provider) -> provider.coverage().area()))
                .toList();
        if (candidates.isEmpty()) {
            throw new InvalidLocationException("No provider covers the requested points");
        }
        return candidates;
    }
}
