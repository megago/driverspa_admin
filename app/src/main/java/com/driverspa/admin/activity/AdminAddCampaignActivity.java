package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.*;

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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminCampaignAddRequestEvent;
import com.driverspa.util.otto.ws.AdminCampaignAddResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminAddCampaignActivity extends AdminBaseActivity  implements DatePickerDateSetListener {

	AdminBoxesAdapter adapter;
	String boxId;
	@BindView(R.id.txtTitle)
	TextView title;
	@BindView(R.id.description)
	EditText description;
	@BindView(R.id.endDate)
	TextView endDate;
	@BindView(R.id.discount)
	EditText discount;
	@BindView(R.id.offlineImage)
	ImageView offlineImage;
	@BindView(R.id.onlineImage)
	ImageView onlineImage;
	@BindView(R.id.bothImage)
	ImageView bothImage;
	String campaignType;
	Washer washer;
	@BindView(R.id.both)
	View both;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
    Instant endDateDT;

    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_add_campaign);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Добавление акции");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddCampaignButtonClicked();
			}
		});

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
    	ButterKnife.bind(this);
    	campaignType = Constants.BOTH;
    	setSelectedCampaignType();
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
				onBackPressed();
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

	private void setSelectedCampaignType(){

	onlineImage.setImageResource(android.R.color.transparent);
	offlineImage.setImageResource(android.R.color.transparent);
	bothImage.setImageResource(android.R.color.transparent);
	if(campaignType.equals(ONLINE))
		onlineImage.setImageResource(R.drawable.ic_checkmark);
	else if(campaignType.equals(OFFLINE))
		offlineImage.setImageResource(R.drawable.ic_checkmark);
	else if(campaignType.equals(BOTH))
		bothImage.setImageResource(R.drawable.ic_checkmark);

	}

	@OnClick(R.id.online)
	public void onOnlineButtonClicked(){
		campaignType = ONLINE;
		setSelectedCampaignType();
	}

	@OnClick(R.id.offline)
	public void onOfflineButtonClicked(){
		campaignType = OFFLINE;
		setSelectedCampaignType();
	}

	@OnClick(R.id.both)
	public void onBothButtonClicked(){
		campaignType = BOTH;
		setSelectedCampaignType();
	}


	public void onAddCampaignButtonClicked(){
		if(TextUtils.isEmpty(description.getText().toString())){
			ToastUtil.display(this,"Введите описание акции");
			description.requestFocus();
			description.setError("Обязательное поле");
			return;
		}
		if(TextUtils.isEmpty(endDate.getText().toString())){
			ToastUtil.display(this,"Введите дата окончания акции");
			return;
		}

		if(TextUtils.isEmpty(discount.getText().toString())){
			discount.requestFocus();
			ToastUtil.display(this,"Введите скидку для акции");
			return;
		}

		int discountInt = 0;
		try{
			discountInt = Integer.parseInt(discount.getText().toString());
		}
		catch(Exception e){
			discountInt = 0;
		}
		if(discountInt == 0){
			discount.requestFocus();
			ToastUtil.display(this,"Введите скидку для акции");
			return;
		}

    	setWaitScreen(true);
		BA.getEventBus().post(new AdminCampaignAddRequestEvent(washer.getId(),description.getText().toString(), campaignType, endDateDT, discountInt));
	}

    @Subscribe
    public void onCampaignResponseReceived(AdminCampaignAddResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		if(BaseAssist.isSuccess(event.getData())){
				finish();
				overridePendingTransitionWithCommonCloseTransition();
				ToastUtil.display(this,"Успешно добавлена новая акция");
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

	@OnFocusChange(R.id.discount)
	public void onFocusChangeDiscount(boolean focus){
		if(!focus){
			if(TextUtils.isEmpty(discount.getText().toString()))
				discount.setText("0");
		}
		else{
			if(!TextUtils.isEmpty(discount.getText().toString()) && discount.getText().toString().equals("0")){
				discount.setText("");
			}
		}
	}

	@Override
	public void onDatePickerDataset(Long time, String type) {
		Instant dt = Instant.ofEpochMilli(time);
		String dateStr = DATE_FORMATTER.format(dt);
		endDate.setText(dateStr);
		endDateDT = dt;
	}

	@OnClick(R.id.endDate)
	public void endDateClicked(){
		if(endDateDT == null)
			endDateDT = Instant.now();
		DatePickerFragment picker =  DatePickerFragment.newInstance("end_date", endDateDT.toEpochMilli());
		picker.setDatePickerDateSetListener(this);
		picker.show(getSupportFragmentManager(), "datePicker");
	}
}
