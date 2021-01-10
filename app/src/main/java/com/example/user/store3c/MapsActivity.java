package com.example.user.store3c;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private String menu_item, mall_name;
    private Float latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Button mapRtn;
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int imgHeight;

        //Toolbar toolbar = findViewById(R.id.toolbarMap);
        //getActionBar().show();
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setLogo(R.drawable.store1);

        Intent intentItem = getIntent();
        Bundle bundleItem = intentItem.getExtras();

        if (bundleItem != null) {
            menu_item = bundleItem.getString("Menu");
            latitude = bundleItem.getFloat("Latitude");
            longitude = bundleItem.getFloat("Longitude");
            mall_name = bundleItem.getString("Mall");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_id);
        //mapFragment.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_map, null);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        mapRtn = findViewById(R.id.mapRtn_id);

        if (screenWidth > 800) {
            imgHeight = 500;
            if (mapFragment != null && mapFragment.getView() != null) {
                mapFragment.getView().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            }
        }

        mapRtn.setOnClickListener(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Add a marker in Sydney and move the camera
        //LatLng mall = new LatLng(25.053603, 121.288672);
        LatLng mall = new LatLng(latitude, longitude);

        //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location_icon);

        //MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(mall).title("台茂購物中心").snippet("shopping mall").icon(icon);
        //mMap.addMarker(markerOptions);
        googleMap.addMarker(new MarkerOptions().position(mall).title(mall_name).snippet("shopping mall"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(mall));
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mall).zoom(17).build();

        // 使用動畫的效果移動地圖
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mapRtn_id) {
            Intent intentItem = new Intent();
            Bundle bundleItem = new Bundle();
            bundleItem.putString("Menu", menu_item);
            intentItem.putExtras(bundleItem);
            intentItem.setClass(MapsActivity.this, PositionActivity.class);
            startActivity(intentItem);
            MapsActivity.this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent =  new Intent();
        Bundle bundleItem = new Bundle();
        bundleItem.putString("Menu", menu_item);
        intent.putExtras(bundleItem);
        intent.setClass(MapsActivity.this, PositionActivity.class);
        startActivity(intent);
        MapsActivity.this.finish();

    }

}
