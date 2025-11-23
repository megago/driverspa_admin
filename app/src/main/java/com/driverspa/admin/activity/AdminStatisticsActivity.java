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
import com.driverspa.admin.fragment.AdminStatisticsFragment;
import com.driverspa.util.UserPreferences;

public class AdminStatisticsActivity extends BaseActivity implements AdminStatisticsFragment.ActivityActions{
	public static FragmentManager fragmentManager;
	  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		  Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		  Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

          setContentView(R.layout.activity_common_custom);

		  Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		  setSupportActionBar(mToolbar);
		  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		  getSupportActionBar().setDisplayShowTitleEnabled(false);
		  boolean fromWorkshift=getIntent().getBooleanExtra(AdminTransactionsActivity.FROM_WORKSHIFT,false);

          fragmentManager = getSupportFragmentManager();

        if( savedInstanceState == null ) {
			if(!fromWorkshift) {
				Fragment fragment = AdminStatisticsFragment.init(false,0,0);
				getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
			}
			else{
				long workShiftStartDate = getIntent().getLongExtra(AdminTransactionsActivity.START_DATE,0);
				long workShiftEndDate = getIntent().getLongExtra(AdminTransactionsActivity.END_DATE,0);
				Fragment fragment = AdminStatisticsFragment.init(true,workShiftStartDate,workShiftEndDate);
				getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();

			}
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
