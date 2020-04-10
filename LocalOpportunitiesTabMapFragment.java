package com.example.ikoala.ui.social;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.ikoala.R;
import com.example.ikoala.logger.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LocalOpportunitiesTabMapFragment extends Fragment implements OnMapReadyCallback {

    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private ArrayList<LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput> mapResultsList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/";
    private static final String API_KEY = "AIzaSyBBSnAZOqmavii0imK03cioNooWPZT1KZk";
    private static final String TYPE_SEARCH = "nearbysearch";
    private static final String OUT_JSON = "/json?";
    private static String searchType = ""; //request changing for each desired search type

    //Map location variables (includes places api)
    Integer responseCode = null;
    String responseMessage = "";
    private GoogleMap mMap;
    private PlacesClient mPlacesClient;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MAP_SERACH_RADIUS = 1000;
    private boolean mLocationPermissionGranted;
    private Location lastKnownLocation;
    private TableLayout mTableLayout;
    private Pair<Double, TableRow> itemPair;
    private ArrayList<Pair<Double, TableRow>> itemList;

    private FragmentActivity myContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle passedBundle = getArguments();
        if(passedBundle != null){
            if(passedBundle.containsKey("mapSearchTerm")){
                searchType = passedBundle.getString("mapSearchTerm");
            }
        }

        View view = inflater.inflate(R.layout.fragment_tab_localopportunities_map,container,false);
        mTableLayout = view.findViewById(R.id.mapListTable);
        mTableLayout.setStretchAllColumns(true);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(myContext);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(LocalOpportunitiesTabMapFragment.this);
        Places.initialize(myContext.getApplicationContext(), API_KEY);
        mPlacesClient = Places.createClient(myContext);

    }

    @Override
    public void onAttach(Context context) {
        myContext = (FragmentActivity) context;
        itemList = new ArrayList<>();
        super.onAttach(context);
    }
    /**
     * Google maps api code below
     */

    //This method is automatically called when the map is setUp
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
//                // Inflate the layouts for the info window, title and snippet.
//                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
//                        (FrameLayout) findViewById(R.id.map), false);
//
//                TextView title = infoWindow.findViewById(R.id.title);
//                title.setText(marker.getTitle());
//
//                TextView snippet = infoWindow.findViewById(R.id.snippet);
//                snippet.setText(marker.getSnippet());

