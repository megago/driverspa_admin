package com.driverspa.admin.activity;


import static com.driverspa.util.Constants.HYBRID;
import static com.driverspa.util.Constants.OFFLINE;
import static com.driverspa.util.Constants.ONLINE;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.driverspa.adapter.AdminBoxesAdapter;
import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminBoxAddRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxAddResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminAddBoxActivity extends AdminBaseActivity {
	    
	AdminBoxesAdapter adapter;
	String boxId;
	@BindView(R.id.txtTitle)
	TextView title;
	@BindView(R.id.boxName)
	EditText boxName;
	@BindView(R.id.washerPerson)
	EditText washerPerson;
	@BindView(R.id.offlineImage)
	ImageView offlineImage;
	@BindView(R.id.onlineImage)
	ImageView onlineImage;
	@BindView(R.id.hybridImage)
	ImageView hybridImage;
	String boxType;
	Washer washer;
	@BindView(R.id.hybrid)
	View hybrid;

    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
	    Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_add_box);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.add_box_title));
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddBoxButtonClicked();
			}
		});

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
    	ButterKnife.bind(this);
    	boxType = ONLINE;
		hybrid.setVisibility(View.GONE);
    	setSelectedBoxType();
    } 	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
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

	@Override
	public void onResume() {
		super.onResume();			
		BA.getEventBus().register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
//		BA.getEventBus().post(new AdminWasherInfoRequestEvent(washer.getId()));
		BA.getEventBus().unregister(this);
	}
	
	private void setSelectedBoxType(){		
//		onlineImage.setSelected(false);
//		offlineImage.setSelected(false);
//		hybridImage.setSelected(false);
//
//		if(boxType.equals(ONLINE))
//			onlineImage.setSelected(true);
//		else if(boxType.equals(OFFLINE))
//			offlineImage.setSelected(true);
//		else if(boxType.equals(HYBRID))
//			hybridImage.setSelected(true);

		onlineImage.setImageResource(android.R.color.transparent);
		offlineImage.setImageResource(android.R.color.transparent);
		hybridImage.setImageResource(android.R.color.transparent);
		if(boxType.equals(ONLINE))
			onlineImage.setImageResource(R.drawable.ic_checkmark);
		else if(boxType.equals(OFFLINE))
			offlineImage.setImageResource(R.drawable.ic_checkmark);
		else if(boxType.equals(HYBRID))
			hybridImage.setImageResource(R.drawable.ic_checkmark);

	}
	
	@OnClick(R.id.online)
	public void onOnlineButtonClicked(){
		boxType = ONLINE;
		setSelectedBoxType();
	}	
	
	@OnClick(R.id.offline)
	public void onOfflineButtonClicked(){
		boxType = OFFLINE;
		setSelectedBoxType();
	}	
	
	@OnClick(R.id.hybrid)
	public void onHybridButtonClicked(){
		boxType = HYBRID;
		setSelectedBoxType();		
	}
	
	@OnClick(R.id.btnAddBox)
	public void onAddBoxButtonClicked(){
		if(TextUtils.isEmpty(boxName.getText().toString())){
			ToastUtil.display(this,BA.str(R.string.enter_box_name));
			boxName.requestFocus();
			boxName.setError(BA.str(R.string.required_field));
			return;
		}

//		if(TextUtils.isEmpty(washerPerson.getText().toString())){
//			ToastUtil.display(this,"Введите имя мойщика");
//			washerPerson.requestFocus();
//			washerPerson.setError("Обязательное поле");
//			return;
//		}

    	setWaitScreen(true);
		BA.getEventBus().post(new AdminBoxAddRequestEvent(washer.getId(), boxType, boxName.getText().toString(),washerPerson.getText().toString()));
	}	
		
    @Subscribe
    public void onBoxResponseReceived(AdminBoxAddResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		if(BaseAssist.isSuccess(event.getData())){
				finish();
				overridePendingTransitionWithCommonCloseTransition();
				ToastUtil.display(this,BA.str(R.string.box_added_ok));
//				Builder dialog = new Builder(this);
//    			dialog.setCancelable(false);
//    			dialog.setTitle("Успешно добавлен новый бокс");
//    			dialog.setPositiveButton("Ок", new OnClickListener() {
//    				@Override
//    				public void onClick(DialogInterface dialog, int which) {
//    	            	finish();
//    	            	overridePendingTransitionWithCommonCloseTransition();
//    				}
//    			});
//    			dialog.show();
    		}
    		else{
    			ToastUtil.display(this, event.getData().getMessage());
    		}    			
    	}    	
    }

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {

		}
	}
 
}
