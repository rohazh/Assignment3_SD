package routeplanner.bridge;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import routeplanner.model.RouteOption;

public class ScenicRoutePlanner extends RoutePlanner {

    public static final double DEFAULT_MAX_DETOUR = 1.6;

    private final double maxDetourFactor;

    public ScenicRoutePlanner(MapProvider provider) {
        this(provider, DEFAULT_MAX_DETOUR);
    }

    public ScenicRoutePlanner(MapProvider provider, double maxDetourFactor) {
        super(provider);
        if (Double.isNaN(maxDetourFactor) || maxDetourFactor < 1.0) {
            throw new IllegalArgumentException("Detour factor must be at least 1.0: " + maxDetourFactor);
        }
        this.maxDetourFactor = maxDetourFactor;
    }

    @Override
    public String strategyName() {
        return "scenic";
    }

    @Override
    protected RouteOption select(List<RouteOption> options) {
        Duration fastest = options.stream()
                .map(RouteOption::duration)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        double limitSeconds = fastest.toSeconds() * maxDetourFactor;
        return options.stream()
                .filter(option -> option.duration().toSeconds() <= limitSeconds)
                .max(Comparator.comparingInt(RouteOption::scenicScore)
                        .thenComparing(RouteOption::duration, Comparator.reverseOrder()))
                .orElseThrow();
    }
}
