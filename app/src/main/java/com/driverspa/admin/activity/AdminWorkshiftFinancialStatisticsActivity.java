package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminAccountDetailsAdapter;
import com.driverspa.model.CarwashAccountDetail;
import com.driverspa.model.CarwashFinancialStatistics;
import com.driverspa.model.Washer;
import com.driverspa.model.Workshift;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableListView;

public class AdminWorkshiftFinancialStatisticsActivity extends AdminBaseActivity{
	    
	@BindView(R.id.list_view)
	ExpandableListView listView;
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

	@BindView(R.id.openedDate)
	TextView openedDate;
	@BindView(R.id.closedDate)
	TextView closedDate;
	@BindView(R.id.duration)
	TextView duration;

	Washer washer;
	AdminAccountDetailsAdapter adapter;
	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	DecimalFormat decimalFormatter = new DecimalFormat("#,###.##");
	List<CarwashAccountDetail> accountDetails;
    Double cashAmount,cashAmountOpening,cashAmountEnding,cardAmount,cardAmountOpening,cardAmountEnding,bankAmount,bankAmountOpening,bankAmountEnding;

	Workshift workshift;

	CarwashFinancialStatistics statistics;
	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_workshift_financial_statistics);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Смена");
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		ButterKnife.bind(this);
		workshift = Workshift.deserialize(getIntent().getStringExtra(Constants.WORKSHIFT_INFO));

		String openedName = "";
		if(workshift.getOpenedUser() != null){
			if(!TextUtils.isEmpty(workshift.getOpenedUser().getFirstName()))
				openedName = workshift.getOpenedUser().getFirstName();
			else
				openedName = workshift.getOpenedUser().getPhone();
		}
		if(workshift.getStartTime()!=null){
			HashMap<String,String> startTime = Functions.formatUTCDate(workshift.getStartTime());
			openedDate.setText(startTime.get(Functions.DATE)+" в "+startTime.get(Functions.TIME)+ (TextUtils.isEmpty(openedName)?"":(" ("+openedName+")")));
		}

		String closedName = "";
		if(workshift.getClosedUser() != null){
			if(!TextUtils.isEmpty(workshift.getClosedUser().getFirstName()))
				closedName = workshift.getClosedUser().getFirstName();
			else
				closedName = workshift.getClosedUser().getPhone();
		}
		if(workshift.getEndTime()!=null){
			HashMap<String,String> endTime = Functions.formatUTCDate(workshift.getEndTime());
			closedDate.setText(endTime.get(Functions.DATE)+" в "+endTime.get(Functions.TIME)+ (TextUtils.isEmpty(closedName)?"":(" ("+closedName+")")));
		}

		int h = workshift.getDuration()/60;
		int m = workshift.getDuration()%60;

		duration.setText(h+" часов "+m+" минут");

		statistics = workshift.getFinancialStatistics();
		accountDetails = statistics.getAccounts();
		calculateMainScreenValues();
		if(accountDetails!= null && accountDetails.size() > 0) {
			adapter = new AdminAccountDetailsAdapter(this);
			adapter.set(accountDetails);
			listView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
		else {
			addEmptyTextView(listView, "Нет счетов");
		}

		listView.setVisibility(View.GONE);
    }

	@OnClick(R.id.transactions)
	public void transactionsClicked() {
		Intent intent = new Intent(this,AdminTransactionsActivity.class);
		intent.putExtra(AdminTransactionsActivity.FROM_WORKSHIFT,true);
		intent.putExtra(AdminTransactionsActivity.START_DATE,workshift.getStartTime().toEpochMilli());
		intent.putExtra(AdminTransactionsActivity.END_DATE,workshift.getEndTime().toEpochMilli());
		startActivity(intent);
	}

	@OnClick(R.id.statistics)
	public void statisticsClicked() {
		Intent intent = new Intent(this,AdminStatisticsActivity.class);
		intent.putExtra(AdminTransactionsActivity.FROM_WORKSHIFT,true);
		intent.putExtra(AdminTransactionsActivity.START_DATE,workshift.getStartTime().toEpochMilli());
		intent.putExtra(AdminTransactionsActivity.END_DATE,workshift.getEndTime().toEpochMilli());
		startActivity(intent);
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
	}
	
	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@OnClick(R.id.allAccounts)
	public void allAccountsClicked() {
		if (listView.getVisibility() == View.GONE) {
			listView.setVisibility(View.VISIBLE);
			allAccounts.setText("Состояние счетов при смене");
			allAccounts.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);

		} else {
			listView.setVisibility(View.GONE);
			allAccounts.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
			allAccounts.setText("Состояние счетов при смене");
		}
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

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,final int arg2,long arg3) {

				}
			});
		}
	}

}
