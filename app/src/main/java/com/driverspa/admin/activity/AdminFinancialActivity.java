package com.driverspa.admin.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.fragment.AdminCashFragment;
import com.driverspa.admin.fragment.AdminMoneyFragment;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminCashFinancialRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;

public class AdminFinancialActivity extends AdminBaseActivity implements AdminMoneyFragment.ActivityActions, AdminCashFragment.ActivityActions, DatePickerDateSetListener {

	@BindView(R.id.pager)
	ViewPager viewPager;
	PagerAdapter adapter;
	int selectedTab = 0;
	@BindView(R.id.money)
	View money;
	@BindView(R.id.cash)
	View cash;
	@BindView(R.id.incomeExpense)
	View incomeExpense;

	Washer washer;
	TextView titleView;

	DecimalFormat formatter = new DecimalFormat("#,###.##");
	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
	DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(ZoneId.systemDefault());
	@BindView(R.id.startTime)
	TextView startTime;
	Instant startTimeDt;
	@BindView(R.id.endTime)
	TextView endTime;
	Instant endTimeDt;

	@BindView(R.id.spinner)
	NDSpinner spinner;
	private boolean isSpinnerTouched = false;
	private int selectedPeriodType = 0;
	LinkedHashMap<Integer,String> operationTypes =  BA.getReference().getOperationTypes();

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
	    Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_financial);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		ButterKnife.bind(this);

		cash.setSelected(true);
		adapter = new PagerAdapter(getSupportFragmentManager());

		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				selectedTab = position;
				setTabButtons(position);
			}
		});

		titleView.setText(BA.str(R.string.finance));
		viewPager.setOffscreenPageLimit(3);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(selectedTab);

		spinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isSpinnerTouched = true;
				return false;
			}
		});

		installFinishOnBackCallback();
	}


	private void createSpinner(){
		if(spinner != null) {
			final String[] valuesArray =  {BA.str(R.string.for_period),BA.str(R.string.for_day),BA.str(R.string.for_week), BA.str(R.string.for_month)};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
			adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (!isSpinnerTouched) return;
					selectedPeriodType = pos;
					setSelectedPeriodDate();
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			spinner.setAdapter(adapter);
			isSpinnerTouched = true;
			spinner.setSelection(selectedPeriodType);
			isSpinnerTouched = false;
		}
	}


	private void initPeriodTimes(){
		startTimeDt = Functions.getStartOfDay(Instant.now());
		endTimeDt = Functions.getEndOfDay(Instant.now());
		startTime.setText(timeFormatter.format(startTimeDt));
		endTime.setText(timeFormatter.format(endTimeDt));
	}

	@Override
	public void onDatePickerDataset(Long time,String type) {
		if(type.equals("start")) {
			if(time > endTimeDt.toEpochMilli()) {
				ToastUtil.display(this,BA.str(R.string.err_start_date_after_end));
				return;
			}

			startTimeDt = Functions.getStartOfDay(Instant.ofEpochMilli(time));
			startTime.setText(timeFormatter.format(startTimeDt));
		}

		if(type.equals("end")) {
			if(time < startTimeDt.toEpochMilli()) {
				ToastUtil.display(this,BA.str(R.string.err_end_date_before_start));
				return;
			}

			endTimeDt = Functions.getEndOfDay(Instant.ofEpochMilli(time));
			endTime.setText(timeFormatter.format(endTimeDt));
		}

		setWaitScreen(true);
		BA.getEventBus().post(new AdminCashFinancialRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)  ));
	}


	private void setSelectedPeriodDate(){
		switch (selectedPeriodType){
			case 0://period
				if(startTimeDt == null || endTimeDt == null){
					initPeriodTimes();
				}
				setWaitScreen(true);
				startTime.setText(timeFormatter.format(startTimeDt));
				endTime.setText(timeFormatter.format(endTimeDt));
				BA.getEventBus().post(new AdminCashFinancialRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
			case 1: //today
				initPeriodTimes();

				setWaitScreen(true);
				startTime.setText(timeFormatter.format(startTimeDt));
				endTime.setText(timeFormatter.format(endTimeDt));

				BA.getEventBus().post(new AdminCashFinancialRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
			case 2: //week
				setWaitScreen(true);

				//Start of the week
				Calendar c = Calendar.getInstance();
				c.setFirstDayOfWeek(Calendar.MONDAY);
				c.setTimeInMillis(System.currentTimeMillis());
				int today = c.get(Calendar.DAY_OF_WEEK);
				c.add(Calendar.DAY_OF_WEEK, -today+Calendar.MONDAY);
				c.set(Calendar.HOUR_OF_DAY, 00);
				c.set(Calendar.MINUTE, 00);
				startTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());

				//Today
				c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				endTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());
				startTime.setText(timeFormatter.format(startTimeDt));
				endTime.setText(timeFormatter.format(endTimeDt));
				BA.getEventBus().post(new AdminCashFinancialRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
			case 3: //month
				setWaitScreen(true);
				c = Calendar.getInstance();
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.set(Calendar.HOUR_OF_DAY, 00);
				c.set(Calendar.MINUTE, 00);
				startTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());

				//Today
				c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				endTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());

				startTime.setText(timeFormatter.format(startTimeDt));
				endTime.setText(timeFormatter.format(endTimeDt));
				BA.getEventBus().post(new AdminCashFinancialRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
		}
	}

	@OnClick(R.id.startTime)
	public void startTimeClicked() {
		DatePickerFragment picker =  DatePickerFragment.newInstance("start", startTimeDt.toEpochMilli());
		picker.setDatePickerDateSetListener(this);
		picker.show(getSupportFragmentManager(), "start");
	}

	@OnClick(R.id.endTime)
	public void endTimeClicked() {
		DatePickerFragment picker =  DatePickerFragment.newInstance("end", endTimeDt.toEpochMilli());
		picker.setDatePickerDateSetListener(this);
		picker.show(getSupportFragmentManager(), "end");
	}

	//	@OnClick(R.id.transactions)
	public void transactionsHistoryClicked(String transactionCode, String informationText) {
		Intent intent = new Intent(this,AdminTransactionsActivity.class);
		intent.putExtra(AdminTransactionsActivity.FROM_WORKSHIFT,true);
		intent.putExtra(AdminTransactionsActivity.START_DATE, startTimeDt.toEpochMilli());
		intent.putExtra(AdminTransactionsActivity.END_DATE, endTimeDt.toEpochMilli());
		intent.putExtra(AdminTransactionsActivity.OPERATION_TYPE,transactionCode);
		intent.putExtra(AdminTransactionsActivity.INFORMATION_TEXT,informationText);
		startActivity(intent);
	}


	private String formatDate(Instant instant){
		if(instant != null) {
			return requestFormatter.format(instant);
		}
		return requestFormatter.format(Instant.now());
	}



	private void setTabButtons(int pos){
		switch (pos){
			case 0:
				money.setSelected(false);
				cash.setSelected(true);
				incomeExpense.setSelected(false);
				break;
			case 1:
				money.setSelected(true);
				cash.setSelected(false);
				incomeExpense.setSelected(false);
				break;
			case 2:
				money.setSelected(false);
				cash.setSelected(false);
				incomeExpense.setSelected(true);
				break;
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;        
    }


	@OnClick(R.id.cash)
	public void onStaffInfoButtonClick(){
		viewPager.setCurrentItem(0);
	}

	@OnClick(R.id.money)
	public void onStaffFinancialButtonClick(){
		viewPager.setCurrentItem(1);
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
			createSpinner();
		}
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


	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}
	User user;
	Activity context = this;

	public class PagerAdapter extends FragmentStatePagerAdapter {

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
				case 0:
					//Staff fragment
					return  new AdminCashFragment();
				case 1:
					//Staff fragment
					return  new AdminMoneyFragment();

//				case 3:
//					//Staff fragment
//					return  new AdminIncomeExpenseFragment();
			}
			return null;
		}
		@Override
		public int getCount() {
			return 2; //No of Tabs
		}
	}


	@Override
	public void setWaitScreen(boolean set) {
		super.setWaitScreen(set);
	}
}
