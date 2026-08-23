package com.driverspa.admin.activity;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.L;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;
import com.driverspa.view.NDSpinner;

public class ExtraSettingsActivity extends AdminBaseActivity {

	Washer washer;
	@BindView(R.id.bonus_for_new_clients)
	EditText bonusForNewClients;
	@BindView(R.id.auto_accept_books)
	CheckBox autoAccept;
	@BindView(R.id.checkbox)
	View autoAcceptCheckBox;
	@BindView(R.id.availableAfter)
	NDSpinner availableAfterBooksSpinner;
	Integer selectedTime;
	private boolean isSpinnerTouched = false;

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
    	setContentView(R.layout.activity_extra_settings);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

		ButterKnife.bind(this);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.extra_settings));
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});

		availableAfterBooksSpinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isSpinnerTouched = true;
				return false;
			}
		});

		bonusForNewClients.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				try {
					int value = Integer.parseInt(s.toString());
					if (value > 100) {
						bonusForNewClients.setText("100");
						bonusForNewClients.setSelection(bonusForNewClients.getText().toString().length());
					}
				} catch (Exception e) {
				}
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

	@OnClick(R.id.auto_accept_layout)
	public void onAutoAccepClicked(){
		autoAccept.setChecked(!autoAccept.isChecked());
		autoAcceptCheckBox.setVisibility(autoAccept.isChecked()?View.VISIBLE:View.GONE);
	}

	private void saveData(){
		if(!TextUtils.isEmpty(bonusForNewClients.getText().toString()))
		    washer.setBonusForNewClients(Integer.parseInt(bonusForNewClients.getText().toString()));
		else
			washer.setBonusForNewClients(0);
		washer.setBookingAvailableAfterTime(selectedTime);
		L.d("fuck here "+autoAccept.isChecked());
		washer.setAutoAcceptBookings(autoAccept.isChecked());
		BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
		finish();
		overridePendingTransitionWithCommonCloseTransition();
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
			autoAccept.setChecked(washer.isAutoAcceptBookings());
			autoAcceptCheckBox.setVisibility(autoAccept.isChecked()?View.VISIBLE:View.GONE);
			bonusForNewClients.setText(washer.getBonusForNewClients()+"");
			if(washer.getBookingAvailableAfterTime() != null)
			   selectedTime = washer.getBookingAvailableAfterTime();
			else
			   selectedTime = 0;
			createAvailableAfterBooksSpinner();
		}
	}

	private void createAvailableAfterBooksSpinner(){
		if(availableAfterBooksSpinner != null) {
			final String[] valuesArray =  {BA.str(R.string.min_0), BA.str(R.string.min_30full),BA.str(R.string.hour_1),BA.str(R.string.hour_2)};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
			adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
			availableAfterBooksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (!isSpinnerTouched) return;
					switch(pos){
						case 0:
							selectedTime = 0;
							break;
						case 1:
							selectedTime = 30;
							break;
						case 2:
							selectedTime = 60;
							break;
						case 3:
							selectedTime = 120;
							break;
					}
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			availableAfterBooksSpinner.setAdapter(adapter);
			isSpinnerTouched = true;
			if(selectedTime == 0)
				availableAfterBooksSpinner.setSelection(0);
			else if(selectedTime == 30)
				availableAfterBooksSpinner.setSelection(1);
			else if(selectedTime == 60)
				availableAfterBooksSpinner.setSelection(2);
			else if(selectedTime == 120)
				availableAfterBooksSpinner.setSelection(3);
			isSpinnerTouched = false;
		}
	}
}
