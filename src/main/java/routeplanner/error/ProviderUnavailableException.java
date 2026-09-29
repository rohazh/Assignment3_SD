package routeplanner.error;

public class ProviderUnavailableException extends RoutingException {

    private static final long serialVersionUID = 1L;

    public ProviderUnavailableException(String message) {
        super(message);
    }
}
