package justrun.running.routeplanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
//import android.support.v4.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
//import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import justrun.running.routeplanner.directionModules.DirectionFinder;
import justrun.running.routeplanner.directionModules.DirectionFinderListener;
import justrun.running.routeplanner.directionModules.Route;
import justrun.running.routeplanner.distanceModule.Destination;
import justrun.running.routeplanner.distanceModule.DistanceFinder;
import justrun.running.routeplanner.distanceModule.DistanceFinderListener;
import justrun.running.routeplanner.util.RecyclerViewAdapter;
import justrun.running.routeplanner.util.SavedRouteRA;

import static justrun.running.routeplanner.util.GoogleMapHelper.getDefaultPolyLines;
import static justrun.running.routeplanner.util.UiHelper.showAlwaysCircularProgressDialog;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements DistanceFinderListener, DirectionFinderListener, NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
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
    private ArrayList<LatLng> Destinations = new ArrayList<>();
    private LatLng Destination;
    private static final int REQUEST_LOCATION = 500;
    private static final double r = 6371e3;
    private int pos = 0;
    private LatLng current;
    private PolylineOptions polylineOptions;
    private String distance;
    private String Time;
    private String str_poly;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user;
    private String m_Text = "";
    private List<Myroute> MyRouteList = new ArrayList<>();
    private ArrayList<String> MyRouteNames = new ArrayList<>();
    private  int myroutePosition = 0;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ask for permission location
        /*
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.MyLayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TextView tv_name = headerView.findViewById(R.id.userName);
        TextView tv_email = headerView.findViewById(R.id.userEmail);
        ImageView iv_image = headerView.findViewById(R.id.userImage);
        tv_email.setText(user.getEmail());
        tv_name.setText(user.getDisplayName());
        Glide.with(this).load( user.getPhotoUrl()).into(iv_image);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        assert mapFragment != null;

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mapFragment.getMapAsync(googleMap -> {
            //defaultMapSettings(googleMap);
            this.googleMap = googleMap;
            LatLng current;

            //if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Check Permissions Now
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                // set marker at current location
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                current = new LatLng(latitude, longitude);
                drawCurrent(current);
            }

            // set button invisible first
            Button btn2 = (Button) findViewById(R.id.deleteButton);
            btn2.setVisibility(View.INVISIBLE);
            findViewById(R.id.deleteButton).setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Delete route " + MyRouteNames.get(myroutePosition)+" ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            });


            Button btn = (Button) findViewById(R.id.saveButton);
            btn.setVisibility(View.INVISIBLE);
            findViewById(R.id.saveButton).setOnClickListener(view -> {
                // enter the name for the route
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Route Name");
                //Set up the input
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        SaveRoute();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            });


            // update the new origin
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

        /*
        AppCompatSpinner polylineStyleSpinner = findViewById(R.id.polylineStyleSpinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ELEVATION_OPTIONS );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        polylineStyleSpinner.setAdapter(adapter);
        */
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

            if (sourceAddressTextView.getText().toString().matches("")) {
                Toast.makeText(this, "Please enter the distance!", Toast.LENGTH_SHORT).show();
                return;
            }
            int distance = Integer.valueOf(sourceAddressTextView.getText().toString());
            getLocation_1(Origin.longitude, Origin.latitude, distance * 1000);
            String origin = Origin.latitude + "," + Origin.longitude;
            ArrayList<String> destinations = new ArrayList<>();
            for (int i = 0; i < Destinations.size(); i++) {
                LatLng des = Destinations.get(i);
                String destination = des.latitude + "," + des.longitude;
                destinations.add(destination);
            }

            //String destination = Destination.latitude + "," + Destination.longitude;
            //String origin = sourceAddressTextView.getText().toString();
            //String destination = destinationAddressTextView.getText().toString();


            //fetchDistances(origin, destinations);
            mNames.clear();
            for (int i = 0; i < destinations.size(); i++) {
                mNames.add(destinations.get(i));
            }
            initRecyclerView();
            Button btn = (Button) findViewById(R.id.saveButton);
            btn.setVisibility(View.VISIBLE);
            Button btn2 = (Button) findViewById(R.id.deleteButton);
            btn2.setVisibility(View.INVISIBLE);

        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the contacts-related task you need to do.
                    googleMap.setMyLocationEnabled(true);
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    current = new LatLng(latitude, longitude);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    current = new LatLng(42.361145, -71.057083);
                }
                drawCurrent(current);
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void drawCurrent(LatLng current) {
        // draw marker
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        MarkerOptions c = new MarkerOptions();
        c.position(current);
        c.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        googleMap.addMarker(c);
        Origin = current;
    }

    // directions

    public void fetchDirections(String origin, int position1, int position2) {

        try {
            /*
            googleMap.clear();
            MarkerOptions op = new MarkerOptions();
            op.position(Origin);
            op.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.addMarker(op);

            MarkerOptions options = new MarkerOptions();
            options.position(Destinations.get(position));
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            googleMap.addMarker(options);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(Destinations.get(position)));
            */
            this.pos = position1 + 1;
            String waypoint1 = Destinations.get(position1).latitude + "," + Destinations.get(position1).longitude;
            String waypoint2 = Destinations.get(position2).latitude + "," + Destinations.get(position2).longitude;

            new DirectionFinder(this, origin, waypoint1, waypoint2).execute();
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
        if (materialDialog != null && materialDialog.isShowing())
            materialDialog.dismiss();
        if (!routes.isEmpty() && polyline != null) polyline.remove();
        try {
            for (Route route : routes) {
                distance = route.distance.text;
                int time = route.duration.value / 60;
                time = time / 2; // approximate running time
                TextView textView = findViewById(R.id.item_distance);
                TextView textView2 = findViewById(R.id.item_time);
                TextView textView3 = findViewById(R.id.routeName);
                Time = "~" + Integer.toString(time) + " min";

                // add polyline
                str_poly = route.Polyline;
                polylineOptions = getDefaultPolyLines(route.points);
                polyline = googleMap.addPolyline(polylineOptions);
                // se text
                textView.setText("Distance:  " + distance);
                textView2.setText("Duration:  " + Time);
                textView3.setText("Route " + Integer.toString(this.pos));


            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred on finding the directions...", Toast.LENGTH_SHORT).show();
        }
        LatLng coordinate = routes.get(0).startLocation; //Store these lat lng values somewhere. These should be constant.
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                coordinate, 15);
        googleMap.animateCamera(location);
        //googleMap.animateCamera(buildCameraUpdate(routes.get(0).startLocation), 10, null);
    }


    // distance finder
    public void fetchDistances(String origin, ArrayList<String> destinations) {
        try {
            new DistanceFinder(this, origin, destinations).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
        for (int i = 0; i < destinations.size(); i++) {
            mNames.add(destinations.get(i).distance.text);
        }
        // mNames.add("Unknown");
        initRecyclerView();
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, Origin, Destinations);
        recyclerView.setAdapter(adapter);
    }

    public void getLocation_1(double startLong, double startLat, double distance) {

        Destinations.clear(); // clear prev destinations
        double angle = 0;
        distance = (distance * 0.8) / 3;
        double δ = distance / r;
        // convert into radian
        startLong = Math.toRadians(startLong);
        startLat = Math.toRadians(startLat);

        for (int i = 0; i < 6; i++) {
            angle = (360 / 6.0) * i;
            angle = Math.toRadians(angle);
            double lat = Math.asin(Math.sin(startLat) * Math.cos(δ) + Math.cos(startLat) * Math.sin(δ) * Math.cos(angle));
            double lon = startLong + Math.atan2(Math.sin(angle) * Math.sin(δ) * Math.cos(startLat), Math.cos(δ) - Math.sin(startLat) * Math.sin(lat));
            // convert lon, lat into decimal
            lon = Math.toDegrees(lon);
            lat = Math.toDegrees(lat);
            LatLng d = new LatLng(lat, lon);
            Destinations.add(d);
        }
        String origin = Origin.latitude + "," + Origin.longitude;
        fetchDirections(origin, 0, 1);
        /*
        MarkerOptions options = new MarkerOptions();
        options.position(Destinations.get(0));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        googleMap.addMarker(options);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Destinations.get(0)));
        */

    }



    // save route to data base
    public void SaveRoute() {
        if (user != null) {
            //Myroute myroute = new Myroute(distance, time, polylineOptions);
            Myroute myroute = new Myroute(m_Text, distance, Time, str_poly);
            String id = user.getUid();
            mDatabase.child(id).child(m_Text).setValue(myroute);
            String str_emailgoogle = user.getEmail();
            Toast.makeText(this, "Route saved to " + str_emailgoogle, Toast.LENGTH_LONG).show();
            ReadRoute();
        } else {
            Toast.makeText(this, "Your are not logged in", Toast.LENGTH_LONG).show();
        }
    }

    public void deleteRoute() {
        if (user != null) {
            //Myroute myroute = new Myroute(distance, time, polylineOptions);
            String id = user.getUid();
            String routeName = MyRouteNames.get(myroutePosition);
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference myRef = database.child(id).child(routeName);
            myRef.removeValue();

            Toast.makeText(this, "Route deleted ", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Your are not logged in", Toast.LENGTH_LONG).show();
        }
    }


    // get route
    public void ReadRoute() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String id = user.getUid();
        DatabaseReference myRef = database.child(id);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /* This method is called once with the initial value and again whenever data at this location is updated.*/
                long value = dataSnapshot.getChildrenCount();
                Log.d(TAG, "no of children: " + value);
                //GenericTypeIndicator<List<Myroute>> genericTypeIndicator =new GenericTypeIndicator<List<Myroute>>(){};
                //List<Myroute> myRouteList=dataSnapshot.getValue(genericTypeIndicator);

                MyRouteNames.clear();
                MyRouteList.clear();
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    String name = s.getKey();
                    MyRouteNames.add(name);
                    Log.d("TAG", name);
                    String rname = "" + s.child("name").getValue();
                    String rdistance = "" + s.child("distance").getValue();
                    String rtime = "" + s.child("time").getValue();
                    String rpolyline = "" + s.child("polyline").getValue();
                    Myroute myroute = new Myroute(rname, rdistance, rtime, rpolyline);
                    MyRouteList.add(myroute);
                    //MyRouteNames.add(myroute.name);
                }

                MyRouteRecyclerView(value);
                //displayMyRoutes();


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void displayMyRoutes(int position, long value) {

        myroutePosition = position;
        TextView textView = findViewById(R.id.item_distance);
        TextView textView2 = findViewById(R.id.item_time);
        TextView textView3 = findViewById(R.id.routeName);

        if (value == 0)
        {
            textView.setText("");
            textView2.setText("");
            textView3.setText("");
            Button btn2 = (Button) findViewById(R.id.deleteButton);
            btn2.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "No saved routes", Toast.LENGTH_SHORT).show();

        }
        else
        {
            List<LatLng> decoded =  decodePolyLine(MyRouteList.get(position).polyline);
            // add polyline
            if (polyline != null) polyline.remove();
            polylineOptions = getDefaultPolyLines(decoded);
            polyline = googleMap.addPolyline(polylineOptions);
            // se text
            textView.setText("Distance:  " + MyRouteList.get(position).distance);
            textView2.setText("Duration:  " + MyRouteList.get(position).time);
            textView3.setText("" + MyRouteList.get(position).name);
        }

    }

    private void MyRouteRecyclerView(long value) {
        Log.d(TAG, "initRecyclerView: my route recyclerview");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);

        Button btn2 = (Button) findViewById(R.id.deleteButton);
        btn2.setVisibility(View.VISIBLE);
        Button btn = (Button) findViewById(R.id.saveButton);
        btn.setVisibility(View.INVISIBLE);

        SavedRouteRA adapter = new SavedRouteRA(this, MyRouteNames);
        recyclerView.setAdapter(adapter);
        displayMyRoutes(0, value);

    }

    private List<LatLng> decodePolyLine(final String poly) {
        List<LatLng> decoded = new ArrayList<>();
        try {
            int len = poly.length();
            int index = 0;
            int lat = 0;
            int lng = 0;

            while (index < len) {
                int b;
                int shift = 0;
                int result = 0;
                do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = poly.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                decoded.add(new LatLng(
                        lat / 100000d, lng / 100000d
                ));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return decoded;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    deleteRoute();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.my_routes) {

            ReadRoute();


        }
        else if (id == R.id.logout) {
            mAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // ...
                            Toast.makeText(getApplicationContext(),"Logged Out",Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(getApplicationContext(),LoginActivity.class);
                            i.putExtra("logout", true);
                            startActivity(i);
                            finish();
                        }
                    });

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }


}

