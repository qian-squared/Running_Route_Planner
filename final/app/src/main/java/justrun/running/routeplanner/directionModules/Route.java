package justrun.running.routeplanner.directionModules;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Ahsen Saeed on 5/15/2017.
 */

public class Route {

    public Distance distance;
    public Duration duration;
    public LatLng endLocation;
    public LatLng startLocation;
    public String Polyline;

    public List<LatLng> points;
}
