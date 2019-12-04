package justrun.running.routeplanner.distanceModule;

import java.util.List;

/**
 * Created by Ahsen Saeed on 5/15/2017.
 */
public interface DistanceFinderListener {

    void onDistanceFinderSuccess(List<Destination> destinations);

    void onDistanceFinderStart();

}
