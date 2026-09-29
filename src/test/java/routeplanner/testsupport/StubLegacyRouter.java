package routeplanner.testsupport;

import java.util.ArrayList;
import java.util.List;
import routeplanner.legacy.LegacyGeoRouter;
import routeplanner.legacy.LegacyRoute;
import routeplanner.legacy.LegacyRouteSet;

public final class StubLegacyRouter extends LegacyGeoRouter {

    private final List<LegacyRoute> routes = new ArrayList<>();
    private int status = STATUS_OK;
    private RuntimeException failure;
    private double[] lastStart;
    private double[] lastEnd;
    private int calls;

    public StubLegacyRouter withRoute(LegacyRoute route) {
        routes.add(route);
        return this;
    }

    public StubLegacyRouter withStatus(int status) {
        this.status = status;
        return this;
    }

    public StubLegacyRouter throwing(RuntimeException failure) {
        this.failure = failure;
        return this;
    }

    @Override
    public int solve(double[] start, double[] end, LegacyRouteSet out) {
        calls++;
        lastStart = start;
        lastEnd = end;
        if (failure != null) {
            throw failure;
        }
        routes.forEach(out::add);
        return status;
    }

    public double[] lastStart() {
        return lastStart;
    }

    public double[] lastEnd() {
        return lastEnd;
    }

    public int callCount() {
        return calls;
    }
}
