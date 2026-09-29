package routeplanner.app;

import java.util.Locale;
import routeplanner.error.RoutingException;
import routeplanner.legacy.LegacyGeoRouter;
import routeplanner.model.Coordinate;
import routeplanner.model.Route;
import routeplanner.model.RouteRequest;
import routeplanner.service.RoutingService;

public final class Main {

    private static final Coordinate ASTANA = new Coordinate(51.1694, 71.4491);
    private static final Coordinate ALMATY = new Coordinate(43.2389, 76.8897);
    private static final Coordinate PARIS = new Coordinate(48.8566, 2.3522);
    private static final Coordinate LYON = new Coordinate(45.7640, 4.8357);
    private static final Coordinate TOKYO = new Coordinate(35.6762, 139.6503);
    private static final Coordinate OSAKA = new Coordinate(34.6937, 135.5023);

    private Main() {
    }

    public static void main(String[] args) {
        LegacyGeoRouter legacyRouter = new LegacyGeoRouter();
        RoutingService service = RoutingConfig.defaultService(legacyRouter);

        if (args.length == 5) {
            runFromArguments(service, args);
            return;
        }

        show(service, new RouteRequest(ASTANA, ALMATY, "fastest"));
        show(service, new RouteRequest(ASTANA, ALMATY, "scenic"));
        show(service, new RouteRequest(PARIS, LYON, "scenic"));
        show(service, new RouteRequest(TOKYO, OSAKA, "shortest"));

        System.out.println();
        System.out.println("Legacy routing link goes down:");
        legacyRouter.unlink();
        show(service, new RouteRequest(ASTANA, ALMATY, "fastest"));
    }

    private static void runFromArguments(RoutingService service, String[] args) {
        try {
            Coordinate origin = new Coordinate(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
            Coordinate destination = new Coordinate(Double.parseDouble(args[2]), Double.parseDouble(args[3]));
            show(service, new RouteRequest(origin, destination, args[4]));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
            System.out.println("Usage: <lat1> <lon1> <lat2> <lon2> <fastest|scenic|shortest>");
        }
    }

    private static void show(RoutingService service, RouteRequest request) {
        try {
            Route route = service.route(request);
            System.out.println(String.format(Locale.ROOT,
                    "%-8s via %-9s %8.1f km  %5d min  scenic %3d  waypoints %d",
                    route.strategy(), route.providerName(),
                    route.option().distanceKm(), route.option().duration().toMinutes(),
                    route.option().scenicScore(), route.option().path().size()));
        } catch (RoutingException | IllegalArgumentException e) {
            System.out.println("Routing failed: " + e.getMessage());
        }
    }
}
