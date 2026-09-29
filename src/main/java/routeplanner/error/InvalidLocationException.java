package routeplanner.error;

public class InvalidLocationException extends RoutingException {

    private static final long serialVersionUID = 1L;

    public InvalidLocationException(String message) {
        super(message);
    }
}
