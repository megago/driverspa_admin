package com.driverspa.admin.activity;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AnalyticsTabBarAdapter;
import com.driverspa.admin.fragment.AdminAnalyticsFragment;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.Statistic;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminAnalyticsRequestEvent;
import com.driverspa.util.otto.ws.AdminAnalyticsResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;

public class AdminAnalyticsActivity extends AdminBaseActivity implements AdminAnalyticsFragment.ActivityActions, DatePickerDateSetListener {

	@BindView(R.id.pager)
	ViewPager viewPager;
	@BindView (R.id.tabView)
	Gallery tabView;

	int selectedTab = 0;
	Washer washer;
	TextView titleView;
	DecimalFormat formatter = new DecimalFormat("#,###.##");
	SimpleDateFormat timeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	@BindView(R.id.startTime)
	TextView startTime;
	Date startTimeDt;
	@BindView(R.id.endTime)
	TextView endTime;
	Date endTimeDt;

	@BindView(R.id.spinner)
	NDSpinner spinner;
	private boolean isSpinnerTouched = false;
	private int selectedPeriodType = 0;

	List<Statistic> statistics = new ArrayList<>();

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
	    Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_analytics);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		ButterKnife.bind(this);

		titleView.setText("Аналитика");
		spinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isSpinnerTouched = true;
				return false;
			}
		});
	}


	private void createSpinner(){
		if(spinner != null) {
			final String[] valuesArray =  {"За период","За день","За неделю", "За месяц"};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valuesArray);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
//		if(startTimeDt == null){
		startTimeDt = Functions.getStartOfDay(new Date());
//		}

//		if(endTimeDt == null){
		endTimeDt = Functions.getEndOfDay(new Date());
//		}
		startTime.setText(timeFormatter.format(startTimeDt.getTime()));
		endTime.setText(timeFormatter.format(endTimeDt.getTime()));
	}

	@Override
	public void onDatePickerDataset(Long time,String type) {
		if(type.equals("start")) {
			if(time > endTimeDt.getTime()) {
				ToastUtil.display(this,"Дата начало не может быть позднее чем дата окончания");
				return;
			}

			startTimeDt = Functions.getStartOfDay(new Date(time));
			startTime.setText(timeFormatter.format(startTimeDt.getTime()));
		}

		if(type.equals("end")) {
			if(time < startTimeDt.getTime()) {
				ToastUtil.display(this,"Дата окончания не может быть ранее чем дата начало");
				return;
			}

			endTimeDt = Functions.getEndOfDay(new Date(time));
			endTime.setText(timeFormatter.format(endTimeDt.getTime()));
		}

		setWaitScreen(true);
		BA.getEventBus().post(new AdminAnalyticsRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt) ) );
	}


	private void setSelectedPeriodDate(){
		switch (selectedPeriodType){
			case 0://period
				if(startTimeDt == null || endTimeDt == null){
					initPeriodTimes();
				}
				setWaitScreen(true);
				startTime.setText(timeFormatter.format(startTimeDt.getTime()));
				endTime.setText(timeFormatter.format(endTimeDt.getTime()));
				BA.getEventBus().post(new AdminAnalyticsRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
			case 1: //today
				initPeriodTimes();

				setWaitScreen(true);
				startTime.setText(timeFormatter.format(startTimeDt.getTime()));
				endTime.setText(timeFormatter.format(endTimeDt.getTime()));

				BA.getEventBus().post(new AdminAnalyticsRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
			case 2: //week
				setWaitScreen(true);

				//Start of the week
				Calendar c = Calendar.getInstance();
				c.setFirstDayOfWeek(Calendar.MONDAY);
				c.setTime(new Date());
				int today = c.get(Calendar.DAY_OF_WEEK);
				c.add(Calendar.DAY_OF_WEEK, -today+Calendar.MONDAY);
				c.set(Calendar.HOUR_OF_DAY, 00);
				c.set(Calendar.MINUTE, 00);
				startTimeDt = c.getTime();

				//Today
				c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				endTimeDt = c.getTime();
				startTime.setText(timeFormatter.format(startTimeDt.getTime()));
				endTime.setText(timeFormatter.format(endTimeDt.getTime()));
				BA.getEventBus().post(new AdminAnalyticsRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
			case 3: //month
				setWaitScreen(true);
				c = Calendar.getInstance();
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.set(Calendar.HOUR_OF_DAY, 00);
				c.set(Calendar.MINUTE, 00);
				startTimeDt = c.getTime();

				//Today
				c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				endTimeDt = c.getTime();

				startTime.setText(timeFormatter.format(startTimeDt.getTime()));
				endTime.setText(timeFormatter.format(endTimeDt.getTime()));
				BA.getEventBus().post(new AdminAnalyticsRequestEvent(washer.getId(),formatDate(startTimeDt),formatDate(endTimeDt)));
				break;
		}
	}

	@OnClick(R.id.startTime)
	public void startTimeClicked() {
		DatePickerFragment picker =  DatePickerFragment.newInstance("start",startTimeDt.getTime());
		picker.setDatePickerDateSetListener(this);
		picker.show(getSupportFragmentManager(), "start");
	}

	@OnClick(R.id.endTime)
	public void endTimeClicked() {
		DatePickerFragment picker =  DatePickerFragment.newInstance("end",endTimeDt.getTime());
		picker.setDatePickerDateSetListener(this);
		picker.show(getSupportFragmentManager(), "end");
	}

	private String formatDate(Date date){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		if(date != null) {
			String formattedDate = formatter.format(date.getTime());
			return formattedDate;
		}
		return formatter.format(date.getTime());
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_options, menu);
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
	public void onAnalyticsResponseReceived(AdminAnalyticsResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				statistics = event.getResult().getResponse().getStatistics();
				if(statistics != null && statistics.size() > 0){
					viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
					viewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.horizontal_margin));
					viewPager.setOffscreenPageLimit(3);
					viewPager.setCurrentItem(0);
					viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
						@Override
						public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

						}

						@Override
						public void onPageSelected(int position) {
						 tabView.setSelection(position);
						}

						@Override
						public void onPageScrollStateChanged(int state) {

						}
					});

					final AnalyticsTabBarAdapter tabViewAdapter = new AnalyticsTabBarAdapter(this, statistics);
					tabView.setAdapter(tabViewAdapter);
					tabView.setCallbackDuringFling(false);
					tabView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						public void onItemSelected(AdapterView<?> arg0, View arg1,final int position, long arg3) {
							tabViewAdapter.setSelectedTab(position);
							tabViewAdapter.notifyDataSetChanged();
							viewPager.setCurrentItem(position);
						}
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					});

				}
			}
		}
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}
	User user;
	Activity context = this;

	private class PagerAdapter extends FragmentStatePagerAdapter {

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return AdminAnalyticsFragment.init(statistics.get(position));
		}

		@Override
		public int getCount() {
			return statistics.size();
		}

	}

}
