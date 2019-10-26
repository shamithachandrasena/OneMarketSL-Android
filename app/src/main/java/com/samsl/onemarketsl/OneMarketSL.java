package com.samsl.onemarketsl;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.samsl.onemarketsl.characters.User;
import com.samsl.onemarketsl.models.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import config.android.Connector;


public class OneMarketSL extends AppCompatActivity implements OnMapReadyCallback{
    GoogleMap map;

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_market_sl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
           addOne(view);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    public boolean addOne(View view) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connector.Connect("http://18.219.200.74:8080/", "test@test.com", "123456789");
        Location currentLocation = new Location();
        currentLocation.setLatitude(6.9664325);
        currentLocation.setLongitude(79.921921);
        EditText editText = (EditText) findViewById(R.id.searchText);
        List shops = Connector.Connect().search(editText.getText().toString(), currentLocation);
        List<User> users = new ArrayList(shops.size());
        for (int i=0; i<shops.size(); i++) {
           User user = new User();
           LinkedHashMap temp = (LinkedHashMap) shops.get(i);
           user.setName(temp.get("name").toString());
           LinkedHashMap tempLocation = (LinkedHashMap) temp.get("location");
           Location location =  new Location();
           location.setLatitude((Double)tempLocation.get("latitude"));
           location.setLongitude((Double)tempLocation.get("longitude"));
           user.setLocation(location);
           map.addMarker(new MarkerOptions()
                    .position(new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude()))
                    .title(user.getName()));
        }
        editText.setText("");
        return true;
    }
}
