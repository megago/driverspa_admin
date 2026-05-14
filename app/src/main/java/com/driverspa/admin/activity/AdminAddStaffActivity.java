package com.driverspa.admin.activity;


import static com.driverspa.util.Constants.CARWASHER;
import static com.driverspa.util.Constants.DIRECTOR;
import static com.driverspa.util.Constants.OPERATOR;

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
import android.widget.EditText;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.ParseException;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminStaffAddRequestEvent;
import com.driverspa.util.otto.ws.AdminStaffAddResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;

public class AdminAddStaffActivity extends AdminBaseActivity {

	@BindView(R.id.staffName)
	TextView staffName;
	@BindView(R.id.phone)
	EditText phone;
	@BindView(R.id.salaryPercentage)
	EditText salaryPercentage;
	@BindView(R.id.staffRole)
	NDSpinner staffRole;
	@BindView(R.id.staffStatus)
	NDSpinner staffStatus;
	Washer washer;
	String selectedRole;
	String selectedStatus;
	private boolean isSpinnerTouched = false;

    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
	    Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_add_staff);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Добавление сотрудника");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
//		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onAddBoxButtonClicked();
//			}
//		});
    	ButterKnife.bind(this);

		staffRole.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isSpinnerTouched = true;
				return false;
			}
		});

		staffStatus.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isSpinnerTouched = true;
				return false;
			}
		});

		salaryPercentage.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				try {
					int value = Integer.parseInt(s.toString());
					if(value > 100){
						salaryPercentage.setText("100");
					}
				}
				catch(Exception e){}
			}
		});

		try {
			MaskedWatcher maskedWatcher = new MaskedWatcher("### ###-##-##");
			phone.addTextChangedListener(maskedWatcher);
		} catch (ParseException e) {
			e.printStackTrace();
		}
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
				closeKeyboard();
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
		BA.getEventBus().unregister(this);
	}

	@OnClick(R.id.btnSend)
	public void onAddBoxButtonClicked(){
		closeKeyboard();
		if(TextUtils.isEmpty(staffName.getText().toString())){
			ToastUtil.display(this,"Введите ФИО сотрудника");
			staffName.requestFocus();
			staffName.setError("Обязательное поле");
			return;
		}

		String phoneStr = phone.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

//		if(TextUtils.isEmpty(phoneStr)){
//			ToastUtil.display(this,"Введите номер телефона");
//			phone.requestFocus();
//			phone.setError("Обязательное поле");
//			return;
//		}

    	setWaitScreen(true);
		BA.getEventBus().post(new AdminStaffAddRequestEvent(washer.getId(), staffName.getText().toString(),"",(TextUtils.isEmpty(phoneStr)?"":"+7"+phoneStr),selectedRole,selectedStatus,Integer.parseInt(TextUtils.isEmpty(salaryPercentage.getText().toString())?"0":salaryPercentage.getText().toString())));
	}	
		
    @Subscribe
    public void onStaffResponseReceived(AdminStaffAddResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		if(BaseAssist.isSuccess(event.getData())){
				finish();
				overridePendingTransitionWithCommonCloseTransition();
				ToastUtil.display(this,"Успешно добавлен новый сотрудник");
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
			selectedRole=CARWASHER;
			selectedStatus=Constants.WORKS;
			createStaffRoleSpinner();
			createStaffStatusSpinner();
		}
	}

	private void createStaffRoleSpinner(){
		if(staffRole != null) {
				final String[] valuesArray =  {"Мойщик","Оператор","Директор"};
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
				adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
				staffRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
						if (!isSpinnerTouched) return;
						switch(pos){
							case 0:
								selectedRole = Constants.CARWASHER;
								break;
							case 1:
								selectedRole = Constants.OPERATOR;
								break;
							case 2:
								selectedRole = Constants.DIRECTOR;
								break;
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
			staffRole.setAdapter(adapter);
			isSpinnerTouched = true;
			if(DIRECTOR.equals(selectedRole))
				staffRole.setSelection(2);
			else if(OPERATOR.equals(selectedRole))
				staffRole.setSelection(1);
			else if(CARWASHER.equals(selectedRole))
				staffRole.setSelection(0);
			isSpinnerTouched = false;
		}
	}

	private void createStaffStatusSpinner(){
		if(staffStatus != null) {
			final String[] valuesArray =  {"Работает","В отпуске","Уволен"};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
			adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
			staffStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (!isSpinnerTouched) return;
					switch(pos){
						case 0:
							selectedStatus = Constants.WORKS;
							break;
						case 1:
							selectedStatus = Constants.VACATION;
							break;
						case 2:
							selectedStatus = Constants.RESIGNED;
							break;
					}
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			staffStatus.setAdapter(adapter);
			isSpinnerTouched = true;
			if(Constants.WORKS.equals(selectedStatus))
				staffStatus.setSelection(0);
			else if(Constants.VACATION.equals(selectedStatus))
				staffStatus.setSelection(1);
			else if(Constants.RESIGNED.equals(selectedStatus))
				staffStatus.setSelection(2);
			isSpinnerTouched = false;

		}
	}

}
