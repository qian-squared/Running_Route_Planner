package spartons.com.googlemapspolylineexample;

import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
//import android.support.v4.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
//import android.support.v7.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
//import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // sign in
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import spartons.com.googlemapspolylineexample.directionModules.DirectionFinder;
import spartons.com.googlemapspolylineexample.directionModules.DirectionFinderListener;
import spartons.com.googlemapspolylineexample.directionModules.Route;
import spartons.com.googlemapspolylineexample.distanceModule.Destination;
import spartons.com.googlemapspolylineexample.distanceModule.DistanceFinder;
import spartons.com.googlemapspolylineexample.distanceModule.DistanceFinderListener;
import spartons.com.googlemapspolylineexample.util.RecyclerViewAdapter;

import static androidx.recyclerview.widget.RecyclerView.*;
import static spartons.com.googlemapspolylineexample.util.GoogleMapHelper.buildCameraUpdate;
import static spartons.com.googlemapspolylineexample.util.GoogleMapHelper.getDefaultPolyLines;
import static spartons.com.googlemapspolylineexample.util.UiHelper.showAlwaysCircularProgressDialog;


public class MainActivity extends AppCompatActivity implements DistanceFinderListener, DirectionFinderListener {

    // create recycler
    private static final String TAG = "MainActivity";
    private ArrayList<String> mNames = new ArrayList<>();

    private static final String[] ELEVATION_OPTIONS = new String[]{
            "None",
            "Uphill",
            "Downhill"
    };

    private GoogleMap googleMap;
    private Polyline polyline;
    private EditText sourceAddressTextView;
    private MaterialDialog materialDialog;
    private LatLng Origin;
    private LatLng Destination;
    private static final int REQUEST_LOCATION = 500;
    private static final double r = 6371e3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap -> {
            //defaultMapSettings(googleMap);
            this.googleMap = googleMap;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
            else
                googleMap.setMyLocationEnabled(true);
            LatLng boston = new LatLng(42.361145, -71.057083);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(boston,10));
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    googleMap.clear();
                    MarkerOptions options = new MarkerOptions();
                    options.position(latLng);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    googleMap.addMarker(options);
                    Origin = latLng;
                }
            });


        });


        sourceAddressTextView = findViewById(R.id.sourceAddressTextView);
        //destinationAddressTextView = findViewById(R.id.destinationAddressTextView);

        AppCompatSpinner polylineStyleSpinner = findViewById(R.id.polylineStyleSpinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ELEVATION_OPTIONS );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        polylineStyleSpinner.setAdapter(adapter);
        /*
        polylineStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    polylineStyle = PolylineStyle.PLAIN;
                else if (position == 1)
                    polylineStyle = PolylineStyle.DOTTED;
                if (polyline == null || !polyline.isVisible())
                    return;
                List<LatLng> points = polyline.getPoints();
                polyline.remove();
                if (position == 0)
                    polyline = googleMap.addPolyline(getDefaultPolyLines(points));
                else if (position == 1)
                    polyline = googleMap.addPolyline(getDottedPolylines(points));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        */
        findViewById(R.id.getDirectionButton).setOnClickListener(view -> {
            int distance = Integer.valueOf(sourceAddressTextView.getText().toString());
            getLocation_1(70, Origin.longitude,Origin.latitude, distance*1000);
            String origin = Origin.latitude + "," + Origin.longitude;
            String destination = Destination.latitude + "," + Destination.longitude;
            //String origin = sourceAddressTextView.getText().toString();
            //String destination = destinationAddressTextView.getText().toString();
            if (origin.isEmpty() || destination.isEmpty()) {
                Toast.makeText(this, "Please select an origin!", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchDistances(origin, destination);
        });
    }

    public void fetchDistances(String origin, String destination) {
        try {
            new DistanceFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

// directions
    public void fetchDirections( String origin, String destination) {

        LatLng Destination = new LatLng ((Origin.latitude-0.01) , (Origin.longitude-0.01));
        MarkerOptions options = new MarkerOptions();
        options.position(Destination);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        googleMap.addMarker(options);

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onDirectionFinderStart() {

        if (materialDialog == null)
            materialDialog = showAlwaysCircularProgressDialog(this, "Finding routes...");
        else materialDialog.show();
    }


    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        int x = 1;
        System.out.println(x);
        if (materialDialog != null && materialDialog.isShowing())
            materialDialog.dismiss();
        if (!routes.isEmpty() && polyline != null) polyline.remove();
        try {
            for (Route route : routes) {
                PolylineOptions polylineOptions = getDefaultPolyLines(route.points);
                //System.out.println(polylineOptions);
                polyline = googleMap.addPolyline(polylineOptions);
                //System.out.println(polyline);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred on finding the directions...", Toast.LENGTH_SHORT).show();
        }

        googleMap.animateCamera(buildCameraUpdate(routes.get(0).endLocation), 10, null);
    }

    @Override
    public void onDistanceFinderStart() {
        if (materialDialog == null)
            materialDialog = showAlwaysCircularProgressDialog(this, "Finding routes...");
        else materialDialog.show();
    }

    @Override
    public void onDistanceFinderSuccess(List<Destination> destinations) {
        if (materialDialog != null && materialDialog.isShowing())
            materialDialog.dismiss();

        mNames.clear();
        mNames.add(destinations.get(0).distance.text);
        mNames.add("Unknown");
        mNames.add("Unknown");
        mNames.add("Unknown");
        mNames.add("Unknown");
        mNames.add("Unknown");
        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, Origin, Destination);
        recyclerView.setAdapter(adapter);


    }


    public void getLocation_1(double angle, double startLong,double startLat, double distance){

        double δ = distance/r;
        // convert into radian
        angle = Math.toRadians(angle);
        startLong = Math.toRadians(startLong);
        startLat = Math.toRadians(startLat);
        double lat = Math.asin(Math.sin(startLat)*Math.cos(δ)+Math.cos(startLat)*Math.sin(δ)*Math.cos(angle));
        double lon = startLong + Math.atan2(Math.sin(angle)*Math.sin(δ)*Math.cos(startLat),Math.cos(δ)-Math.sin(startLat)*Math.sin(lat));
        // convert lon, lat into decimal
        lon = Math.toDegrees(lon);
        lat = Math.toDegrees(lat);

        Destination = new LatLng(lat, lon);

        MarkerOptions options = new MarkerOptions();
        options.position(Destination);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        googleMap.addMarker(options);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Destination));
    }


}

