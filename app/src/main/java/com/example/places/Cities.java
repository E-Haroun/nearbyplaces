package com.example.places;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Cities extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    //google map object
    private GoogleMap mMap;

    //current and destination location objects
    Location myLocation = null;
    Location destinationLocation = null;
    protected LatLng start = null;
    protected LatLng end = null;

    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;
    boolean locationPermission = false;

    //polyline object to draw the routes
    private List<Polyline> polylines = null;

    //destination City name
    private String cityName="";

   /* Map<String,String> mmmap= new HashMap<String,String>(){{
        put("key1", "value1");
        put("key2", "value2");
    }};*/

    //hashmap containing names and lnglnt
    Map<String, Pair<Double,Double>> cityMap= new HashMap<String,Pair<Double,Double>>(){{
        put("Tanger", new Pair(35.76, -5.83));
        put("Oujda", new Pair(34.74, -1.89));
        put("Rabat", new Pair(33.97, -6.85));
        put("Casablanca", new Pair(33.56, -7.58));
        put("Agadir", new Pair(30.42, -9.59));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);

        //request location permission.
        requestPermision();

        //init google map fragment to show map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void requestPermision() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission granted.
                    locationPermission = true;
                    getMyLocation();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    //to get user location
    private void getMyLocation() {
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
        mMap.setMyLocationEnabled(true); //to point to current location
        //if we change location, the location on the map updates automatically
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                myLocation=location;
                LatLng ltlng=new LatLng(location.getLatitude(),location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        ltlng, 16f);
                mMap.animateCamera(cameraUpdate);
            }
        });

        //get destination location when user click on map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                end=latLng;
                mMap.clear();
                start=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                //start route finding
                Findroutes(start,end);
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getMyLocation();

    }

    public void tangerRoute(View view){
        cityName="Tanger";
        cityRoute(view);
    }

    public void oujdaRoute(View view){
        cityName="Oujda";
        cityRoute(view);
    }

    public void rabatRoute(View view){
        cityName="Rabat";
        cityRoute(view);
    }

    public void casablancaRoute(View view){
        cityName="Casablanca";
        cityRoute(view);
    }

    public void agadirRoute(View view){
        cityName="Agadir";
        cityRoute(view);
    }

    public void cityRoute(View view){
        Pair latlngCityPair= cityMap.get(cityName);
        Double lat= (Double) latlngCityPair.first;
        Double lng= (Double) latlngCityPair.second;
        LatLng city=new LatLng(lat,lng);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(city));
        mMap.clear();

        end=city;
        start=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
        //start route finding
        Findroutes(start,end);
        //switch case depending on city to define icon
        switch (cityName){

            case "Tanger":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(cityName)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tanger_hercule)));
                break;

            case "Casablanca":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(cityName)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.casa)));
                break;

            case "Rabat":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(cityName)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.rab)));
                break;

            case "Agadir":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(cityName)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.agadir)));
                break;

            case "Oujda":
                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(cityName)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ouj)));
                break;

            default:
                break;
        }

    }


    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End)
    {
        if(Start==null || End==null) {
            Toast.makeText(Cities.this,"Unable to get location",Toast.LENGTH_LONG).show();
        }
        else
        {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyBduMVB0WBcPisEmptCSIqhJ7L1sAxH4Z0")  //also define your api key here.
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
        Toast.makeText(Cities.this,"Finding Route...",Toast.LENGTH_LONG).show();
    }

    //If Route finding success..
    @Override
    public void onRoutingSuccess(ArrayList<com.directions.route.Route> route, int shortestRouteIndex) {

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
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
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


        //iterating over the hashmap of cities to make sure if we are on one of the already
        //defined ones of not, if so we don't have to add an icon or title
        int p=0;
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.##",otherSymbols);

        Double lat = null;
        Double lng=null;

        for (Map.Entry<String,Pair<Double,Double>> set : cityMap.entrySet()) {
            String cityValue=set.getKey();
            Pair latlngCityPair= cityMap.get(cityValue);


            lat= (Double) latlngCityPair.first;
            lng= (Double) latlngCityPair.second;

            //Toast.makeText(this, String.valueOf(df.format((Double)polylineEndLatLng.latitude)), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, String.valueOf(lat), Toast.LENGTH_SHORT).show();

            if( String.valueOf(df.format((Double)polylineEndLatLng.latitude)).equals(String.valueOf(lat)) &&
                    String.valueOf(df.format((Double)polylineEndLatLng.longitude)).equals(String.valueOf(lng)) ) {
                p=1;
            }
        }

        if(p==0){
            endMarker.title("Destination");
            mMap.addMarker(endMarker);
        }else{

        }
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(start,end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { Findroutes(start,end); }
}