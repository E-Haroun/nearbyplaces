package com.example.places;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Nearby extends AppCompatActivity {

    //Initialization Variables
    Spinner sptype;
    Button btn;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0 , currentLong = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        // Assign variables

        sptype = findViewById(R.id.spinner);
        btn = findViewById(R.id.button);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Initialization array of places
        String[] placeTypeList = {"atm", "bank", "hospital", "movie_theater", "restaurant"};
        // initialize array of places name
        String[] placeNameList = {"ATM", "BANK", "HOSPITAL", "MOVIE THEATER", "RESTAURANT"};

        // Set adapter on spinner
        sptype.setAdapter(new ArrayAdapter<>(Nearby.this, android.R.layout.simple_spinner_dropdown_item, placeNameList));

        // Initialize fused location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // check permission
        if (ActivityCompat.checkSelfPermission(Nearby.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // call methods
            getCurrentLocation();
        }else{
            // when permission denied
            //request permission
            ActivityCompat.requestPermissions(Nearby.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get selected item of spinner
                int i = sptype.getSelectedItemPosition();
                // initilize url
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLat + "," + currentLong + // location
                        "&radius=5000" + // nearby
                        "&type=" + placeTypeList[i] + // place
                        "&sensor=true" + // sensor
                        "&key=" + getResources().getString(R.string.google_map_key); // apiKey

                // Execute place task method to download json data
                new PlaceTrack().execute(url);


            }
        });
    }

    private void getCurrentLocation() {
        // Initialize task Location
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
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>(){
            @Override
            public void onSuccess(Location location){
                // When success
                if (location != null){
                    //when location is not equal to null
                    // get current Latitude
                    currentLat = location.getLatitude();
                    // get current Langitude
                    currentLong = location.getLongitude();
                    //Sync to map
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            // when map is ready
                            map = googleMap;
                            //zoom current location on map
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat,currentLong),10));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ( requestCode == 44){
            if ( grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // when permission grant
                // call
                getCurrentLocation();
            }
        }
    }

    private class PlaceTrack extends AsyncTask<String,Integer,String> {


        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                // Initialize data
                 data =  downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            // Execute parser task
            new ParserTask().execute(s);
        }
    }
    private  String downloadUrl(String string) throws IOException {
         //Initialize url
        URL url = new URL ( string);
        //Initialize connection
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        //Connect
        connection.connect();
        //initialize input stream
        InputStream stream = connection.getInputStream();
        //Initialize buffer reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //Initialize String builder
        StringBuilder builder = new StringBuilder();
        //Initialize string variable
        String line = "";
        // Loop for all
         while ((line = reader.readLine())!= null){
            // Append line
             builder.append(line);
        }
         //get append data
        String data = builder.toString();
         //clode reader
         reader.close();
         // return data
        return data;

    }

    private class ParserTask extends AsyncTask<String,Integer, List<HashMap<String,String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //Xreate json parser clasx
            JsonParser jsonParser = new JsonParser();
            // initialize hash map
            List<HashMap<String,String>> maplist = null;
            JSONObject object = null;
            try {
                // Initialize json object
                 object = new JSONObject(strings[0]);
                 //Parse json obj
                maplist = jsonParser.parseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // return map list
            return maplist;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //Clear map
            map.clear();

            for ( int i = 0; i< hashMaps.size();i++){
                //Initalize hash map
                HashMap<String, String> hashMapList = hashMaps.get(i);
                //get Latitude
                double lat = Double.parseDouble(hashMapList.get("lat"));
                //get longitude
                double lng = Double.parseDouble(hashMapList.get("lng"));
                //get name
                String name = hashMapList.get("name");
                //Concat latitude and longitude
                LatLng latLng = new LatLng(lat,lng);
                //initialize markers opt°
                MarkerOptions options = new MarkerOptions();
                //set position
                options.position(latLng);
                //tittle
                options.title(name);
                //add marker
                map.addMarker(options);
            }
        }
    }
}