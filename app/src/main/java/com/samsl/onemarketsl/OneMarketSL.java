package com.samsl.onemarketsl;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.samsl.onemarketsl.characters.User;
import com.samsl.onemarketsl.items.Fruit;
import com.samsl.onemarketsl.items.Hardware;
import com.samsl.onemarketsl.items.Stationary;
import com.samsl.onemarketsl.items.Vegetable;
import com.samsl.onemarketsl.models.Location;
import com.samsl.onemarketsl.storages.StoreDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import config.android.Connector;


public class OneMarketSL extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowLongClickListener {
    GoogleMap map;
    LocationManager locationManager;
    Location networkLocation;
    Location bestLocation;
    Location GPSLocation = new Location();
    private static final int INITIAL_REQUEST=1337;
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    private String keyWord;


    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        Location currentLocation = new Location();
        if(GPSLocation.getLatitude()!=null){
            currentLocation = GPSLocation;
        }else{
            currentLocation.setLatitude(6.9664325);
            currentLocation.setLongitude(79.921921);
        }
        map.setOnInfoWindowLongClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ----------- Registering default app behavior ------------------ //
        setContentView(R.layout.activity_one_market_sl);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // --------------------------------------------------------------- //
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        registerActionListeners();
        registerGoogleMapListeners();
        connectWithNetwork();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_one_market_sl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean registerGoogleMapListeners(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return true;
    }

