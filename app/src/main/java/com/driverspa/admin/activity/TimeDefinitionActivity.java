package com.driverspa.admin.activity;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.model.Washer.Day;
import com.driverspa.model.Washer.Schedule;
import com.driverspa.util.Functions;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class TimeDefinitionActivity extends AdminBaseActivity {

	Washer washer;
	
	@BindView(R.id.txtTitle)
	TextView textTitle;
	@BindView(R.id.comment)
	EditText comment;
	@BindView(R.id.radio_group)
	RadioGroup radioGroup;

	@BindView(R.id.allweek)
	RadioButton allWeek;
	@BindView(R.id.daytime)
	RadioButton dayTime;
	@BindView(R.id.daytime2)
	RadioButton dayTime2;
	@BindView(R.id.other)
	RadioButton other;

	@BindView(R.id.monday)
	EditText monday;	
	@BindView(R.id.tuesday)
	EditText tuesday;	
	@BindView(R.id.wednesday)
	EditText wednesday;	
	@BindView(R.id.thursday)
	EditText thursday;	
	@BindView(R.id.friday)
	EditText friday;	
	@BindView(R.id.saturday)
	EditText saturday;	
	@BindView(R.id.sunday)
	EditText sunday;		

	ArrayList<Day> mon;
	ArrayList<Day> tue;
	ArrayList<Day> wed;
	ArrayList<Day> thu;
	ArrayList<Day> fri;
	ArrayList<Day> sat;
	ArrayList<Day> sun;
	
	int mondayFromHour;
	int mondayFromMinute;
	int mondayToHour;
	int mondayToMinute;

	int tuesdayFromHour;
	int tuesdayFromMinute;
	int tuesdayToHour;
	int tuesdayToMinute;

	int wednesdayFromHour;
	int wednesdayFromMinute;
	int wednesdayToHour;
	int wednesdayToMinute;

	int thursdayFromHour;
	int thursdayFromMinute;
	int thursdayToHour;
	int thursdayToMinute;

	int fridayFromHour;
	int fridayFromMinute;
	int fridayToHour;
	int fridayToMinute;

	int saturdayFromHour;
	int saturdayFromMinute;
	int saturdayToHour;
	int saturdayToMinute;

	int sundayFromHour;
	int sundayFromMinute;
	int sundayToHour;
	int sundayToMinute;
	
	boolean isVisible = false;



    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
    	setContentView(R.layout.activity_time_definition);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

		ButterKnife.bind(this);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Время работы");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});


		other.setChecked(true);
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
				saveData();
				break;
        }
        return true;
    }	

    @Override
    public void onBackPressed() {
		saveData();
    }
    
    private void saveData(){
		mon = new ArrayList<Day>();
		mon.add(new Day());
		tue = new ArrayList<Day>();
		tue.add(new Day());
		wed = new ArrayList<Day>();
		wed.add(new Day());
		thu = new ArrayList<Day>();
		thu.add(new Day());
		fri = new ArrayList<Day>();
		fri.add(new Day());
		sat = new ArrayList<Day>();
		sat.add(new Day());
   		sun = new ArrayList<Day>();
   		sun.add(new Day());
		
    	mon.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
    	tue.get(0).setFrom(tuesdayFromHour*60+tuesdayFromMinute);
    	wed.get(0).setFrom(wednesdayFromHour*60+wednesdayFromMinute);
    	thu.get(0).setFrom(thursdayFromHour*60+thursdayFromMinute);
    	fri.get(0).setFrom(fridayFromHour * 60 + fridayFromMinute);
    	sat.get(0).setFrom(saturdayFromHour * 60 + saturdayFromMinute);
    	sun.get(0).setFrom(sundayFromHour*60+sundayFromMinute);

    	mon.get(0).setTo(mondayToHour*60+mondayToMinute);
    	tue.get(0).setTo(tuesdayToHour*60+tuesdayToMinute);
    	wed.get(0).setTo(wednesdayToHour*60+wednesdayToMinute);
    	thu.get(0).setTo(thursdayToHour*60+thursdayToMinute);
		fri.get(0).setTo(fridayToHour * 60 + fridayToMinute);
		sat.get(0).setTo(saturdayToHour * 60 + saturdayToMinute);
    	sun.get(0).setTo(sundayToHour*60+sundayToMinute);

    	washer.getSchedule().setMonday(mon);
    	washer.getSchedule().setTuesday(tue);
    	washer.getSchedule().setWednesday(wed);
    	washer.getSchedule().setThursday(thu);
    	washer.getSchedule().setFriday(fri);
    	washer.getSchedule().setSaturday(sat);
    	washer.getSchedule().setSunday(sun);
		washer.getSchedule().setComment(comment.getText().toString());

		BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
		finish();
		overridePendingTransitionWithCommonCloseTransition();

	}
    
	private void showTimePicker(final EditText targetText) {
		final Dialog dialog = new Dialog(this,android.R.style.Theme_Holo_Light_NoActionBar);
		dialog.setContentView(R.layout.dialog_time_picker);
		dialog.setTitle("Выберите интервал");
		final TimePicker timeFrom = (TimePicker) dialog.findViewById(R.id.time_from);
		timeFrom.setIs24HourView(true);
		final TimePicker timeTo = (TimePicker) dialog.findViewById(R.id.time_to);
		timeTo.setIs24HourView(true);

		switch (targetText.getId()) {
		case R.id.monday:
			timeFrom.setCurrentHour(mondayFromHour);
			timeFrom.setCurrentMinute(mondayFromMinute);
			timeTo.setCurrentHour(mondayToHour);
			timeTo.setCurrentMinute(mondayToMinute);
			break;
		case R.id.tuesday:
			timeFrom.setCurrentHour(tuesdayFromHour);
			timeFrom.setCurrentMinute(tuesdayFromMinute);
			timeTo.setCurrentHour(tuesdayToHour);
			timeTo.setCurrentMinute(tuesdayToMinute);
			break;
		case R.id.wednesday:
			timeFrom.setCurrentHour(wednesdayFromHour);
			timeFrom.setCurrentMinute(wednesdayFromMinute);
			timeTo.setCurrentHour(wednesdayToHour);
			timeTo.setCurrentMinute(wednesdayToMinute);
			break;
		case R.id.thursday:
			timeFrom.setCurrentHour(thursdayFromHour);
			timeFrom.setCurrentMinute(thursdayFromMinute);
			timeTo.setCurrentHour(thursdayToHour);
			timeTo.setCurrentMinute(thursdayToMinute);
			break;
		case R.id.friday:
			timeFrom.setCurrentHour(fridayFromHour);
			timeFrom.setCurrentMinute(fridayFromMinute);
			timeTo.setCurrentHour(fridayToHour);
			timeTo.setCurrentMinute(fridayToMinute);
			break;
		case R.id.saturday:
			timeFrom.setCurrentHour(saturdayFromHour);
			timeFrom.setCurrentMinute(saturdayFromMinute);
			timeTo.setCurrentHour(saturdayToHour);
			timeTo.setCurrentMinute(saturdayToMinute);
			break;
		case R.id.sunday:
			timeFrom.setCurrentHour(sundayFromHour);
			timeFrom.setCurrentMinute(sundayFromMinute);
			timeTo.setCurrentHour(sundayToHour);
			timeTo.setCurrentMinute(sundayToMinute);
			break;
		}

		final Button button = (Button) dialog.findViewById(R.id.ok);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				switch (targetText.getId()) {
				case R.id.monday:
					mondayFromHour = timeFrom.getCurrentHour();
					mondayFromMinute = timeFrom.getCurrentMinute();
					mondayToHour = timeTo.getCurrentHour();
					mondayToMinute = timeTo.getCurrentMinute();
					targetText.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
					break;
				case R.id.tuesday:
					tuesdayFromHour = timeFrom.getCurrentHour();
					tuesdayFromMinute = timeFrom.getCurrentMinute();
					tuesdayToHour = timeTo.getCurrentHour();
					tuesdayToMinute = timeTo.getCurrentMinute();
					targetText.setText(Functions.formatTimeInterval(tuesdayFromHour, tuesdayFromMinute, tuesdayToHour, tuesdayToMinute));
					break;
				case R.id.wednesday:
					wednesdayFromHour = timeFrom.getCurrentHour();
					wednesdayFromMinute = timeFrom.getCurrentMinute();
					wednesdayToHour = timeTo.getCurrentHour();
					wednesdayToMinute = timeTo.getCurrentMinute();
					targetText.setText(Functions.formatTimeInterval(wednesdayFromHour, wednesdayFromMinute, wednesdayToHour, wednesdayToMinute));
					break;
				case R.id.thursday:
					thursdayFromHour = timeFrom.getCurrentHour();
					thursdayFromMinute = timeFrom.getCurrentMinute();
					thursdayToHour = timeTo.getCurrentHour();
					thursdayToMinute = timeTo.getCurrentMinute();
					targetText.setText(Functions.formatTimeInterval(thursdayFromHour, thursdayFromMinute, thursdayToHour, thursdayToMinute));
					break;
				case R.id.friday:
					fridayFromHour = timeFrom.getCurrentHour();
					fridayFromMinute = timeFrom.getCurrentMinute();
					fridayToHour = timeTo.getCurrentHour();
					fridayToMinute = timeTo.getCurrentMinute();
					targetText.setText(Functions.formatTimeInterval(fridayFromHour, fridayFromMinute, fridayToHour, fridayToMinute));
					break;
				case R.id.saturday:
					saturdayFromHour = timeFrom.getCurrentHour();
					saturdayFromMinute = timeFrom.getCurrentMinute();
					saturdayToHour = timeTo.getCurrentHour();
					saturdayToMinute = timeTo.getCurrentMinute();
					targetText.setText(Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));
					break;
				case R.id.sunday:
					sundayFromHour = timeFrom.getCurrentHour();
					sundayFromMinute = timeFrom.getCurrentMinute();
					sundayToHour = timeTo.getCurrentHour();
					sundayToMinute = timeTo.getCurrentMinute();
					targetText.setText(Functions.formatTimeInterval(sundayFromHour, sundayFromMinute, sundayToHour, sundayToMinute));
					break;
				}

				dialog.dismiss();
				isVisible = false;
			}
		});		
		if(!isVisible){ 
			dialog.show();
		    isVisible = true;
		}
	}
	
	@OnClick(R.id.monday)
	public void onMondayClicked(){
		showTimePicker(monday);		
	}

	@OnClick(R.id.tuesday)
	public void onTuesdayClicked(){
		showTimePicker(tuesday);		
	}
	
	@OnClick(R.id.wednesday)
	public void onWednesdayClicked(){
		showTimePicker(wednesday);
	}

	@OnClick(R.id.thursday)
	public void onThursdayClicked(){
		showTimePicker(thursday);		
	}

	@OnClick(R.id.friday)
	public void onFridayClicked(){
		showTimePicker(friday);		
	}

	@OnClick(R.id.saturday)
	public void onSaturdayClicked(){
		showTimePicker(saturday);		
	}

	@OnClick(R.id.sunday)
	public void onSundayClicked(){
		showTimePicker(sunday);		
	}

	
	@OnFocusChange(R.id.monday)
	public void onMondayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(monday);
		}
	}

	@OnFocusChange(R.id.tuesday)
	public void onTuesdayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(tuesday);
		}
	}

	@OnFocusChange(R.id.wednesday)
	public void onWednesdayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(wednesday);
		}
	}

	@OnFocusChange(R.id.thursday)
	public void onThursdayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(thursday);
		}
	}

	@OnFocusChange(R.id.friday)
	public void onFridayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(friday);
		}
	}

	@OnFocusChange(R.id.saturday)
	public void onSaturdayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(saturday);
		}
	}

	@OnFocusChange(R.id.sunday)
	public void onSundayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(sunday);
		}
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
		if(washer != null) {
			saveData();

		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {

			if(washer.getSchedule() != null){
				Schedule schedule = washer.getSchedule();
				comment.setText(schedule.getComment()!=null?schedule.getComment():"");

				mon = schedule.getMonday();
				if(mon != null){
					mondayFromHour = mon.get(0).getFrom()/60;
					mondayFromMinute = mon.get(0).getFrom()%60;
					mondayToHour = mon.get(0).getTo()/60;
					mondayToMinute = mon.get(0).getTo()%60;
				}
				else{
					mon = new ArrayList<Day>();
					mon.add(new Day());
				}

				tue = schedule.getTuesday();
				if(tue != null){
					tuesdayFromHour = tue.get(0).getFrom()/60;
					tuesdayFromMinute = tue.get(0).getFrom()%60;
					tuesdayToHour = tue.get(0).getTo()/60;
					tuesdayToMinute = tue.get(0).getTo()%60;
				}
				else{
					tue = new ArrayList<Day>();
					tue.add(new Day());
				}

				wed = schedule.getWednesday();
				if(wed != null){
					wednesdayFromHour = wed.get(0).getFrom()/60;
					wednesdayFromMinute = wed.get(0).getFrom()%60;
					wednesdayToHour = wed.get(0).getTo()/60;
					wednesdayToMinute = wed.get(0).getTo()%60;
				}
				else{
					wed = new ArrayList<Day>();
					wed.add(new Day());
				}

				thu = schedule.getThursday();
				if(thu != null){
					thursdayFromHour = thu.get(0).getFrom()/60;
					thursdayFromMinute = thu.get(0).getFrom()%60;
					thursdayToHour = thu.get(0).getTo()/60;
					thursdayToMinute = thu.get(0).getTo()%60;
				}
				else{
					thu = new ArrayList<Day>();
					thu.add(new Day());
				}

				fri = schedule.getFriday();
				if(fri != null){
					fridayFromHour = fri.get(0).getFrom()/60;
					fridayFromMinute = fri.get(0).getFrom()%60;
					fridayToHour = fri.get(0).getTo()/60;
					fridayToMinute = fri.get(0).getTo()%60;
				}
				else{
					fri = new ArrayList<Day>();
					fri.add(new Day());
				}

				sat = schedule.getSaturday();
				if(sat != null){
					saturdayFromHour = sat.get(0).getFrom()/60;
					saturdayFromMinute = sat.get(0).getFrom()%60;
					saturdayToHour = sat.get(0).getTo()/60;
					saturdayToMinute = sat.get(0).getTo()%60;
				}
				else{
					sat = new ArrayList<Day>();
					sat.add(new Day());
				}
				sun = schedule.getSunday();
				if(sun != null){
					sundayFromHour = sun.get(0).getFrom()/60;
					sundayFromMinute = sun.get(0).getFrom()%60;
					sundayToHour = sun.get(0).getTo()/60;
					sundayToMinute = sun.get(0).getTo()%60;
				}
				else{
					sun = new ArrayList<Day>();
					sun.add(new Day());
				}

				monday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
				tuesday.setText(Functions.formatTimeInterval(tuesdayFromHour, tuesdayFromMinute, tuesdayToHour, tuesdayToMinute));
				wednesday.setText(Functions.formatTimeInterval(wednesdayFromHour, wednesdayFromMinute, wednesdayToHour, wednesdayToMinute));
				thursday.setText(Functions.formatTimeInterval(thursdayFromHour, thursdayFromMinute, thursdayToHour, thursdayToMinute));
				friday.setText(Functions.formatTimeInterval(fridayFromHour, fridayFromMinute, fridayToHour, fridayToMinute));
				saturday.setText(Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));
				sunday.setText(Functions.formatTimeInterval(sundayFromHour, sundayFromMinute, sundayToHour, sundayToMinute));
			}
		}
	}


	@OnClick(R.id.allweek)
	public void onAllWeekButtonClicked(){
		mondayFromHour = 0;
		mondayFromMinute = 0;
		mondayToHour = 23;
		mondayToMinute = 59;
		monday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		tuesdayFromHour = 0;
		tuesdayFromMinute = 0;
		tuesdayToHour = 23;
		tuesdayToMinute = 59;
		tuesday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		wednesdayFromHour = 0;
		wednesdayFromMinute = 0;
		wednesdayToHour = 23;
		wednesdayToMinute = 59;
		wednesday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		thursdayFromHour = 0;
		thursdayFromMinute = 0;
		thursdayToHour = 23;
		thursdayToMinute = 59;
		thursday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		fridayFromHour = 0;
		fridayFromMinute = 0;
		fridayToHour = 23;
		fridayToMinute = 59;
		friday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		saturdayFromHour = 0;
		saturdayFromMinute = 0;
		saturdayToHour = 23;
		saturdayToMinute = 59;
		saturday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		sundayFromHour = 0;
		sundayFromMinute = 0;
		sundayToHour = 23;
		sundayToMinute = 59;
		sunday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
	}

	@OnClick(R.id.daytime)
	public void onDayTimeButtonClicked(){
		mondayFromHour = 8;
		mondayFromMinute = 0;
		mondayToHour = 21;
		mondayToMinute = 0;
		monday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		tuesdayFromHour = 8;
		tuesdayFromMinute = 0;
		tuesdayToHour = 21;
		tuesdayToMinute = 0;
		tuesday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		wednesdayFromHour = 8;
		wednesdayFromMinute = 0;
		wednesdayToHour = 21;
		wednesdayToMinute = 0;
		wednesday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		thursdayFromHour = 8;
		thursdayFromMinute = 0;
		thursdayToHour = 21;
		thursdayToMinute = 0;
		thursday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		fridayFromHour = 8;
		fridayFromMinute = 0;
		fridayToHour = 21;
		fridayToMinute = 0;
		friday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		saturdayFromHour = 8;
		saturdayFromMinute = 0;
		saturdayToHour = 21;
		saturdayToMinute = 0;
		saturday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		sundayFromHour = 8;
		sundayFromMinute = 0;
		sundayToHour = 21;
		sundayToMinute = 0;
		sunday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
	}

	@OnClick(R.id.daytime2)
	public void onDayTime2ButtonClicked(){
		mondayFromHour = 8;
		mondayFromMinute = 0;
		mondayToHour = 23;
		mondayToMinute = 59;
		monday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		tuesdayFromHour = 8;
		tuesdayFromMinute = 0;
		tuesdayToHour = 23;
		tuesdayToMinute = 59;
		tuesday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		wednesdayFromHour = 8;
		wednesdayFromMinute = 0;
		wednesdayToHour = 23;
		wednesdayToMinute = 59;
		wednesday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		thursdayFromHour = 8;
		thursdayFromMinute = 0;
		thursdayToHour = 23;
		thursdayToMinute = 59;
		thursday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		fridayFromHour = 8;
		fridayFromMinute = 0;
		fridayToHour = 23;
		fridayToMinute = 59;
		friday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		saturdayFromHour = 8;
		saturdayFromMinute = 0;
		saturdayToHour = 23;
		saturdayToMinute = 59;
		saturday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));

		sundayFromHour = 8;
		sundayFromMinute = 0;
		sundayToHour = 23;
		sundayToMinute = 59;
		sunday.setText(Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
	}
}
