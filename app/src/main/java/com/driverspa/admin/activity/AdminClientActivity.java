package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.EXTRA_BOOKING_ID;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;

import com.splunk.mint.Mint;

import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.admin.fragment.AdminClientFragment;
import com.driverspa.model.ClientInfo;
import com.driverspa.util.UserPreferences;

public class AdminClientActivity extends BaseActivity implements AdminClientFragment.ActivityActions{

	public static final String CLIENT_INFO = "CLIENT_INFO";
	AdminClientFragment fragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

		  Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		  setSupportActionBar(mToolbar);
		  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		  getSupportActionBar().setDisplayShowTitleEnabled(false);

		  if( savedInstanceState == null ) {
			Intent intent = getIntent();
			String clientInfo = intent.getStringExtra(CLIENT_INFO);
        	fragment =  AdminClientFragment.newInstance(clientInfo);
        	getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
		}        
	  }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_options, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
			finish();
			overridePendingTransitionWithCommonCloseTransition();
	}

	@Override
	public void openBookInfo(String bookId) {
		Intent intent = new Intent(this,AdminBookingInfoActivity.class);
		intent.putExtra(EXTRA_BOOKING_ID, bookId );
		startActivity(intent);
	}

	@Override
	public void editClient(ClientInfo client) {
		Intent intent = new Intent(this,AdminEditClientActivity.class);
		intent.putExtra(CLIENT_INFO, client.serialize());
		startActivity(intent);
	}


}