//                return infoWindow;
                return null;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();
        updateLocationUI();

        // Get the current location of the device and set the position of the map
        // Also make call to places api to get location data
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(myContext, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();

                            //saving the values of the last known location to a global variable
                            lastKnownLocation = mLastKnownLocation;

                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                StringBuilder sb;

                                //Forum the url to send for local opportunities data
                                sb = new StringBuilder(PLACES_API_BASE);
                                sb.append(TYPE_SEARCH);
                                sb.append(OUT_JSON);
                                sb.append("location=" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                                sb.append("&radius=" + MAP_SERACH_RADIUS);
                                sb.append("&type=" + searchType);
                                sb.append("&key=" + API_KEY);

                                URL mapUrl = null;
                                try {
                                    mapUrl = new URL(sb.toString());
                                } catch (MalformedURLException e) {
                                    Log.e("localOpportunities", "ERROR converting String to URL " + e.toString());
                                    return;
                                }

                                //Execute the async call to the api endpoint to retrieve location data
                                RetrievePlaces mapsTask = new RetrievePlaces();
                                mapsTask.execute(mapUrl);
                            }
                        }

                    }
                });
            }
        } catch (SecurityException e) {
            android.util.Log.e("Exception: %s", e.getMessage());
        }
    }

    //Async class used to get Map location data
    class RetrievePlaces extends AsyncTask<URL, String, ArrayList<LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput> > {

        protected ArrayList<LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput> doInBackground(URL... urls) {
            StringBuilder jsonResults = new StringBuilder();
            HttpURLConnection conn = null;

            try {
                URL url = urls[0];
                conn = (HttpURLConnection) url.openConnection();
                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            }catch (IOException e) {
                android.util.Log.e("localOpportunities", "Http connection ERROR " + e.toString());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try{
                if(responseCode != null && responseCode == 200){

                    //Process the returned JSON object
                    JSONObject jsonObject = new JSONObject(jsonResults.toString());
                    JSONArray resultsJsonArray = jsonObject.getJSONArray("results");
                    LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput filteredOutput;

                    for(int i = 0; i < resultsJsonArray.length(); i++){
                        Gson g = new Gson();
                        filteredOutput = g.fromJson(resultsJsonArray.getJSONObject(i).toString(), LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput.class);
                        mapResultsList.add(filteredOutput);
                    }
                    return mapResultsList;

                } else{
                    String errorMsg = "Http ERROR response " + responseMessage + "\n" + "Are you online ? " + "\n";
                    android.util.Log.e("localOpportunities", errorMsg);
                    return  null;
                }
            }catch (JSONException e) {
                android.util.Log.e("localOpportunities", "Http Response ERROR " + e.toString());
            }
            return null;
        }

        protected void onPostExecute(ArrayList<LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput> places) {

            if(places != null && places.size() != 0){
                places.forEach(item -> {
                    LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput.LocationGeometry geometry = item.getGeometry();
                    LatLng latLng = new LatLng(geometry.location.lat, geometry.location.lng);

                    //Adding the location to a list of locations
                    String name = item.getName();
                    double distance = CalculateDistanceToPoint(
                            lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude(),
                            latLng.latitude,
                            latLng.longitude);
                    distance = Math.round(distance * 100) / 100.0; //rounding to two dp
                    String type = searchType;

                    final TextView facilityName = new TextView(myContext);
                    facilityName.setMaxWidth(20);
                    facilityName.setGravity(Gravity.CENTER);
                    facilityName.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    facilityName.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT));
                    facilityName.setText(name);

                    final TextView facilityDistance = new TextView(myContext);
                    facilityDistance.setMaxWidth(10);
                    facilityDistance.setGravity(Gravity.CENTER);
                    facilityDistance.setBackgroundColor(Color.parseColor("#f7f7f7"));
                    facilityDistance.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT));
                    facilityDistance.setText(Double.toString(distance));

                    final TextView facilityType = new TextView(myContext);
                    facilityType.setGravity(Gravity.CENTER);
                    facilityType.setLayoutParams(new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.MATCH_PARENT));
                    facilityType.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    facilityType.setText(type);

                    final TableRow tr = new TableRow(myContext);
                    tr.addView(facilityName);
                    tr.addView(facilityDistance);
                    tr.addView(facilityType);

                    itemList.add(new Pair<>(distance, tr));

                    //Adding the location as a marker to Map
                    mMap.addMarker(new MarkerOptions()
                            .title(item.getName())
                            .position(latLng)
                            .snippet(item.getVicinity()));
                });
            }

            //Once everything is done, add rows to table
            itemList.sort(new Comparator<Pair<Double, TableRow>>()
            {
                @Override
                public int compare(Pair<Double, TableRow> o1, Pair<Double, TableRow>  o2)
                {
                    if(o1.first < o2.first){
                        return -1;
                    } else if(o1.first.equals(o2.first)){
                        return 0;
                    }else {
                        return 1;
                    }
                }
            });

            if(mTableLayout.getChildCount() == 1)
            {
                itemList.forEach(item -> {

                    mTableLayout.addView(item.second);

                    //separator row
                    final TableRow trSep = new TableRow(myContext);
                    TextView tvSep = new TextView(myContext);
                    TableRow.LayoutParams tvSepLay = new
                            TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    tvSepLay.span = 3;
                    tvSep.setLayoutParams(tvSepLay);
                    tvSep.setBackgroundColor(Color.parseColor("#000000"));
                    tvSep.setHeight(2);
                    trSep.addView(tvSep);

                    mTableLayout.addView(trSep);
                });
            }
        }

    }

    @Override
    public void onPause(){
        super.onPause();

        //or try view
//        mTableLayout.removeAllViews();
    }

    //Requesting location permission from mobile device
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.myContext.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(myContext,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //Show user as blue dot on map
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            android.util.Log.e("Exception: %s", e.getMessage());
        }
    }

    //Save state of map when activity is paused
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    //Callback of locationPermission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    //Classes used for formatting output of MapSearch
    public static class MapSearchFormattedOutput {
        private String name;
        private String vicinity;
        private LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput.LocationGeometry geometry;

        public String getName() {return name;}
        public String getVicinity() {return vicinity;}
        public LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput.LocationGeometry getGeometry() {return geometry;}

        static class LocationGeometry{

            private LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput.LocationGeometry.LocationObject location;

            static class LocationObject{
                double lat;
                double lng;
            }
            public LocalOpportunitiesTabMapFragment.MapSearchFormattedOutput.LocationGeometry.LocationObject getLocation(){ return location;}
        }
    }

    /**
     * Calculate the distance between two points using longitude and latitude
     * @return the distance in miles as a double value
     * Adaptation of the open source code available here: https://www.geodatasource.com/developers/java
     */
    public static double CalculateDistanceToPoint(double lat1, double long1, double lat2, double long2){

        if((lat1 == lat2) && (long1 == long2)){
            return 0.0;
        }
        else{
            double theta = long1 - long2;
            double distance = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            distance = Math.acos(distance);
            distance = Math.toDegrees(distance);
            distance = distance * 60 * 1.1515; //distance is in miles
            return distance;
        }
    }
}
