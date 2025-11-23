package com.driverspa.admin.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.L;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class AddressMapActivity extends AdminBaseActivity implements OnMapReadyCallback {
	
	public static final String LOC_LAT = "LOC_LAT";
	public static final String LOC_LON = "LOC_LON";
	public static FragmentManager fragmentManager;
    private GoogleMap mMap;
    private LatLng latLng;
    public static final double LAT = 43.240008;
    public static final double LON = 76.912231;
    Washer washer;
    @BindView(R.id.streetName)
    EditText street;
    String city = UserPreferences.getCity(BA.getContext());

    @Override
	protected void onCreate(Bundle arg0) {		
		super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_address_map);
        ButterKnife.bind(this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Адрес и местоположение");
        mToolbar.findViewById(R.id.action_done).setVisibility(View.VISIBLE);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        street.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(washer != null)
                   washer.setAddress(s.toString());
            }
        });


    }

	/***** Sets up the map if it is possible to do so *****/
	public void setUpMapIfNeeded() {
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	        // Try to obtain the map from the SupportMapFragment.
	        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapThis)).getMapAsync(this);
	        // Check if we were successful in obtaining the map.
	    }
	}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null)
            setUpMap();
    }

    private void setUpMap() {
        UiSettings ui = mMap.getUiSettings();
        ui.setCompassEnabled(false);
        ui.setZoomControlsEnabled(true);
        ui.setMyLocationButtonEnabled(true);
        ui.setAllGesturesEnabled(true);
        mMap.setMyLocationEnabled(true);
//       final LatLng myLL = new LatLng(55.754684, 37.623164);
//       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLL, ZOOM_LEVEL));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                AddressMapActivity.this.latLng = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_partner))));
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

        MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_partner)));
        LatLng cityDefaultLatLng = Functions.getCityLatLng(city);
        if(!(cityDefaultLatLng.latitude == latLng.latitude && cityDefaultLatLng.longitude == latLng.longitude))
            mMap.addMarker(marker);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        getSupportMenuInflater().inflate(R.menu.menu, menu);              
        return true;        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	saveData();
                break;          
        }
        return true;
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
       if(washer != null) {
           String addressStr = washer.getAddress();//getIntent().getStringExtra(STREET);
           street.setText(addressStr);

//            getIntent().getDoubleExtra(LOC_LAT, LAT);
//            getIntent().getDoubleExtra(LOC_LON, LON);
           double lat  = washer.getLonLat().get(1);   //getIntent().getDoubleExtra(AddressMapActivity.LOC_LAT, AddressMapActivity.LAT);
           double lon = washer.getLonLat().get(0);//getIntent().getDoubleExtra(AddressMapActivity.LOC_LON, AddressMapActivity.LON);

           latLng = new LatLng(lat, lon);
           setUpMapIfNeeded();
       }
    }

        @Override
    public void onBackPressed() {
    	saveData();
    }
    
    private void saveData(){
        String addressStr = street.getText().toString();
        if(TextUtils.isEmpty(addressStr)){
            street.setError("Обязательное поле");
            ToastUtil.display(this,"Введите адрес");
            return;
        }

        LatLng cityDefaultLatLng = Functions.getCityLatLng(city);
        if((cityDefaultLatLng.latitude == latLng.latitude && cityDefaultLatLng.longitude == latLng.longitude)) {
            ToastUtil.display(this, "Выберите на карте местоположение мойки");
            L.d(cityDefaultLatLng.latitude+" lat "+ latLng.latitude);
            L.d(cityDefaultLatLng.longitude+" lat "+latLng.longitude);
            return;
        }

        ArrayList<Double> lonlat = new ArrayList<Double>();
		lonlat.add(latLng.longitude);
		lonlat.add(latLng.latitude);
		washer.setLonLat(lonlat);
        finish();
        overridePendingTransitionWithCommonCloseTransition();
    }

    @Override
    public void onResume() {
        super.onResume();
        BA.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
        if(washer != null)
            BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
    }
}
