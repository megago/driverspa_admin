package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.BOOKING_INFO_TYPE;
import static com.driverspa.util.Constants.EXTRA_BOOK;
import static com.driverspa.util.Constants.EXTRA_BOOKING_ID;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.splunk.mint.Mint;

import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.admin.fragment.AdminBookingInfoFragment;
import com.driverspa.model.BookInfo;
import com.driverspa.model.ClientInfo;
import com.driverspa.util.UserPreferences;

public class AdminBookingInfoActivity extends BaseActivity implements AdminBookingInfoFragment.ActivityActions{
	public static FragmentManager fragmentManager;
	AdminBookingInfoFragment fragment;

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
			String bookId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
			String bookInfoType = getIntent().getStringExtra(BOOKING_INFO_TYPE);			
			fragment = AdminBookingInfoFragment.newInstance(bookId,bookInfoType);
        	getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
		}        
	  }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
    	getMenuInflater().inflate(R.menu.menu_options, menu);
		return true;
	}

	@Override
		public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case android.R.id.home:
	            	setResult(RESULT_OK, new Intent());
	                finish();	                
	                overridePendingTransitionWithCommonCloseTransition();
	                break;
				case R.id.menu_options:
					if(fragment != null)
					  fragment.showOptions();
					break;
	        }
	        return true;
	    }
	    
		@Override
		public void showProgressBar(boolean set) {
			setWaitScreen(set);
		}

		@Override
		public void goToHome() {
			setResult(RESULT_OK, new Intent());
			finish();				
	  }

	@Override
	public void editEntry(BookInfo bookInfo) {
		Intent intent = new Intent(this,AdminEditEntryActivity.class);
		intent.putExtra(EXTRA_BOOK, bookInfo.serialize() );
		startActivity(intent);
	}

	@Override
	public void openClientInfo(ClientInfo client) {
		Intent intent = new Intent(this,AdminClientActivity.class);
		intent.putExtra(AdminClientActivity.CLIENT_INFO,client.serialize());
		startActivity(intent);
		overridePendingTransitionWithCommoOpeningTransition(this);
	}

}
