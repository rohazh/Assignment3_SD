package routeplanner.bridge;

import java.util.Comparator;
import java.util.List;
import routeplanner.model.RouteOption;

public class ShortestRoutePlanner extends RoutePlanner {

    public ShortestRoutePlanner(MapProvider provider) {
        super(provider);
    }

    @Override
    public String strategyName() {
        return "shortest";
    }

    @Override
    protected RouteOption select(List<RouteOption> options) {
        return options.stream()
                .min(Comparator.comparingDouble(RouteOption::distanceKm))
                .orElseThrow();
    }
}
