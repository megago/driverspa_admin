package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.splunk.mint.Mint;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.fragment.AdminAddCampaignFragment;
import com.driverspa.admin.fragment.AdminAddWashCountCampaignFragment;
import com.driverspa.admin.fragment.AdminCampaignFragment;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.FragmentVisibilityEvent;

public class AdminCampaignActivity extends AdminBaseActivity implements AdminCampaignFragment.ActivityActions, AdminAddCampaignFragment.ActivityActions, AdminAddWashCountCampaignFragment.ActivityActions {

	public static FragmentManager fragmentManager;
	Fragment fragment;
	TextView titleView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

		setContentView(R.layout.activity_admin_campaign);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.promo));

		fragmentManager = getSupportFragmentManager();

		if( savedInstanceState == null ) {
			fragment = new AdminCampaignFragment();
			invalidateOptionsMenu();
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
		}

		getSupportFragmentManager().addOnBackStackChangedListener(
				new FragmentManager.OnBackStackChangedListener() {
					public void onBackStackChanged() {
						invalidateOptionsMenu();
						switch(getSupportFragmentManager().getBackStackEntryCount()){
							case 0: //CampaignFragment
								BA.getEventBus().post(new FragmentVisibilityEvent());
								break;
							case 1://AddCampaignFragment
								break;
						}
					}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
//		if(fragment != null && fragment instanceof AdminCampaignFragment){
//			getMenuInflater().inflate(R.menu.menu_options, menu);
//		}
//		else
//			getMenuInflater().inflate(R.menu.menu_clean, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				setResult(RESULT_OK, new Intent());
				onBackPressed();
//				finish();
//				overridePendingTransitionWithCommonCloseTransition();
				break;
			case R.id.menu_options:
				if(fragment != null && fragment instanceof AdminCampaignFragment)
					((AdminCampaignFragment)fragment).showOptions();
				break;
		}
		return true;
	}

	@Override
	public void openAddCampaign() {
//		titleView.setText("Доб");
		Fragment fragment = new AdminAddCampaignFragment();
		invalidateOptionsMenu();
		addFragmentContent(R.id.fragment_container,fragment);
//		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
	}

	@Override
	public void openAddWashCountCampaign() {
		Fragment fragment = new AdminAddWashCountCampaignFragment();
		invalidateOptionsMenu();
		addFragmentContent(R.id.fragment_container,fragment);
	}

	@Override
	public void openCampaign() {
		onBackPressed();
//		fragment = new AdminCampaignFragment();
//		invalidateOptionsMenu();
//		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
	}
}