    private boolean connectWithNetwork(){
        if(Connector.Connect()==null){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                Connector.Connect("http://18.219.200.74:8080/", "test@test.com", "123456789");
                return true;
            }catch (Exception e){
                final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("OneMarketSL Error")
                        .setMessage("System can't login to OneMarketSL Network.\nPlease check your internet connection")
                        .setPositiveButton("Network Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                Intent myIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(myIntent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            }
                        });
                dialog.show();
                return false;
            }
        }return true;
    }

    private String getFruitString(StoreDto storeInfo){
        StringBuilder sb = new StringBuilder();
        for(Fruit fruit: storeInfo.getFruits()){
            sb.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;"+fruit.getFruitName()+" : "+fruit.getQuantity());
        }
        return sb.toString();
    }

    private String getVegetableString(StoreDto storeInfo){
        StringBuilder sb = new StringBuilder();
        for(Vegetable vegetable: storeInfo.getVegetables()){
            sb.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;"+vegetable.getVegetableName()+" : "+vegetable.getQuantity());
        }
        return sb.toString();
    }

    private String getHardwareString(StoreDto storeInfo){
        StringBuilder sb = new StringBuilder();
        for(Hardware hardware: storeInfo.getHardware()){
            sb.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;"+hardware.getHardwareName()+" : "+hardware.getQuantity());
        }
        return sb.toString();
    }

    private String getStationaryString(StoreDto storeInfo){
        StringBuilder sb = new StringBuilder();
        for(Stationary stationary: storeInfo.getStationary()){
            sb.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;"+stationary.getStationaryName()+" : "+stationary.getQuantity());
        }
        return sb.toString();
    }

    private boolean registerActionListeners(){
        try{
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Please click and hold on store name to view details", Snackbar.LENGTH_LONG)
                            .setAction("OK", view1 -> {

                            }).show();
                }
            });

            Button button = (Button) findViewById(R.id.button);
            button.getBackground().setColorFilter(Color.parseColor("#359c5e"), PorterDuff.Mode.MULTIPLY);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    search(view);
                }
            });

            EditText editText = (EditText) findViewById(R.id.searchText);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    boolean handled = false;
                    if(event!=null){
                        if (event.getAction() == KeyEvent.KEYCODE_ENTER) {
                            search(v);
                            handled = true;
                        }
                    }else if (actionId == EditorInfo.IME_ACTION_DONE){
                        search(v);
                        handled = true;
                    }
                    return handled;
                }
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean search(View view) {
        try{
            if(connectWithNetwork()){
                map.clear();
                Location currentLocation = new Location();
                if(GPSLocation.getLatitude()!=null){
                    currentLocation = GPSLocation;
                }else{
                    currentLocation.setLatitude(6.9664325);
                    currentLocation.setLongitude(79.921921);
                }
                EditText editText = (EditText) findViewById(R.id.searchText);
                keyWord = editText.getText().toString();
                List shops = Connector.Connect().search(keyWord,currentLocation);
                List<User> users = new ArrayList(shops.size());
                for (int i = 0; i < shops.size(); i++) {
                    User user = new User();
                    LinkedHashMap temp = (LinkedHashMap) shops.get(i);
                    user.setName(temp.get("name").toString());
                    LinkedHashMap tempLocation = (LinkedHashMap) temp.get("location");
                    Location location = new Location();
                    location.setLatitude((Double) tempLocation.get("latitude"));
                    location.setLongitude((Double) tempLocation.get("longitude"));
                    user.setLocation(location);
                    user.setAddress((String)temp.get("address"));
                    user.setEmail((String)temp.get("email"));
                    user.setUserID((Integer) temp.get("userID"));
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude()))
                            .title(user.getName())
                            .snippet("Address: "+user.getAddress()+"\nContact Info: "+user.getEmail())
                    ).setTag(user.getUserID());
                }
                editText.setText("");
                return true;
            }
        }catch (Exception e){
//            showMessage("System Error","Error with user input.\nPlease check your keywords and Try Again.","OK");
        }
        return false;
    }

    private void showMessage(String title, Spanned message, String button){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }
    public void toggleGPSUpdates(View view) {
        if (!checkLocation())
            return;
        Button button = (Button) view;
        if (button.getText().equals(getResources().getString(R.string.pause))) {
            locationManager.removeUpdates(locationListenerGPS);
            button.setText(R.string.resume);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2 * 60 * 1000, 10, locationListenerGPS);
            button.setText(R.string.pause);
        }
    }
    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }
    public void selectBestLocationProvider() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                return;
            }
            locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, locationListenerBest);
        }
    }
    private final LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            networkLocation.setLongitude(location.getLongitude());
            networkLocation.setLatitude(location.getLatitude());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };

    private final LocationListener locationListenerGPS = new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            GPSLocation.setLongitude(location.getLongitude());
            GPSLocation.setLatitude(location.getLatitude());
            // Getting latitude of the current location
            double latitude = location.getLatitude();

            // Getting longitude of the current location
            double longitude = location.getLongitude();

            float speed = location.getSpeed();

            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Showing the current location in Google Map
            CameraPosition camPos = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(12)
                    .bearing(location.getBearing())
                    .tilt(0)
                    .build();
            CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
            map.animateCamera(camUpdate);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private final LocationListener locationListenerBest = new LocationListener() {

        @Override
        public void onLocationChanged(android.location.Location location) {
            bestLocation.setLongitude(location.getLongitude());
            bestLocation.setLatitude(location.getLatitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        try{
            StoreDto storeInfo = Connector.Connect().getItemDetails((Integer) marker.getTag(),keyWord);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                showMessage("Store Details", Html.fromHtml("<b>"+"Fruits: "+storeInfo.getFruits().size()+"</b>"+getFruitString(storeInfo)+
                                "<br><b>Vegetables: "+storeInfo.getVegetables().size()+"</b>"+ getVegetableString(storeInfo)+
                                "<br><b>Hardware: "+storeInfo.getHardware().size()+"</b>"+getHardwareString(storeInfo)+
                                "<br><b>Stationary: "+storeInfo.getStationary().size()+"</b>"+getStationaryString(storeInfo), Html.FROM_HTML_MODE_LEGACY)
                        ,"Close");
            } else {
                showMessage("Store Details", Html.fromHtml("<b>"+"Fruits: "+storeInfo.getFruits().size()+"</b>"+getFruitString(storeInfo)+
                                "<br><b>Vegetables: "+storeInfo.getVegetables().size()+"</b>"+ getVegetableString(storeInfo)+
                                "<br><b>Hardware: "+storeInfo.getHardware().size()+"</b>"+getHardwareString(storeInfo)+
                                "<br><b>Stationary: "+storeInfo.getStationary().size()+"</b>"+getStationaryString(storeInfo))
                        ,"Close");
            }

        }catch (Exception e){

        }
    }
}
