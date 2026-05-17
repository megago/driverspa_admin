package com.driverspa.admin.activity;


import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
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

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminWorkshiftsAdapter;
import com.driverspa.model.Washer;
import com.driverspa.model.Workshift;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminWorkshiftAddRequestEvent;
import com.driverspa.util.otto.ws.AdminWorkshiftAddResponseEvent;
import com.driverspa.util.otto.ws.AdminWorkshiftCloseRequestEvent;
import com.driverspa.util.otto.ws.AdminWorkshiftCloseResponseEvent;
import com.driverspa.util.otto.ws.AdminWorkshiftsRequestEvent;
import com.driverspa.util.otto.ws.AdminWorkshiftsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminWorkShiftsActivity extends AdminBaseActivity {
	    
	@BindView(R.id.list_view)
	ListView listView;
	Washer washer;
	AdminWorkshiftsAdapter adapter;
	
    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_workshifts);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Смены");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		ButterKnife.bind(this);
    } 	
    
    @Subscribe
    public void onAdminWorkshiftsReceived(AdminWorkshiftsResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
			List<Workshift> workshifts = event.getData().getResponse().getWorkshifts();
			if(workshifts!= null && workshifts.size() > 0) {
				adapter = new AdminWorkshiftsAdapter(this);
				adapter.set(workshifts);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
			else {
				addEmptyTextView(listView, "Нет смены");
			}
    	}
    	else {
    		addEmptyTextView(listView, "Нет смены");
    	}
    }

    @Subscribe
    public void onAdminWorkshiftCloseReceived(AdminWorkshiftCloseResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData()))
			   BA.getEventBus().post(new AdminWorkshiftsRequestEvent(washer.getId()));
			else{
				ToastUtil.display(this,event.getData().getMessage());
			}
    	}
    	else {
			ToastUtil.display(this,"Ошибка при закрытии");
    	}
    }

	@Subscribe
    public void onAdminWorkshiftAddReceived(AdminWorkshiftAddResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData()))
			   BA.getEventBus().post(new AdminWorkshiftsRequestEvent(washer.getId()));
			else{
				ToastUtil.display(this,event.getData().getMessage());
			}
    	}
    	else {
			ToastUtil.display(this,"Ошибка при открытии смены");
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
		BA.getEventBus().post(new AdminWorkshiftsRequestEvent(washer.getId()));
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
				public void onItemClick(AdapterView<?> arg0, View arg1,final int arg2,long arg3) {
					final Workshift item = (Workshift)adapter.getItem(arg2);

					if(item.getEndTime() == null) {
						AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(AdminWorkShiftsActivity.this);
						dialog.setTitle("Закрыть смену?");
						dialog.setNeutralButton("Нет", null);
						dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								setWaitScreen(true);
								BA.getEventBus().post(new AdminWorkshiftCloseRequestEvent(washer.getId(), item.getId()));
							}
						});
						dialog.show();
					}
					else{
						Intent intent = new Intent(AdminWorkShiftsActivity.this,AdminWorkshiftFinancialStatisticsActivity.class);
						intent.putExtra(Constants.WORKSHIFT_INFO, item.serialize());
						startActivity(intent);
					}
				}
			});
		}
	}

	@OnClick(R.id.btnAdd)
	public void onAddClicked(){
		AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(AdminWorkShiftsActivity.this);
		dialog.setTitle("Открыть смену?");
		dialog.setNeutralButton("Нет", null);
		dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setWaitScreen(true);
				BA.getEventBus().post(new AdminWorkshiftAddRequestEvent(Constants.URL_PREFIX+washer.getId()));
			}
		});
		dialog.show();
	}

}
