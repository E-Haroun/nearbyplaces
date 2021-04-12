package com.example.places;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.gms.location.LocationListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,RoutingListener {

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Map is ready here !");
        mMap = googleMap;

        if (mLocationLocationPermissionsGranted) {
            getDeviceLocattion();
            //add check permission and add icon return to current position
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            // search bar vs icon => search bar win
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            //
            init();
        }
    }

    // logt
    private static final String TAG = "MapActivity";

    //
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 16f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, 168), new LatLng(71, 136));
    private static final int RC_SIGN_IN = 9001;

    // Widgets
    private AutoCompleteTextView mSearchtext;
    private ImageView mGps;
    // var
    private Boolean mLocationLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapterNew mPlaceAutocompleteAdapter;

    protected LatLng start = null;
    protected LatLng end = null;
    //polyline object
    private List<Polyline> polylines = null;
    // Declaring a Location Manager
    protected LocationManager locationManager;
    String provider;
    Location myLocation = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchtext = findViewById(R.id.input_search);
        // check location perm
        getLocationPermission();

        // to hide keyboard
        EditText edtView = (EditText) findViewById(R.id.input_search);
        edtView.setInputType(InputType.TYPE_NULL);

        //
        mGps = findViewById(R.id.ic_gps);
        //


    }


    private void init() {
        Log.d(TAG, "Initializing");
       /*
        String apiKey = "AIzaSyCz4dKLSf9o7KqHku4lseGlnZCdQFL9rsY";
        Places.initialize(getApplicationContext(), apiKey);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(com.google.android.gms.location.places.Places.GEO_DATA_API)
                .addApi(com.google.android.gms.location.places.Places.PLACE_DETECTION_API)
                .enableAutoManage(this,this)
                .build();
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
        mSearchtext.setAdapter(mPlaceAutocompleteAdapter);
        */

        String apiKey = "AIzaSyCz4dKLSf9o7KqHku4lseGlnZCdQFL9rsY";
        AutocompleteSessionToken autocompleteSessionToken;
        autocompleteSessionToken = AutocompleteSessionToken.newInstance();

        // Initialize Places.
        Places.initialize(getApplicationContext(), apiKey);
        PlaceAutocompleteAdapterNew mAdapter;
        PlacesClient placesClient = Places.createClient(this);
        // Create a new Places client instance.

        mAdapter = new PlaceAutocompleteAdapterNew(this, placesClient, autocompleteSessionToken);
        mSearchtext.setAdapter(mAdapter);
        ///
        mSearchtext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == event.ACTION_DOWN
                        || event.getAction() == event.KEYCODE_ENTER) {
                    // execute method searching
                    geoLocate();

                }
                return false;
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnClick clicked GPS");
                getDeviceLocattion();
            }
        });

        hideSoftkeyboard();

    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate : geoLocating !");
        String searchstring = mSearchtext.getText().toString();
        Geocoder geocoder = new Geocoder(com.example.places.MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchstring, 1);
        } catch (IOException e) {
            Log.d(TAG, "geoLocate: IOException " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: Found location !" + address.toString());
            //Toast.makeText(this,address.toString(),Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            end = new LatLng(address.getLatitude(), address.getLongitude());
            Log.d(TAG,"ddddddddddd > "+address.getLongitude());
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            start=new LatLng(latitude,longitude);
            //start route finding
            Findroutes(start,end);

        }
    }

    // Get Devices Location
    private void getDeviceLocattion() {
        Log.d(TAG, "getDeviceLocattion :  getting the current devices location !");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Task location = null;
        try {
            if (mLocationLocationPermissionsGranted) {
                 location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@Nullable Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found Location !");
                            Location currentLocation = (Location) task.getResult();
                            //call for move camera
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");

                        } else {
                            Log.d(TAG, "onComplete: Current location is null !");
                            Toast.makeText(com.example.places.MapActivity.this, "unable to find location ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocattion : SecurityException " + e.getMessage());

        }

    }

    // Move Camera and focus on location
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to !" + latLng.latitude + ",lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //add marker when geolocated
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(options);

        // hide marker on my location
        /*
        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        */
        hideSoftkeyboard();


    }
    // initialze map
    private void initMap(){
        Log.d(TAG,"Initialzing Map!");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // callback for the map
        //first methode
        /*
        mapFragment.getMapAsync(new OnMapReadyCallback() {
             @Override
             public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
             }
         });
         */
        //second Methode
        mapFragment.getMapAsync(com.example.places.MapActivity.this);


    }
    //check the permission
    private void getLocationPermission(){
        Log.d(TAG,"Getting Location Permissions !");

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                //
                mLocationLocationPermissionsGranted = true;
                // without asking for permission again
                //initMap();
            }else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG,"OnRequestPermissions : called !");

        mLocationLocationPermissionsGranted = false;
        //check req_code
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                // permission granted and getArray of results
                if(grantResults.length >0 ){
                    // it can be more results loop for them all
                    for (int i=0 ; i< grantResults.length;i++){
                        if( grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG,"OnRequestPermissionsResult : permission failed!");

                            mLocationLocationPermissionsGranted = false;
                           return;
                        }
                        Log.d(TAG,"OnRequestPermissionsResult : permission granted!");

                        mLocationLocationPermissionsGranted = true;
                        // initialaze Map
                        initMap();
                    }
                }
            }
        }
    }

    // to hide keyboard
private void hideSoftkeyboard(){
        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Hide:
        //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        //Show
        //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /*
    ---------------- google places autocomplete suggestion
    */
    /*
    private AdapterView.OnItemSelectedListener mAuOnItemSelectedListener = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
            final AutocompletePrediction item = mAdapter.getItem(i);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(placesClient,placeId);
        }
    };*/

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(this,"Unable to get location",Toast.LENGTH_LONG).show();
        }
        else
        {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener((RoutingListener) MapActivity.this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyCz4dKLSf9o7KqHku4lseGlnZCdQFL9rsY")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }



    //Routing call back functions.
    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar= Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
//        Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {
        Toast.makeText(this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if(polylines!=null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng=null;
        LatLng polylineEndLatLng=null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i <route.size(); i++) {

            if(i==shortestRouteIndex)
            {
                polyOptions.color(MapActivity.this.getResources().getColor(R.color.design_default_color_primary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng=polyline.getPoints().get(0);
                int k=polyline.getPoints().size();
                polylineEndLatLng=polyline.getPoints().get(k-1);
                polylines.add(polyline);

            }
            else {

            }

        }

        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(start,end);
    }




}
