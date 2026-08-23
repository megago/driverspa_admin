package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class AddressActivity extends AdminBaseActivity {
	
	public final static String STREET = "STREET";
	public final static String HOUSE = "HOUSE";
    	
	@BindView(R.id.streetName)
	EditText street;
	@BindView(R.id.icon_pin)
	ImageView pin;
	@BindView(R.id.txtMap)
	TextView mapText;	
	
	double lat;
	double lon;

	Washer washer;

    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_address);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.address));
		mToolbar.findViewById(R.id.action_done).setVisibility(View.VISIBLE);
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});
		ButterKnife.bind(this);
        street.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
  	          washer.setAddress(s.toString());
			}
		});
    }
	
	@OnClick(R.id.map)
	public void onMapSetMarkerClicked(){
		Intent intent = new Intent(this,AddressMapActivity.class);						
		intent.putExtra(AddressMapActivity.LOC_LAT, lat);
		intent.putExtra(AddressMapActivity.LOC_LON, lon);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_ADDRESS_MAP);
	}	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
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
	public void onWasherReceived(WasherGetResponseEvent event){
		washer = event.getWasher();
		if(washer != null) {
			String addressStr = washer.getAddress();//getIntent().getStringExtra(STREET);
			street.setText(addressStr);
			lat = washer.getLonLat().get(1);   //getIntent().getDoubleExtra(AddressMapActivity.LOC_LAT, AddressMapActivity.LAT);
			lon = washer.getLonLat().get(0);//getIntent().getDoubleExtra(AddressMapActivity.LOC_LON, AddressMapActivity.LON);
			DecimalFormat f = new DecimalFormat("##.00");

//			if (lat != AddressMapActivity.LAT && lon != AddressMapActivity.LON) {
				pin.setVisibility(View.VISIBLE);
				mapText.setText(f.format(lat) + ", " + f.format(lon));
//			}
		}
	}

    @Override
    public void onBackPressed() {
    	saveData();
    }
    
    private void saveData(){
		String addressStr = street.getText().toString();
		if(TextUtils.isEmpty(addressStr)){
			street.setError(BA.str(R.string.required_field));
			mapText.requestFocus();
			ToastUtil.display(this,BA.str(R.string.enter_address));
			return;
		}

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
		if(washer != null) {
			String addressStr = street.getText().toString();
			washer.setAddress(addressStr);
			BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
		}
	}
}
