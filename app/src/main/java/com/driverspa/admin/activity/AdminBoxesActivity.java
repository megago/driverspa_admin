package com.driverspa.admin.activity;


import static com.driverspa.util.Constants.EXTRA_BOX_ID;
import static com.driverspa.util.Constants.EXTRA_BOX_NAME;
import static com.driverspa.util.Constants.EXTRA_BOX_WASHER_PERSON;

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

import com.driverspa.adapter.AdminBoxesAdapter;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Box;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminBoxListRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxListResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminBoxesActivity extends AdminBaseActivity {
	    
	@BindView(R.id.list_view)
	ListView listView;
	Washer washer;
	AdminBoxesAdapter adapter;
	
    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

    	setContentView(R.layout.activity_admin_boxes);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Боксы");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
		});

		ButterKnife.bind(this);
    } 	
    
    @Subscribe
    public void onAdminBoxListReceived(AdminBoxListResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		ArrayList<Box> boxes = event.getData().getResponse().getBoxes();
			boxes = Functions.filterDisableddBoxes(boxes);

			washer.setBoxSettings(boxes);
    		adapter = new AdminBoxesAdapter(this);
    		adapter.set(boxes);
    		listView.setAdapter(adapter);
    		adapter.notifyDataSetChanged();
    	}
    	else {
    		addEmptyTextView(listView, "Боксы не настроены");
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
				onAddBoxClicked();
                break;
        }
        return true;
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
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			BA.getEventBus().post(new AdminBoxListRequestEvent(washer.getId()));
			setWaitScreen(true);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
					String boxId = adapter.getList().get(arg2).getUid();
					String boxName = adapter.getList().get(arg2).getBoxName();
					String washerPerson = adapter.getList().get(arg2).getWasherPerson();
					Intent intent = new Intent(AdminBoxesActivity.this,AdminChangeBoxTypeActivity.class);
					intent.putExtra(EXTRA_BOX_ID, boxId);
					intent.putExtra(EXTRA_BOX_NAME, boxName);
					intent.putExtra(EXTRA_BOX_WASHER_PERSON, washerPerson);
					startActivity(intent);
					overridePendingTransitionWithCommoOpeningTransition(AdminBoxesActivity.this);
				}
			});
		}
	}

	@OnClick(R.id.btnAddBox)
	public void onAddBoxClicked(){
		Intent intent = new Intent(AdminBoxesActivity.this,AdminAddBoxActivity.class);
		startActivity(intent);
		overridePendingTransitionWithCommoOpeningTransition(AdminBoxesActivity.this);
	}

}
