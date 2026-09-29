package routeplanner.adapter;

import routeplanner.error.InvalidLocationException;
import routeplanner.error.ProviderUnavailableException;
import routeplanner.error.RouteNotFoundException;
import routeplanner.error.RoutingException;
import routeplanner.legacy.LegacyGeoRouter;

final class LegacyStatusTranslator {

    void requireSuccess(int status) throws RoutingException {
        switch (status) {
            case LegacyGeoRouter.STATUS_OK -> {
            }
            case LegacyGeoRouter.STATUS_NO_ROAD ->
                    throw new RouteNotFoundException("No road connection between the given points");
            case LegacyGeoRouter.STATUS_BAD_COORD ->
                    throw new InvalidLocationException("Coordinates are not accepted by the provider");
            case LegacyGeoRouter.STATUS_BUSY ->
                    throw new ProviderUnavailableException("Provider is busy");
            default ->
                    throw new ProviderUnavailableException("Provider failed for an unrecognized reason");
        }
    }
}
