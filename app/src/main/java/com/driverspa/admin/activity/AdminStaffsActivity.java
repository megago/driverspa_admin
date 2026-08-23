package com.driverspa.admin.activity;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminStaffsAdapter;
import com.driverspa.model.Staff;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminStaffListRequestEvent;
import com.driverspa.util.otto.ws.AdminStaffListResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class AdminStaffsActivity extends AdminBaseActivity {
	    
	@BindView(R.id.list_view)
	ListView listView;
	Washer washer;
	AdminStaffsAdapter adapter;

	List<Staff> staffs;
	int selectedTab = 0;
	@BindView(R.id.resigned)
	View resigned;
	@BindView(R.id.working)
	View working;

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_staffs);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.employees));
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		ButterKnife.bind(this);

		working.setSelected(true);
//		mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);
//		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				finish();
//				overridePendingTransitionWithCommonCloseTransition();
//			}
//		});


    }

    @OnClick(R.id.working)
	public void onWorkingClicked(){
		selectedTab = 0;
		working.setSelected(true);
		resigned.setSelected(false);
		if(adapter != null) {
			adapter.getFilter().filter(Constants.WORKS);
		}
	}

    @OnClick(R.id.resigned)
	public void onResignedClicked(){
		selectedTab = 1;
		working.setSelected(false);
		resigned.setSelected(true);
		if(adapter != null) {
			adapter.getFilter().filter(Constants.RESIGNED);
		}
	}

    @Subscribe
    public void onAdminStaffListReceived(AdminStaffListResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null && event.getData().getResponse() != null){
			staffs = event.getData().getResponse().getStaffs();
			if(staffs!= null && staffs.size() > 0) {
				Collections.sort(staffs, new Comparator<Staff>() {
					@Override
					public int compare(Staff lhs, Staff rhs) {
						int gradeLeft = 0;
						int gradeRight = 0;
						if(lhs.getStatus().equals(Constants.WORKS) && lhs.getRole().equals(Constants.DIRECTOR))
							gradeLeft = 9;
						else if(lhs.getStatus().equals(Constants.WORKS) && lhs.getRole().equals(Constants.OPERATOR))
							gradeLeft = 8;
						else if(lhs.getStatus().equals(Constants.WORKS) && lhs.getRole().equals(Constants.CARWASHER))
							gradeLeft = 7;
						else if(!lhs.getStatus().equals(Constants.WORKS) && lhs.getRole().equals(Constants.CARWASHER))
							gradeLeft = 6;

						if(rhs.getStatus().equals(Constants.WORKS) && rhs.getRole().equals(Constants.DIRECTOR))
							gradeRight = 9;
						else if(rhs.getStatus().equals(Constants.WORKS) && rhs.getRole().equals(Constants.OPERATOR))
							gradeRight = 8;
						else if(rhs.getStatus().equals(Constants.WORKS) && rhs.getRole().equals(Constants.CARWASHER))
							gradeRight = 7;
						else if(!rhs.getStatus().equals(Constants.WORKS) && rhs.getRole().equals(Constants.CARWASHER))
							gradeRight = 6;

						if(gradeLeft >= gradeRight)
							return -1;
						else
						    return 1;
					}
				});

				adapter = new AdminStaffsAdapter(this,staffs);
//				adapter.set(staffs);
				washer.setStaffs(staffs);
				BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));

				if(selectedTab == 0) {
					onWorkingClicked();
//					adapter.getFilter().filter(Constants.WORKS);
//					working.setSelected(true);
//					resigned.setSelected(false);
				}
				else {
					onResignedClicked();
//					adapter.getFilter().filter(Constants.RESIGNED);
//					working.setSelected(false);
//					resigned.setSelected(true);
				}
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
			else{
				addEmptyTextView(listView, BA.str(R.string.no_employees2));
			}

    	}
    	else {
    		addEmptyTextView(listView, BA.str(R.string.no_employees2));
    	}
    }        

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//    	getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	finish();
            	overridePendingTransitionWithCommonCloseTransition();
                break;   
            case R.id.menu_add:
				onAddClicked();
                break;
        }
        return true;
    }	

	@Override
	public void onResume() {
		super.onResume();			
		BA.getEventBus().register(this);
		BA.getEventBus().post(new AdminStaffListRequestEvent(washer.getId()));
		setWaitScreen(true);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
					Staff item = (Staff)adapter.getItem(arg2);
					Intent intent = new Intent(AdminStaffsActivity.this,AdminStaffActivity.class);
					intent.putExtra(Constants.STAFF_INFO, item.serialize());
					startActivity(intent);
					overridePendingTransitionWithCommoOpeningTransition(AdminStaffsActivity.this);
				}
			});
		}
	}

	@OnClick(R.id.btnAdd)
	public void onAddClicked(){
		if(!Functions.isUserHasAccess(washer,user,this)) return;
		Intent intent = new Intent(AdminStaffsActivity.this,AdminAddStaffActivity.class);
		startActivity(intent);
		overridePendingTransitionWithCommoOpeningTransition(AdminStaffsActivity.this);
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}
	User user;

}
