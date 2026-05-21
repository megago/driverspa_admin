package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.CarwashCashFinancial;
import com.driverspa.model.NotificationType;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.otto.ws.AdminCashFinancialResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminMoneyFragment extends AdminBaseFragment {

	private ActivityActions activityActions;

	public interface ActivityActions{
	}

	Washer washer;
	DecimalFormat formatter = new DecimalFormat("#,###.##");

	private boolean isSpinnerTouched = false;
	private int selectedPeriodType = 0;
	CarwashCashFinancial cashFinancial;;

	@BindView(R.id.serviceRevenue)
	TextView serviceRevenue;
	@BindView(R.id.goodsRevenue)
	TextView goodsRevenue;
	@BindView(R.id.otherRevenue)
	TextView otherRevenue;
	@BindView(R.id.unearnedRevenue)
	TextView unearnedRevenue;
	@BindView(R.id.totalRevenue)
	TextView totalRevenue;
	@BindView(R.id.salaryExpense)
	TextView salaryExpense;
	@BindView(R.id.otherExpense)
	TextView otherExpense;
	@BindView(R.id.totalExpense)
	TextView totalExpense;
	@BindView(R.id.income)
	TextView income;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_admin_money, container,false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
		BA.setNotificationType(NotificationType.NOTHING);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = getActivity();
		activityActions = (ActivityActions) activity;
	}


	@Override
	public void onDetach() {
		super.onDetach();
		context = null;
		activityActions = null;
	}


	public void setData(){

		if(cashFinancial != null) {
			serviceRevenue.setText(formatter.format(cashFinancial.getServiceRevenue()) + " ₸");
//			goodsRevenue.setText(formatter.format(cashFinancial.()) + " ₸");
			otherRevenue.setText(formatter.format(cashFinancial.getOtherRevenue()) + " ₸");
			unearnedRevenue.setText(formatter.format(cashFinancial.getUnearnedRevenue()) + " ₸");
			totalRevenue.setText(formatter.format(cashFinancial.getTotalRevenue()) + " ₸");
			salaryExpense.setText(formatter.format(cashFinancial.getSalaryExpense()) + " ₸");
			otherExpense.setText(formatter.format(cashFinancial.getOtherExpense()) + " ₸");

			otherExpense.setText(formatter.format(cashFinancial.getOtherExpense()) + " ₸");
			totalExpense.setText(formatter.format(cashFinancial.getTotalExpense()) + " ₸");
			income.setText(formatter.format(cashFinancial.getNetIncome()) + " ₸");
//			total.setText(formatter.format(cashFinancial.getto()) + " ₸");
		}

	}

	@Subscribe
	public void onStaffCashFinancialResponseReceived(AdminCashFinancialResponseEvent event){
//		((AdminFinancialActivity)getActivity()).setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				cashFinancial = event.getResult().getResponse();
				setData();
			}
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}
	User user;
	Activity context;
}
