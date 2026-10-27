package com.driverspa.admin.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
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
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminAddCarwashTransactionActivity;
import com.driverspa.admin.activity.AdminFinancialActivity;
import com.driverspa.model.CarwashCashFinancial;
import com.driverspa.model.NotificationType;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.otto.ws.AdminCashFinancialResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminCashFragment extends AdminBaseFragment  {

	private ActivityActions activityActions;

	public interface ActivityActions{
	}

	DecimalFormat formatter = new DecimalFormat("#,###.##");

	Washer washer;
	CarwashCashFinancial cashFinancial;

	@BindView(R.id.cashService)
	TextView cashService;
	@BindView(R.id.manualCashService)
	TextView manualCashService;
	@BindView(R.id.cashOut)
	TextView cashOut;
	@BindView(R.id.manualCashOut)
	TextView manualCashOut;
	@BindView(R.id.cashIn)
	TextView cashIn;
	@BindView(R.id.manualCashIn)
	TextView manualCashIn;

	@BindView(R.id.salaryPaid)
	TextView salaryPaid;
	@BindView(R.id.manualSalaryPaid)
	TextView manualSalaryPay;
	@BindView(R.id.goodsPaid)
	TextView goodsPaid;
	@BindView(R.id.otherRevenue)
	TextView otherRevenue;
	@BindView(R.id.manualOtherRevenue)
	TextView manualOtherRevenue;
	@BindView(R.id.otherExpense)
	TextView otherExpense;
	@BindView(R.id.manualOtherExpense)
	TextView manualOtherExpense;
	@BindView(R.id.total)
	TextView total;
	@BindView(R.id.unearnedRevenue)
	TextView unearnedRevenue;
	@BindView(R.id.goodsCash)
	TextView goodsCash;
	@BindView(R.id.goodsPurchasesCash)
	TextView goodsPurchasesCash;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_admin_cash, container,false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);

		cashService.setPaintFlags(cashService.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		cashOut.setPaintFlags(cashOut.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		cashIn.setPaintFlags(cashIn.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		salaryPaid.setPaintFlags(salaryPaid.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		otherRevenue.setPaintFlags(otherRevenue.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		unearnedRevenue.setPaintFlags(unearnedRevenue.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		otherExpense.setPaintFlags(otherExpense.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		goodsCash.setPaintFlags(goodsCash.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		goodsPurchasesCash.setPaintFlags(goodsPurchasesCash.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

		manualCashService.setPaintFlags(manualCashService.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		manualCashOut.setPaintFlags(manualCashOut.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		manualCashIn.setPaintFlags(manualCashIn.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		manualSalaryPay.setPaintFlags(manualSalaryPay.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		manualOtherRevenue.setPaintFlags(manualOtherRevenue.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
		manualOtherExpense.setPaintFlags(manualOtherExpense.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);


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

	@Subscribe
	public void onStaffCashFinancialResponseReceived(AdminCashFinancialResponseEvent event){
		if(getActivity() !=null ) ((AdminFinancialActivity)getActivity()).setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				cashFinancial = event.getResult().getResponse();
				setData();
			}
		}
	}

	public void setData(){
		if(cashFinancial != null) {
			cashService.setText(formatter.format(cashFinancial.getCashService()) + " ₸");
			cashOut.setText(formatter.format(cashFinancial.getCashOut()) + " ₸");
			cashIn.setText(formatter.format(cashFinancial.getCashIn()) + " ₸");
			salaryPaid.setText(formatter.format(cashFinancial.getSalaryPaid()) + " ₸");
//			goodsPaid.setText(formatter.format() + " ₸");
			otherRevenue.setText(formatter.format(cashFinancial.getOtherRevenue()) + " ₸");
			otherExpense.setText(formatter.format(cashFinancial.getOtherExpense()) + " ₸");
			unearnedRevenue.setText(formatter.format(cashFinancial.getUnearnedRevenue()) + " ₸");
			goodsCash.setText(formatter.format(cashFinancial.getGoodsCash()) + " ₸");
			goodsPurchasesCash.setText(formatter.format(cashFinancial.getGoodsPurchasesCash()) + " ₸");
			total.setText(formatter.format(cashFinancial.getTotalCash()) + " ₸");
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
	}

	@OnClick(R.id.cashService)
	public void cashTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("2",BA.str(R.string.service_payments_pre)+cashService.getText());
//		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,1));
	}

	@OnClick(R.id.salaryPaid)
	public void salaryPaidTransaction(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("4",BA.str(R.string.salary_payout_pre)+salaryPaid.getText());
//		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,3));
	}
	@OnClick(R.id.cashIn)
	public void cashInTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("10",BA.str(R.string.cashbox_deposit_pre)+cashIn.getText());
//		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,9));
	}
	@OnClick(R.id.cashOut)
	public void cashOutTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("11",BA.str(R.string.collection_pre)+cashOut.getText());
//		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,10));
	}

	@OnClick(R.id.otherExpense)
	public void otherExpenseTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("8,9",BA.str(R.string.other_expenses_pre)+otherExpense.getText());
//		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,8));
	}

	@OnClick(R.id.otherRevenue)
	public void otherRevenueTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("3",BA.str(R.string.other_incomes_pre)+otherRevenue.getText());
//		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,2));
	}

	@OnClick(R.id.unearnedRevenue)
	public void unearnedRevenueTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("5,6,7,12",BA.str(R.string.unrecognized_income_pre)+unearnedRevenue.getText());
//		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,2));
	}


	/** The shop's cash: sales less canceled ones (ops 15 and 18). */
	@OnClick(R.id.goodsCash)
	public void goodsCashTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("15,18","1000",BA.str(R.string.shop_cash_pre)+goodsCash.getText());
	}

	/** Goods purchases (op 16). */
	@OnClick(R.id.goodsPurchasesCash)
	public void goodsPurchasesTransactions(){
		((AdminFinancialActivity)(getActivity())).transactionsHistoryClicked("16","1000",BA.str(R.string.goods_purchases_pre)+goodsPurchasesCash.getText());
	}

	@OnClick(R.id.manualCashService)
	public void manualCash(){
		if(!Functions.isUserHasAccess(washer,user,context)) return;
		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,1));
	}

	@OnClick(R.id.manualSalaryPaid)
	public void manualSalaryPayment(){
		if(!Functions.isUserHasAccess(washer,user,context)) return;
		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,3));
	}
	@OnClick(R.id.manualCashIn)
	public void manualCashIn(){
		if(!Functions.isUserHasAccess(washer,user,context)) return;
		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,9));
	}
	@OnClick(R.id.manualCashOut)
	public void manualCashOut(){
		if(!Functions.isUserHasAccess(washer,user,context)) return;
		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,10));
	}

	@OnClick(R.id.manualOtherExpense)
	public void manualOtherExpense(){
		if(!Functions.isUserHasAccess(washer,user,context)) return;
		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,8));
	}

	@OnClick(R.id.manualOtherRevenue)
	public void manualOtherRevenue(){
		if(!Functions.isUserHasAccess(washer,user,context)) return;
		startActivity(new Intent(getActivity(), AdminAddCarwashTransactionActivity.class).putExtra(AdminAddCarwashTransactionActivity.TRANSACTION_CODE,2));
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}
	User user;
	Activity context;
}
