package com.driverspa.admin.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.fragment.AdminStaffFinancialFragment;
import com.driverspa.admin.fragment.AdminStaffFragment;
import com.driverspa.model.Staff;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddRequestEvent;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddResponseEvent;
import com.driverspa.util.otto.ws.AdminStaffRequestEvent;
import com.driverspa.util.otto.ws.AdminStaffResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminStaffActivity extends AdminBaseActivity implements AdminStaffFragment.ActivityActions, AdminStaffFinancialFragment.ActivityActions {

	@BindView(R.id.pager)
	ViewPager viewPager;
	PagerAdapter adapter;
	int selectedTab = 0;
	@BindView(R.id.staffInfo)
	View staffInfo;
	@BindView(R.id.staffFinancial)
	View staffFinancial;

	AdminStaffFragment staffFragment;
	AdminStaffFinancialFragment staffFinancialFragment;

	Washer washer;
	Staff staff;
	DecimalFormat formatter = new DecimalFormat("#,###.##");
	TextView titleView;

	Dialog accrualDialog;
	boolean accrualDialogOpen = true;
	Dialog paymentDialog;
	boolean paymentDialogOpen = true;
	LinkedHashMap<Integer,String> operationTypes =  BA.getReference().getOperationTypes();

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
	    Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_staff);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		ButterKnife.bind(this);

		staff = Staff.deserialize(getIntent().getStringExtra(Constants.STAFF_INFO));
		staffFragment = AdminStaffFragment.newInstance(getIntent().getStringExtra(Constants.STAFF_INFO));
		staffFinancialFragment = AdminStaffFinancialFragment.newInstance(getIntent().getStringExtra(Constants.STAFF_INFO));

		staffInfo.setSelected(true);
		adapter = new PagerAdapter(getSupportFragmentManager());

		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				selectedTab = position;
				setTabButtons(position);
			}
		});

		titleView.setText(staff.getFirstName()+" "+staff.getLastName()+" ("+formatter.format(staff.getAmount())+" ₸)");
		viewPager.setOffscreenPageLimit(3);
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(selectedTab);
	}

	private void setTabButtons(int pos){
		switch (pos){
			case 0:
				staffInfo.setSelected(true);
				staffFinancial.setSelected(false);
				break;
			case 1:
				staffInfo.setSelected(false);
				staffFinancial.setSelected(true);
				break;
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	finish();
            	overridePendingTransitionWithCommonCloseTransition();
                break;
			case R.id.menu_options:
				if(staff != null) {

					AlertDialog.Builder b = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
					b.setItems(R.array.staff_options, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							switch (i) {
								case 0:
									if(!Functions.isUserHasAccess(washer,user,context)) return;
									Intent intent = new Intent(AdminStaffActivity.this,AdminEditStaffActivity.class);
									intent.putExtra(Constants.STAFF_INFO,staff.serialize());
									startActivity(intent);
									overridePendingTransitionWithCommoOpeningTransition(AdminStaffActivity.this);
									break;
								case 1:
									if(!Functions.isUserHasAccess(washer,user,context)) return;
									accrueDialogCreate();
									break;
								case 2:
									if(!Functions.isUserHasAccess(washer,user,context)) return;
									paymentDialogCreate();
									break;
							}
						}
					});
					b.setTitle("");
//					b.setNeutralButton("Отменить", null);
					b.show();
				}
				break;

        }
        return true;
    }

	@OnClick(R.id.staffInfo)
	public void onStaffInfoButtonClick(){
		viewPager.setCurrentItem(0);
	}

	@OnClick(R.id.staffFinancial)
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
    public void onStaffResponseReceived(AdminStaffResponseEvent event){
    	setWaitScreen(false);
    	if(event.getData() != null){
    		if(BaseAssist.isSuccess(event.getData())){
			  staff = event.getData().getResponse();
			  titleView.setText(staff.getFirstName()+" "+staff.getLastName()+" ("+formatter.format(staff.getAmount())+" ₸)");
//			  titleView.setText(staff.getFirstName()+" "+staff.getLastName());
	    	}
    	}
    }

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
		  if(staff!=null){
			  BA.getEventBus().post(new AdminStaffRequestEvent(washer.getId(),staff.getUid()));
		  }
		}
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
					return  staffFragment;
				case 1:
					//Staff fragment
					return  staffFinancialFragment;
			}
			return null;
		}
		@Override
		public int getCount() {
			return 2; //No of Tabs
		}
	}


	public void accrueDialogCreate() {
		accrualDialogOpen = true;
		this.accrualDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
		Window window = accrualDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		accrualDialog.setContentView(R.layout.dialog_staff_accrual);
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		final View rootView = (FrameLayout) accrualDialog.findViewById(R.id.root_view);
		final EditText amountEditText = (EditText) accrualDialog.findViewById(R.id.amount);

		amountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					amountEditText.setText("");
				}
				else
					amountEditText.setText("0");
			}
		});
		accrualDialog.findViewById(R.id.btnAccrue).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeTransaction(1,operationTypes.get(1),amountEditText);
			}
		});

		accrualDialog.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				accrualDialog.dismiss();
			}
		});

		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
		rootView.startAnimation(fadeIn);
		this.accrualDialog.show();
		accrualDialog.setCancelable(true);
	}

	public void paymentDialogCreate() {
		paymentDialogOpen = true;
		this.paymentDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
		Window window = paymentDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		paymentDialog.setContentView(R.layout.dialog_staff_payment);
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		final View rootView = (FrameLayout) paymentDialog.findViewById(R.id.root_view);
		final EditText amountEditText = (EditText) paymentDialog.findViewById(R.id.amount);

		amountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					amountEditText.setText("");
				}
				else
					amountEditText.setText("0");
			}
		});
		paymentDialog.findViewById(R.id.btnPay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeTransaction(4,operationTypes.get(4),amountEditText);
			}
		});

		paymentDialog.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				paymentDialog.dismiss();
			}
		});

		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
		rootView.startAnimation(fadeIn);
		this.paymentDialog.show();
		paymentDialog.setCancelable(true);
	}

	private void makeTransaction(Integer operationType,String description, EditText amount){
		AdminCarwashTransactionAddRequestEvent request = new AdminCarwashTransactionAddRequestEvent();
		closeKeyboard();

		if(TextUtils.isEmpty(amount.getText().toString())){
			ToastUtil.display(this,"Введите сумму");
			amount.requestFocus();
			return;
		}

		if(Double.parseDouble(amount.getText().toString()) <= 0){
			ToastUtil.display(this,"Введите сумму выше нуля");
			amount.requestFocus();
			return;
		}

		request.setWasherId(washer.getId());
		request.setAmount(Double.parseDouble(amount.getText().toString()));
		request.setDescription(description);
		request.setOperationType(operationType);
		request.setPaymentType("C");
		request.setStaffId(staff.getUid());
		request.setCarwash(Constants.URL_PREFIX+washer.getId());
		setWaitScreen(true);
		BA.getEventBus().post(request);
	}

	@Subscribe
	public void onAdminAddOperationReceived(AdminCarwashTransactionAddResponseEvent event){
		setWaitScreen(false);
		if(accrualDialog != null)
			accrualDialog.dismiss();
		if(paymentDialog != null)
			paymentDialog.dismiss();
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())){
				if (washer != null) {
					if(staff!=null){
						BA.getEventBus().post(new AdminStaffRequestEvent(washer.getId(),staff.getUid()));
					}
				}

				ToastUtil.display(this, "Успешно провели транзакцию");
//				finish();
			}
			else{
				ToastUtil.display(this, event.getData().getMessage());
			}
		}
		else{
			ToastUtil.display(this, "Ошибка при добавлении операции");
		}
	}
}
