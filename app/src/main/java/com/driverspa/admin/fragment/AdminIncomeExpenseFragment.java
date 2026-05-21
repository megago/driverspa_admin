package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.CarwashStaffFinancialInfo;
import com.driverspa.model.NotificationType;
import com.driverspa.model.Staff;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminStaffFinancialInfoRequestEvent;
import com.driverspa.util.otto.ws.AdminStaffFinancialInfoResponseEvent;
import com.driverspa.util.otto.ws.AdminStaffRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;

public class AdminIncomeExpenseFragment extends AdminBaseFragment implements DatePickerDateSetListener {


	public static AdminIncomeExpenseFragment newInstance(String staff){
		AdminIncomeExpenseFragment fragment = new AdminIncomeExpenseFragment();
		Bundle b = new Bundle();
		b.putString(Constants.STAFF_INFO,staff);
		fragment.setArguments(b);
		return fragment;
	}

	private ActivityActions activityActions;

	public interface ActivityActions{
	}

	Washer washer;
	Staff staff;
	DecimalFormat formatter = new DecimalFormat("#,###.##");
	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
	DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(ZoneId.systemDefault());
	@BindView(R.id.startTime)
	TextView startTime;
	Instant startTimeDt;
	@BindView(R.id.endTime)
	TextView endTime;
	Instant endTimeDt;

	@BindView(R.id.spinner)
	NDSpinner spinner;
	private boolean isSpinnerTouched = false;
	private int selectedPeriodType = 0;
	CarwashStaffFinancialInfo staffFinancialInfo;

	@BindView(R.id.carCount)
	TextView carCount;
	@BindView(R.id.totalCarAmount)
	TextView totalCarAmount;
	@BindView(R.id.carAccruedSalary)
	TextView carAccruedSalary;
	@BindView(R.id.otherServicesCount)
	TextView otherServicesCount;
	@BindView(R.id.otherServicesTotalAmount)
	TextView otherServicesTotalAmount;
	@BindView(R.id.otherServicesAccruedSalary)
	TextView otherServicesAccruedSalary;

