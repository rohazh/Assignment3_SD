package routeplanner.bridge;

import java.util.Comparator;
import java.util.List;
import routeplanner.model.RouteOption;

public class FastestRoutePlanner extends RoutePlanner {

    public FastestRoutePlanner(MapProvider provider) {
        super(provider);
    }

    @Override
    public String strategyName() {
        return "fastest";
    }

    @Override
    protected RouteOption select(List<RouteOption> options) {
        return options.stream()
                .min(Comparator.comparing(RouteOption::duration))
                .orElseThrow();
    }
}
