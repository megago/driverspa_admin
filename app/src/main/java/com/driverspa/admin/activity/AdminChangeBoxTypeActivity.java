package com.driverspa.admin.activity;


import static com.driverspa.util.Constants.HYBRID;
import static com.driverspa.util.Constants.OFFLINE;
import static com.driverspa.util.Constants.ONLINE;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.BookingType;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminBoxDeleteRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxDeleteResponseEvent;
import com.driverspa.util.otto.ws.AdminBoxRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxResponseEvent;
import com.driverspa.util.otto.ws.AdminBoxTypeChangeRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxTypeChangeResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminChangeBoxTypeActivity extends AdminBaseActivity {
	    
	AdminBoxesAdapter adapter;
	String boxId;
	@BindView(R.id.txtTitle)
	TextView title;
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

	@BindView(R.id.boxName)
	EditText boxName;
	@BindView(R.id.washerPerson)
	EditText washerPerson;
    String boxNameString;
    String washerPersonString;

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

    	setContentView(R.layout.activity_admin_change_box_type);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		ButterKnife.bind(this);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);

		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onEditBoxClicked();
			}
		});

		boxId = getIntent().getStringExtra(Constants.EXTRA_BOX_ID);
		boxNameString = getIntent().getStringExtra(Constants.EXTRA_BOX_NAME);
		washerPersonString = getIntent().getStringExtra(Constants.EXTRA_BOX_WASHER_PERSON);
		boxName.setText(boxNameString);
		washerPerson.setText(washerPersonString);
//		titleView.setText(boxName);
    	title.setVisibility(View.GONE);

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//    	getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;        
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	finish();
            	overridePendingTransitionWithCommonCloseTransition();
                break;
            case R.id.menu_delete:
            	deleteBox();
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
		onlineImage.setImageResource(android.R.color.transparent);
		offlineImage.setImageResource(android.R.color.transparent);
		hybridImage.setImageResource(android.R.color.transparent);
		if(ONLINE.equals(boxType))
			onlineImage.setImageResource(R.drawable.ic_checkmark);
		else if(OFFLINE.equals(boxType))
			offlineImage.setImageResource(R.drawable.ic_checkmark);
		else if(HYBRID.equals(boxType))
			hybridImage.setImageResource(R.drawable.ic_checkmark);

		//		onlineImage.setSelected(false);
//		offlineImage.setSelected(false);
//		hybridImage.setSelected(false);

//		if(boxType.equals(ONLINE))
//			onlineImage.setSelected(true);
//		else if(boxType.equals(OFFLINE))
//			offlineImage.setSelected(true);
//		else if(boxType.equals(HYBRID))
//			hybridImage.setSelected(true);

	}
	
	@OnClick(R.id.online)
	public void onOnlineButtonClicked(){
		boxType = ONLINE;
		setSelectedBoxType();
//		if(!boxType.equals(ONLINE)){
//			setWaitScreen(true);
//			BA.getEventBus().post(new AdminBoxTypeChangeRequestEvent(washer.getId(), boxId, ONLINE,boxName));
//		}
	}	
	
	@OnClick(R.id.offline)
	public void onOfflineButtonClicked(){
		boxType = OFFLINE;
		setSelectedBoxType();
//		if(!boxType.equals(OFFLINE)){
//			setWaitScreen(true);
//			BA.getEventBus().post(new AdminBoxTypeChangeRequestEvent(washer.getId(), boxId, OFFLINE,boxName));
//		}
	}	
	
//	@OnClick(R.id.hybrid)
//	public void onHybridButtonClicked(){
//		if(!boxType.equals(HYBRID)){
//			setWaitScreen(true);
//			BA.getEventBus().post(new AdminBoxTypeChangeRequestEvent(washer.getId(), boxId, HYBRID,boxName));
//		}
//	}

    @Subscribe
    public void onBoxResponseReceived(AdminBoxResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		if(BaseAssist.isSuccess(event.getData())){
				if(event.getData().getResponse().getBookingType() != null)
    			   boxType = event.getData().getResponse().getBookingType().getValue().toString();
    			setSelectedBoxType();
//				if(boxType.equals(HYBRID)){
//					hybrid.setVisibility(View.VISIBLE);
//				}
    		}
    		else{
    			ToastUtil.display(this, event.getData().getMessage());
    		}    			
    	}    	
    }
    
    @Subscribe
    public void onBoxTypeChangeResponseReceived(AdminBoxTypeChangeResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		if(BaseAssist.isSuccess(event.getData())){
				finish();
				overridePendingTransitionWithCommonCloseTransition();
				ToastUtil.display(this, "Бокс успешно изменен");
//    			boxType = event.getData().getResponse().getBookingType();
//    			setSelectedBoxType();
    		}
    		else{
    			ToastUtil.display(this, event.getData().getMessage());
    		}    			
    	}    	
    }

	@OnClick(R.id.btnDeleteBox)
    public void deleteBox(){
		Builder dialog = new Builder(this,AlertDialog.THEME_HOLO_LIGHT);
		dialog.setTitle("Действительно хотите удалить бокс?");
		dialog.setNegativeButton("Отмена", null);
		dialog.setPositiveButton("Ок", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				BA.getEventBus().post(new AdminBoxDeleteRequestEvent(washer.getId(), boxId));			}
		});
		dialog.show();

    }

    @Subscribe
    public void onBoxDeleteResponseReceived(AdminBoxDeleteResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		if(BaseAssist.isSuccess(event.getData())){
    	        finish();
    	        overridePendingTransitionWithCommonCloseTransition();
				ToastUtil.display(this, "Бокс успешно удален.");
//    			Builder dialog = new Builder(this);
//    			dialog.setCancelable(false);
//    			dialog.setTitle("Бокс "+boxName+" успешно удален. Номером "+boxName+" может стать следующий бокс.");
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

	@OnClick(R.id.btnEditBox)
	public void onEditBoxClicked(){
		if(TextUtils.isEmpty(boxName.getText().toString())){
			ToastUtil.display(this,"Введите название бокса");
			boxName.requestFocus();
			boxName.setError("Обязательное поле");
			return;
		}

		setWaitScreen(true);
		BA.getEventBus().post(new AdminBoxTypeChangeRequestEvent(washer.getId(), boxId, BookingType.toEnum(boxType.toLowerCase()), boxName.getText().toString(),washerPerson.getText().toString(),null));
	}


	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			BA.getEventBus().post(new AdminBoxRequestEvent(washer.getId(), boxId));
			setWaitScreen(true);
		}
	}

}
