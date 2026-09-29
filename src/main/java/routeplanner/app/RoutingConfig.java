package routeplanner.app;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import routeplanner.adapter.LegacyGeoRouterAdapter;
import routeplanner.bridge.FastestRoutePlanner;
import routeplanner.bridge.MapProvider;
import routeplanner.bridge.RoutePlanner;
import routeplanner.bridge.ScenicRoutePlanner;
import routeplanner.bridge.ShortestRoutePlanner;
import routeplanner.legacy.LegacyGeoRouter;
import routeplanner.model.Region;
import routeplanner.provider.AlpineMapProvider;
import routeplanner.provider.AtlasMapProvider;
import routeplanner.service.ProviderSelector;
import routeplanner.service.RoutingService;

public final class RoutingConfig {

    private static final Region CENTRAL_ASIA = new Region("Central Asia", 35, 56, 46, 88);

    private RoutingConfig() {
    }

    public static RoutingService defaultService(LegacyGeoRouter legacyRouter) {
        List<MapProvider> providers = List.of(
                new AtlasMapProvider(),
                new AlpineMapProvider(),
                new LegacyGeoRouterAdapter(legacyRouter, CENTRAL_ASIA));
        Map<String, Function<MapProvider, RoutePlanner>> planners = Map.of(
                "fastest", FastestRoutePlanner::new,
                "scenic", ScenicRoutePlanner::new,
                "shortest", ShortestRoutePlanner::new);
        return new RoutingService(new ProviderSelector(providers), planners);
    }
}
