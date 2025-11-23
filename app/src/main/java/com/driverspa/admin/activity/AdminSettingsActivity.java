package com.driverspa.admin.activity;

import android.os.Bundle;
import android.view.MenuItem;

import com.splunk.mint.Mint;

import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.admin.fragment.AdminSettingsFragment;
import com.driverspa.util.UserPreferences;

public class AdminSettingsActivity extends BaseActivity implements AdminSettingsFragment.ActivityActions{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

		setContentView(R.layout.activity_common);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		setTitle("");

		if( savedInstanceState == null ) {
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new AdminSettingsFragment()).commitAllowingStateLoss();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				overridePendingTransition(0, 0);
				break;
		}
		return true;
	}

}