	@BindView(R.id.accruedSalary)
	TextView accruedSalary;
	@BindView(R.id.manualAccrual)
	TextView manualAccrual;
	@BindView(R.id.salaryPayment)
	TextView salaryPayment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_admin_staff_financial, container,false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if(!TextUtils.isEmpty(getArguments().getString(Constants.STAFF_INFO,"")))
			staff = Staff.deserialize(getArguments().getString(Constants.STAFF_INFO));
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);
		initPeriodTimes();
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
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.navigation_spinner_item, valuesArray);
			adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
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

	private void initPeriodTimes(){
			startTimeDt = Functions.getStartOfDay(Instant.now());
			endTimeDt = Functions.getEndOfDay(Instant.now());
		startTime.setText(timeFormatter.format(startTimeDt));
		endTime.setText(timeFormatter.format(endTimeDt));
	}

	@Override
	public void onDatePickerDataset(Long time,String type) {
		if(type.equals("start")) {
			if(time > endTimeDt.toEpochMilli()) {
				ToastUtil.display(getActivity(),"Дата начало не может быть позднее чем дата окончания");
				return;
			}

			startTimeDt = Functions.getStartOfDay(Instant.ofEpochMilli(time));
			startTime.setText(timeFormatter.format(startTimeDt));
		}

		if(type.equals("end")) {
			if(time < startTimeDt.toEpochMilli()) {
				ToastUtil.display(getActivity(),"Дата окончания не может быть ранее чем дата начало");
				return;
			}

			endTimeDt = Functions.getEndOfDay(Instant.ofEpochMilli(time));
			endTime.setText(timeFormatter.format(endTimeDt));
		}

		setWaitScreen(true);
		BA.getEventBus().post(new AdminStaffFinancialInfoRequestEvent(washer.getId(),staff.getUid(),formatDate(startTimeDt),formatDate(endTimeDt)));
	}

	@Override
	public void onDetach() {
		super.onDetach();
		context = null;
		activityActions = null;
	}

	private void setSelectedPeriodDate(){
		switch (selectedPeriodType){
			case 0://period
			if(startTimeDt == null || endTimeDt == null){
				initPeriodTimes();
			}
			    setWaitScreen(true);
			    startTime.setText(timeFormatter.format(startTimeDt));
			    endTime.setText(timeFormatter.format(endTimeDt));
			    BA.getEventBus().post(new AdminStaffFinancialInfoRequestEvent(washer.getId(),staff.getUid(),formatDate(startTimeDt),formatDate(endTimeDt)));
			break;
			case 1: //today
				initPeriodTimes();

				setWaitScreen(true);
				startTime.setText(timeFormatter.format(startTimeDt));
				endTime.setText(timeFormatter.format(endTimeDt));

				BA.getEventBus().post(new AdminStaffFinancialInfoRequestEvent(washer.getId(),staff.getUid(),formatDate(startTimeDt),formatDate(endTimeDt)));
			break;
			case 2: //week
				setWaitScreen(true);

				//Start of the week
				Calendar c = Calendar.getInstance();
				c.setFirstDayOfWeek(Calendar.MONDAY);
				c.setTimeInMillis(System.currentTimeMillis());
				int today = c.get(Calendar.DAY_OF_WEEK);
				c.add(Calendar.DAY_OF_WEEK, -today+Calendar.MONDAY);
				c.set(Calendar.HOUR_OF_DAY, 00);
				c.set(Calendar.MINUTE, 00);
				startTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());

				//Today
				c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				endTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());
				startTime.setText(timeFormatter.format(startTimeDt));
				endTime.setText(timeFormatter.format(endTimeDt));
				BA.getEventBus().post(new AdminStaffFinancialInfoRequestEvent(washer.getId(),staff.getUid(),formatDate(startTimeDt),formatDate(endTimeDt)));
			break;
			case 3: //month
				setWaitScreen(true);
				c = Calendar.getInstance();
				c.set(Calendar.DAY_OF_MONTH, 1);
				c.set(Calendar.HOUR_OF_DAY, 00);
				c.set(Calendar.MINUTE, 00);
				startTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());

				//Today
				c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				endTimeDt = Instant.ofEpochMilli(c.getTimeInMillis());

				startTime.setText(timeFormatter.format(startTimeDt));
				endTime.setText(timeFormatter.format(endTimeDt));
				BA.getEventBus().post(new AdminStaffFinancialInfoRequestEvent(washer.getId(),staff.getUid(),formatDate(startTimeDt),formatDate(endTimeDt)));
			break;
		}
	}

	private void setData(){
		if(staffFinancialInfo != null) {
			carCount.setText(staffFinancialInfo.getCarsCount()+"");
			totalCarAmount.setText(formatter.format(staffFinancialInfo.getCarsAmount()) + " ₸");
			carAccruedSalary.setText(formatter.format(staffFinancialInfo.getCarsAccrual()) + " ₸");

			otherServicesCount.setText(staffFinancialInfo.getOtherServiceCount()+"");
			otherServicesTotalAmount.setText(formatter.format(staffFinancialInfo.getOtherServiceAmount()) + " ₸");
			otherServicesAccruedSalary.setText(formatter.format(staffFinancialInfo.getOtherServiceAccrual()) + " ₸");

			accruedSalary.setText(formatter.format(staffFinancialInfo.getStaffSalaryAccrual()) + " ₸");
			manualAccrual.setText(formatter.format(staffFinancialInfo.getManualSalaryAccrual()) + " ₸");
			salaryPayment.setText(formatter.format(staffFinancialInfo.getStaffTotalPayment()) + " ₸");
		}
	}

	@Subscribe
	public void onStaffFinancialInfoResponseReceived(AdminStaffFinancialInfoResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				staffFinancialInfo = event.getResult().getResponse();
				setData();
			}
		}
	}

    @Subscribe
	public void staffRefresh(AdminStaffRequestEvent event){
		BA.getEventBus().post(new AdminStaffFinancialInfoRequestEvent(washer.getId(),staff.getUid(),formatDate(startTimeDt),formatDate(endTimeDt)));
	}

	private void loadData(Washer washer, Instant startTimeDt, Instant endTimeDt){
		setWaitScreen(true);
		String startDate = requestFormatter.format(startTimeDt);
		String endDate = requestFormatter.format(endTimeDt);
		BA.getEventBus().post(new AdminStaffFinancialInfoRequestEvent(washer.getId(),staff.getUid(),startDate,endDate));
	}

	@OnClick(R.id.startTime)
	public void startTimeClicked() {
		DatePickerFragment picker =  DatePickerFragment.newInstance("start", startTimeDt.toEpochMilli());
		picker.setDatePickerDateSetListener(this);
		picker.show(getFragmentManager(), "start");
	}

	@OnClick(R.id.endTime)
	public void endTimeClicked() {
		DatePickerFragment picker =  DatePickerFragment.newInstance("end", endTimeDt.toEpochMilli());
		picker.setDatePickerDateSetListener(this);
		picker.show(getFragmentManager(), "end");
	}

	private String formatDate(Instant instant){
		if(instant != null) {
			return requestFormatter.format(instant);
		}
		return requestFormatter.format(Instant.now());
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if (washer != null) {
			if(staff!=null){
				createSpinner();

//				BA.getEventBus().post(new AdminStaffRequestEvent(washer.getId(),staff.getUid()));
			}
		}
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}
	User user;
	Activity context;
}
