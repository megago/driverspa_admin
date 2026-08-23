package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.splunk.mint.Mint;

import java.util.ArrayList;

import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.activity.UserPhotoChangeActivity;
import com.driverspa.admin.fragment.AdminProfileFragment;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.PhotoPostRequestEvent;

public class AdminProfileActivity extends BaseActivity implements AdminProfileFragment.ActivityActions{
	
	PhotoPostRequestEvent photoPost;
	AdminProfileFragment fragment;
	
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
        	fragment = new AdminProfileFragment();
        	getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss();
		}
		  else {
			// On recreation (e.g. after a language switch calls
			// setApplicationLocales) the FragmentManager restores the fragment, but
			// the field would stay null and both back handlers are guarded on it —
			// leaving back unresponsive. Re-acquire the existing instance.
			fragment = (AdminProfileFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		}
	  }

	@Override
	public void showProgressBar(boolean set) {
		setWaitScreen(set);
	}

	@Override
	  public void profileUserPhotoChange(String userId, ArrayList<PhotoParcelable> photos) {
			startActivityForResult(UserPhotoChangeActivity.newIntent(this, userId, photos), ActivityForResult.ACTIVITY_PHOTO_CHANGE);
	  }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if(fragment != null && fragment.saveExitData()) {
//					ToastUtil.display(this, "Автосохранение...");
					setResult(RESULT_CANCELED, new Intent());
					finish();
					overridePendingTransitionWithCommonCloseTransition();
				}
				break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (fragment != null && fragment.saveExitData()) {
			super.onBackPressed();
			fragment.saveData();
//			ToastUtil.display(this, "Автосохранение...");
			setResult(RESULT_CANCELED, new Intent());
			finish();
			overridePendingTransitionWithCommonCloseTransition();
		}
	}
}
