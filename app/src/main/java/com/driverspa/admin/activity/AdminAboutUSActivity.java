package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.splunk.mint.Mint;

import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.admin.fragment.AdminAboutUSFragment;
import com.driverspa.util.UserPreferences;

public class AdminAboutUSActivity extends BaseActivity implements AdminAboutUSFragment.ActivityActions{
	public static FragmentManager fragmentManager;
	  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_common);

		  Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		  setSupportActionBar(mToolbar);
		  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		  getSupportActionBar().setDisplayShowTitleEnabled(false);

          fragmentManager = getSupportFragmentManager();

        if( savedInstanceState == null ) {
			Fragment fragment = new AdminAboutUSFragment();
        	getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
		}        
	  }
	  
	    @Override
		public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case android.R.id.home:
	            	setResult(RESULT_OK, new Intent());
	                finish();
//					overridePendingTransition(0,0);
	                overridePendingTransitionWithCommonCloseTransition();
	                break;          
	        }
	        return true;
	    }

}
