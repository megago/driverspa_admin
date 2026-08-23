package com.driverspa.admin.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminAccountDetailsAdapter;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.CarwashAccountDetail;
import com.driverspa.model.CarwashFinancialStatistics;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddRequestEvent;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddResponseEvent;
import com.driverspa.util.otto.ws.AdminFinancialStatisticsRequestEvent;
import com.driverspa.util.otto.ws.AdminFinancialStatisticsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableListView;

public class AdminFinancialStatisticsActivity extends AdminBaseActivity implements DatePickerDateSetListener{
	    
	@BindView(R.id.list_view)
	ExpandableListView listView;
	@BindView(R.id.startTime)
	TextView startTime;
	Instant startTimeDt;
	@BindView(R.id.cash)
	TextView cash;
	@BindView(R.id.cashStart)
	TextView cashStart;
	@BindView(R.id.cashEnd)
	TextView cashEnd;
	@BindView(R.id.card)
	TextView card;
	@BindView(R.id.cardStart)
	TextView cardStart;
	@BindView(R.id.cardEnd)
	TextView cardEnd;

	@BindView(R.id.bank)
	TextView bank;
	@BindView(R.id.bankStart)
	TextView bankStart;
	@BindView(R.id.bankEnd)
	TextView bankEnd;
	@BindView(R.id.expense)
	TextView expense;
	@BindView(R.id.expenseStart)
	TextView expenseStart;
	@BindView(R.id.expenseEnd)
	TextView expenseEnd;
	@BindView(R.id.revenue)
	TextView revenue;
	@BindView(R.id.revenueStart)
	TextView revenueStart;
	@BindView(R.id.revenueEnd)
	TextView revenueEnd;
	@BindView(R.id.profit)
	TextView profit;
	@BindView(R.id.allAccounts)
	TextView allAccounts;
	@BindView(R.id.cashInOutButton)
	TextView cashInOutButton;

	Washer washer;
	AdminAccountDetailsAdapter adapter;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
	DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(ZoneId.systemDefault());
	DecimalFormat decimalFormatter = new DecimalFormat("#,###.##");
	List<CarwashAccountDetail> accountDetails;
    Double cashAmount,cashAmountOpening,cashAmountEnding,cardAmount,cardAmountOpening,cardAmountEnding,bankAmount,bankAmountOpening,bankAmountEnding;
	LinkedHashMap<Integer,String> operationTypes =  BA.getReference().getOperationTypes();
	CarwashFinancialStatistics statistics;

