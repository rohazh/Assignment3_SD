package routeplanner.error;

public class RouteNotFoundException extends RoutingException {

    private static final long serialVersionUID = 1L;

    public RouteNotFoundException(String message) {
        super(message);
    }
}
