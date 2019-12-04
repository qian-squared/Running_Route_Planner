package justrun.running.routeplanner;

import com.google.android.gms.maps.model.PolylineOptions;

public class Myroute
{
    public String name;
    public String distance;
    public String time;
    public String polyline;

    //public PolylineOptions polylineOptions;

    Myroute( String name, String distance, String time, String polyline) {
        this.name = name;
        this.distance = distance;
        this.time = time;
        this.polyline = polyline;
        //this.polylineOptions = polylineOptions;
    }
}