	Dialog cashInOutDialog;
	boolean cashInOutDialogOpen = true;

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_financial_statistics);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.finance));
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		ButterKnife.bind(this);
		listView.setVisibility(View.GONE);
		startTimeDt = Functions.getStartOfDay(Instant.now());
		startTime.setText(formatter.format(startTimeDt));
		startTime.setPaintFlags(startTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		cashInOutButton.setPaintFlags(cashInOutButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }
    
    @Subscribe
    public void onAdminFinancialStatisticsReceived(AdminFinancialStatisticsResponseEvent event){
    	setWaitScreen(false);
    	if(event.getResult() != null){
			statistics = event.getResult().getResponse();
			accountDetails = statistics.getAccounts();
			calculateMainScreenValues();
			if(accountDetails!= null && accountDetails.size() > 0) {
				adapter = new AdminAccountDetailsAdapter(this);
				adapter.set(accountDetails);
				listView.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
			else {
				addEmptyTextView(listView, BA.str(R.string.no_accounts));
			}
    	}
    	else {
    		addEmptyTextView(listView, BA.str(R.string.no_accounts));
    	}
    }

	@Override
	public void onDatePickerDataset(Long time,String type) {
		startTimeDt = Functions.getStartOfDay(Instant.ofEpochMilli(time));
		startTime.setText(formatter.format(startTimeDt));
		if(washer != null && startTimeDt != null)
		  loadData(washer,startTimeDt);
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
		setWaitScreen(true);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@OnClick(R.id.startTime)
	public void startTimeClicked() {
		DatePickerFragment picker =  DatePickerFragment.newInstance(startTimeDt.toEpochMilli());
		picker.setDatePickerDateSetListener(this);
		picker.show(getSupportFragmentManager(), "datePicker");
	}

	@OnClick(R.id.transactions)
	public void transactionsClicked() {
	  Intent intent = new Intent(this,AdminTransactionsActivity.class);
	  startActivity(intent);
	}

	@OnClick(R.id.allAccounts)
	public void allAccountsClicked() {
		if (listView.getVisibility() == View.GONE) {
			listView.setVisibility(View.VISIBLE);
			allAccounts.setText(BA.str(R.string.all_accounts));
			allAccounts.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
		} else {
			listView.setVisibility(View.GONE);
			allAccounts.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
			allAccounts.setText(BA.str(R.string.all_accounts));
		}
	}

	@OnClick(R.id.cashInOutButton)
	public void cashInOutButtonClicked() {
		cashInOutDialogCreate();
	}

	public void cashInOutDialogCreate() {
		cashInOutDialogOpen = true;
		this.cashInOutDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
		Window window = cashInOutDialog.getWindow();
		window.setGravity(Gravity.CENTER);
		cashInOutDialog.setContentView(R.layout.dialog_cash_inout);
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

		final View rootView = (FrameLayout) cashInOutDialog.findViewById(R.id.root_view);
		final EditText amountEditText = (EditText) cashInOutDialog.findViewById(R.id.amount);

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
		cashInOutDialog.findViewById(R.id.btnPop).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeCashInOutTransaction(11,operationTypes.get(11),amountEditText);
			}
		});
		cashInOutDialog.findViewById(R.id.btnPush).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				makeCashInOutTransaction(10,operationTypes.get(10),amountEditText);
			}
		});

		cashInOutDialog.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cashInOutDialog.dismiss();
			}
		});

		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
		rootView.startAnimation(fadeIn);
		this.cashInOutDialog.show();
		cashInOutDialog.setCancelable(true);
	}

	private void calculateMainScreenValues(){

		if(accountDetails!=null && accountDetails.size() > 0){
			for(CarwashAccountDetail acc:accountDetails){
				switch(acc.getAccountCode()){
					case 1000:
						cashAmount = acc.getAmount();
						cashAmountOpening = acc.getOpeningAmount();
						cashAmountEnding = acc.getEndingAmount();
						break;
					case 1001:
						cardAmount = acc.getAmount();
						cardAmountOpening = acc.getOpeningAmount();
						cardAmountEnding = acc.getEndingAmount();
						break;
					case 1002:
						bankAmount = acc.getAmount();
						bankAmountOpening = acc.getOpeningAmount();
						bankAmountEnding = acc.getEndingAmount();
						break;
				}
			}

			cash.setText(decimalFormatter.format(cashAmount));
			cashStart.setText(decimalFormatter.format(cashAmountOpening));
			cashEnd.setText(decimalFormatter.format(cashAmountEnding));
			card.setText(decimalFormatter.format(cardAmount));
			cardStart.setText(decimalFormatter.format(cardAmountOpening));
			cardEnd.setText(decimalFormatter.format(cardAmountEnding));
			bank.setText(decimalFormatter.format(bankAmount));
			bankStart.setText(decimalFormatter.format(bankAmountOpening));
			bankEnd.setText(decimalFormatter.format(bankAmountEnding));

			expense.setText(decimalFormatter.format(statistics.getExpense()));
			expenseStart.setText(decimalFormatter.format(statistics.getExpenseOpening()));
			expenseEnd.setText(decimalFormatter.format(statistics.getExpenseEnding()));
			revenue.setText(decimalFormatter.format(statistics.getRevenue()));
			revenueStart.setText(decimalFormatter.format(statistics.getRevenueOpening()));
			revenueEnd.setText(decimalFormatter.format(statistics.getRevenueEnding()));
			profit.setText(decimalFormatter.format(statistics.getNetProfit())+" ₸");
		}

	}

	private void loadData(Washer washer, Instant startTimeDt){
		setWaitScreen(true);
		String formattedDate = requestFormatter.format(startTimeDt);
		BA.getEventBus().post(new AdminFinancialStatisticsRequestEvent(washer.getId(),formattedDate));
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			setWaitScreen(true);
			loadData(washer,startTimeDt);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,final int arg2,long arg3) {

				}
			});
		}
	}


	private void makeCashInOutTransaction(Integer operationType,String description, EditText amount){
		   AdminCarwashTransactionAddRequestEvent request = new AdminCarwashTransactionAddRequestEvent();
			closeKeyboard();

			if(TextUtils.isEmpty(amount.getText().toString())){
				ToastUtil.display(this,BA.str(R.string.enter_amount));
				amount.requestFocus();
				return;
			}

			if(Double.parseDouble(amount.getText().toString()) <= 0){
				ToastUtil.display(this,BA.str(R.string.enter_amount_above_zero));
				amount.requestFocus();
				return;
			}

			request.setWasherId(washer.getId());
			request.setAmount(Double.parseDouble(amount.getText().toString()));
			request.setDescription(description);
			request.setOperationType(operationType);
			request.setPaymentType("C");
			request.setCarwash(Constants.URL_PREFIX+washer.getId());
			setWaitScreen(true);
			BA.getEventBus().post(request);
	}


	@Subscribe
	public void onAdminAddOperationReceived(AdminCarwashTransactionAddResponseEvent event){
		setWaitScreen(false);
		if(cashInOutDialog!=null) cashInOutDialog.dismiss();
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())){
				loadData(washer,startTimeDt);
				ToastUtil.display(this, BA.str(R.string.collection_ok));
			}
			else{
				ToastUtil.display(this, event.getData().getMessage());
			}
		}
		else{
			ToastUtil.display(this, BA.str(R.string.err_collection));
		}
	}

}
