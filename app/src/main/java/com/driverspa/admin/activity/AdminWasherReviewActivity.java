package com.driverspa.admin.activity;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.splunk.mint.Mint;

import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.admin.fragment.AdminWasherReviewFragment;
import com.driverspa.model.Washer;
public class AdminWasherReviewActivity extends BaseActivity{
    	
	Washer washer;
	AdminWasherReviewFragment fragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(this.getApplication(), "b054ddc0");
        setContentView(R.layout.activity_common);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		if( savedInstanceState == null ) {
        	getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new AdminWasherReviewFragment()).commit();
		 }        
	   }

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	super.onCreateOptionsMenu(menu);	    	
//	    	getMenuInflater().inflate(R.menu.menu_add, menu);
	    	return true;
	    }
	    
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) { 
	            case android.R.id.home:
	                finish();
	                overridePendingTransitionWithCommonCloseTransition();
	                break;          
	        }
	        return true;
	    }
}
