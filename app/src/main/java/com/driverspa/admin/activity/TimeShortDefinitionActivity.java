package com.driverspa.admin.activity;

import android.app.Dialog;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.model.Washer;
import com.driverspa.model.Washer.Day;
import com.driverspa.model.Washer.Schedule;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class TimeShortDefinitionActivity extends AdminBaseActivity {

	Washer washer;
	@BindView(R.id.comment)
	EditText comment;

	@BindView(R.id.time)
	TextView monday;

	@BindView(R.id.weekendTime)
	TextView saturday;

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

	MultipleSelectDialog paymentDialog;
	MultipleSelectDialog addInfoDialog;

	@BindView(R.id.phone)
	EditText phone;
	@BindView(R.id.email)
	EditText email;
	@BindView(R.id.web)
	EditText web;

	@BindView(R.id.txtPayType)
	TextView payType;
	@BindView(R.id.txtAddInfo)
	TextView addInfo;
	ArrayList<Washer.Contact> contacts;


	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
    	setContentView(R.layout.activity_time_short_definition);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

		ButterKnife.bind(this);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.work_hours_info_short));
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});
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

	@OnClick(R.id.addinfo)
	public void onAddInfoClicked(){
		addInfoDialog.show();
	}

	@OnClick(R.id.payType)
	public void onPayTypeClicked(){
		paymentDialog.show();
	}


	private void saveData(){

		contacts = new ArrayList<Washer.Contact>();
		Washer.Contact phoneContact = new Washer.Contact();
		phoneContact.setType("phone");
		phoneContact.setValue(phone.getText().toString());
		contacts.add(phoneContact);
		Washer.Contact webContact = new Washer.Contact();
		webContact.setType("website");
		webContact.setValue(web.getText().toString());
		contacts.add(webContact);
		Washer.Contact emailContact = new Washer.Contact();
		emailContact.setType("email");
		emailContact.setValue(email.getText().toString());
		contacts.add(emailContact);
		washer.setContacts(contacts);

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
    	tue.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
    	wed.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
    	thu.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
    	fri.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
    	sat.get(0).setFrom(saturdayFromHour*60+saturdayFromMinute);
    	sun.get(0).setFrom(saturdayFromHour*60+saturdayFromMinute);

    	mon.get(0).setTo(mondayToHour*60+mondayToMinute);
    	tue.get(0).setTo(mondayToHour*60+mondayToMinute);
    	wed.get(0).setTo(mondayToHour*60+mondayToMinute);
    	thu.get(0).setTo(mondayToHour*60+mondayToMinute);
		fri.get(0).setTo(mondayToHour*60+mondayToMinute);
		sat.get(0).setTo(saturdayToHour*60+saturdayToMinute);
    	sun.get(0).setTo(saturdayToHour*60+saturdayToMinute);

    	washer.getSchedule().setMonday(mon);
    	washer.getSchedule().setTuesday(tue);
    	washer.getSchedule().setWednesday(wed);
    	washer.getSchedule().setThursday(thu);
    	washer.getSchedule().setFriday(fri);
    	washer.getSchedule().setSaturday(sat);
    	washer.getSchedule().setSunday(sun);
		washer.getSchedule().setComment(comment.getText().toString());

		if(mondayFromHour*60+mondayFromMinute >= mondayToHour*60+mondayToMinute){
			ToastUtil.display(TimeShortDefinitionActivity.this,BA.str(R.string.err_start_after_end));
			mondayToHour = 23;
			mondayToMinute = 59;
			return;
		}

		if(saturdayFromHour*60+saturdayFromMinute >= saturdayToHour*60+saturdayToMinute){
			ToastUtil.display(TimeShortDefinitionActivity.this,BA.str(R.string.err_start_after_end));
			saturdayToHour = 23;
			saturdayToMinute = 59;
			return;
		}

		BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
		finish();
		overridePendingTransitionWithCommonCloseTransition();
	}
    
	private void showTimePicker(final TextView targetText) {
		final Dialog dialog = new Dialog(this, R.style.DialogWithNoActionBar);
		dialog.setContentView(R.layout.dialog_time_picker);
		dialog.setTitle(BA.str(R.string.select_interval));
		final TimePicker timeFrom = (TimePicker) dialog.findViewById(R.id.time_from);
		timeFrom.setIs24HourView(true);

		timeFrom.setCurrentHour(mondayFromHour);
		timeFrom.setCurrentMinute(mondayFromMinute);
		final TimePicker timeTo = (TimePicker) dialog.findViewById(R.id.time_to);
		timeTo.setIs24HourView(true);
		timeTo.setCurrentHour(mondayToHour);
		timeTo.setCurrentMinute(mondayToMinute);

		switch (targetText.getId()) {
		case R.id.time:
			timeFrom.setCurrentHour(mondayFromHour);
			timeFrom.setCurrentMinute(mondayFromMinute);
			timeTo.setCurrentHour(mondayToHour);
			timeTo.setCurrentMinute(mondayToMinute);
			break;
			case R.id.weekendTime:
			timeFrom.setCurrentHour(saturdayFromHour);
			timeFrom.setCurrentMinute(saturdayFromMinute);
			timeTo.setCurrentHour(saturdayToHour);
			timeTo.setCurrentMinute(saturdayToMinute);
			break;
		}

		final Button button = (Button) dialog.findViewById(R.id.ok);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (targetText.getId()) {
				case R.id.time:
					mondayFromHour = timeFrom.getCurrentHour();
					mondayFromMinute = timeFrom.getCurrentMinute();
					mondayToHour = timeTo.getCurrentHour();
					mondayToMinute = timeTo.getCurrentMinute();

					if(mondayToHour == 0 && mondayToMinute == 0){
						mondayToHour = 23;
						mondayToMinute = 59;

					}
					if(mondayFromHour*60+mondayFromMinute >= mondayToHour*60+mondayToMinute){
						ToastUtil.display(TimeShortDefinitionActivity.this,BA.str(R.string.err_start_after_end));
						mondayToHour = 23;
						mondayToMinute = 59;
					}

					targetText.setText(BA.str(R.string.work_hours_weekday_pre1)+Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
					break;

					case R.id.weekendTime:
					saturdayFromHour = timeFrom.getCurrentHour();
					saturdayFromMinute = timeFrom.getCurrentMinute();
					saturdayToHour = timeTo.getCurrentHour();
					saturdayToMinute = timeTo.getCurrentMinute();

					if(saturdayToHour == 0 && saturdayToMinute == 0){
						saturdayToHour = 23;
						saturdayToMinute = 59;
					}

					if(saturdayFromHour*60+saturdayFromMinute >= saturdayToHour*60+saturdayToMinute){
						ToastUtil.display(TimeShortDefinitionActivity.this,BA.str(R.string.err_start_after_end));
						saturdayToHour = 23;
						saturdayToMinute = 59;
					}
					targetText.setText(BA.str(R.string.work_hours_weekend_pre)+Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));
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
	
	@OnClick(R.id.time)
	public void onMondayClicked(){
		showTimePicker(monday);		
	}

	@OnClick(R.id.weekendTime)
	public void onWeekEndClicked(){
		showTimePicker(saturday);
	}

	@OnFocusChange(R.id.time)
	public void onMondayFocusChange(View view, boolean hasFocus){
		if(hasFocus){
			showTimePicker(monday);
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

				monday.setText(BA.str(R.string.work_hours_weekday_pre2)+Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
				saturday.setText(BA.str(R.string.work_hours_weekend_pre)+Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));


				if(washer.getPayOptions() != null && washer.getPayOptions().size() > 0){
					if(washer.getPayOptions().size() == 1) payType.setText(washer.getPayOptions().get(0).toString());
					else payType.setText(BA.str(R.string.selected_paren)+washer.getPayOptions().size()+BA.str(R.string.paren_payment_type));
				}

				if(washer.getAdditionalInfo() != null && washer.getAdditionalInfo().size() > 0){
					if(washer.getAdditionalInfo().size() == 1) addInfo.setText(washer.getAdditionalInfo().get(0));
					else addInfo.setText(BA.str(R.string.selected_paren)+washer.getAdditionalInfo().size()+BA.str(R.string.paren_additional_info));
				}

				contacts = washer.getContacts();
				if(contacts != null && contacts.size() > 0){
					for(Washer.Contact contact : contacts){
						if(contact.getType().equals("phone")){
							phone.setText(contact.getValue());
						}
						else if(contact.getType().equals("website")){
							web.setText(contact.getValue());
						}
						else if(contact.getType().equals("email")){
							email.setText(contact.getValue());
						}
					}
				}
				else{
					contacts = new ArrayList<Washer.Contact>();
					Washer.Contact phoneContact = new Washer.Contact();
					phoneContact.setType("phone");
					phoneContact.setValue("");
					contacts.add(phoneContact);
					Washer.Contact webContact = new Washer.Contact();
					webContact.setType("website");
					webContact.setValue("");
					contacts.add(webContact);
					Washer.Contact emailContact = new Washer.Contact();
					emailContact.setType("email");
					emailContact.setValue("");
					contacts.add(emailContact);
				}

				//Pay Options
				final HashMap<String,String> allPayTypes = BA.getReference().getPayoptions();
				List<HashMap<String,String>> listPayTypes = new ArrayList<HashMap<String,String>>();
				List<HashMap<String,String>> selectedPayTypes = new ArrayList<HashMap<String,String>>();

				for (Map.Entry<String,String> entry : allPayTypes.entrySet()) {
					HashMap<String,String> payMap = new HashMap<String,String>();
					String key = entry.getKey();
					String value = entry.getValue();
					payMap.put(MultipleSelectDialog.ID, key);
					payMap.put(MultipleSelectDialog.NAME, value);
					listPayTypes.add(payMap);
				}

				if(washer.getPayOptions() != null){
					for(int i = 0; i < washer.getPayOptions().size(); i++){
						HashMap<String,String> payMap = new HashMap<String,String>();
						payMap.put(MultipleSelectDialog.ID, washer.getPayOptions().get(i));
						payMap.put(MultipleSelectDialog.NAME, allPayTypes.get(washer.getPayOptions().get(i)));
						selectedPayTypes.add(payMap);
					}
				}

				final ArrayList<String> requestPayTypes = new ArrayList<String>();
				paymentDialog = new MultipleSelectDialog(this, listPayTypes, BA.str(R.string.select_payment_type), selectedPayTypes) {
					@Override
					public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

						if(namesSelectedItm.size() == 0){
							payType.setText(BA.str(R.string.payment_type));
						}
						else if(namesSelectedItm.size() == 1){
							requestPayTypes.add(namesSelectedItm.get(0));
							payType.setText(allPayTypes.get(namesSelectedItm.get(0).toString()));
						}
						else{
							payType.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_payment_type));
							for(int i = 0; i < namesSelectedItm.size(); i++){
								requestPayTypes.add(namesSelectedItm.get(i).toString());
							}
						}
						washer.setPayOptions(new ArrayList<String>(requestPayTypes));
						requestPayTypes.clear();
					}
				};

				//Additional Information
				final HashMap<String,String> allAddInfo = BA.getReference().getAdditionalInfo();
				List<HashMap<String,String>> listAddInfos = new ArrayList<HashMap<String,String>>();
				List<HashMap<String,String>> selectedAddInfos = new ArrayList<HashMap<String,String>>();

				for (Map.Entry<String,String> entry : allAddInfo.entrySet()) {
					HashMap<String,String> addInfoMap = new HashMap<String,String>();
					String key = entry.getKey();
					String value = entry.getValue();
					addInfoMap.put(MultipleSelectDialog.ID, key);
					addInfoMap.put(MultipleSelectDialog.NAME, value);
					listAddInfos.add(addInfoMap);
				}

				if(washer.getAdditionalInfo() != null){
					for(int i = 0; i < washer.getAdditionalInfo().size(); i++){
						HashMap<String,String> addInfoMap = new HashMap<String,String>();
						addInfoMap.put(MultipleSelectDialog.ID, washer.getAdditionalInfo().get(i));
						addInfoMap.put(MultipleSelectDialog.NAME, allAddInfo.get(washer.getAdditionalInfo().get(i)));
						selectedAddInfos.add(addInfoMap);
					}
				}

				final ArrayList<String> requestAddInfoTypes = new ArrayList<String>();
				addInfoDialog = new MultipleSelectDialog(this, listAddInfos, BA.str(R.string.select_additional_info), selectedAddInfos) {
					@Override
					public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

						if(namesSelectedItm.size() == 0){
							addInfo.setText(BA.str(R.string.additional_info));
						}
						else if(namesSelectedItm.size() == 1){
							requestAddInfoTypes.add(namesSelectedItm.get(0));
							addInfo.setText(allPayTypes.get(namesSelectedItm.get(0)));
						}
						else{
							addInfo.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_additional_info));
							for(int i = 0; i < namesSelectedItm.size(); i++){
								requestAddInfoTypes.add(namesSelectedItm.get(i));
							}
						}
						washer.setAdditionalInfo(new ArrayList<String>(requestAddInfoTypes));
						requestAddInfoTypes.clear();
					}
				};

			}
		}
	}

}
