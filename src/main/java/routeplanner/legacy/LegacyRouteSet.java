package routeplanner.legacy;

import java.util.ArrayList;
import java.util.List;

public class LegacyRouteSet {

    private final List<LegacyRoute> routes = new ArrayList<>();

    public void add(LegacyRoute route) {
        routes.add(route);
    }

    public void clear() {
        routes.clear();
    }

    public int size() {
        return routes.size();
    }

    public LegacyRoute get(int index) {
        return routes.get(index);
    }
}
