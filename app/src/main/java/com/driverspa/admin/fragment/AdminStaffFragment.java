package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.driverspa.model.NotificationType;
import com.driverspa.model.Staff;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.otto.ws.AdminStaffRequestEvent;
import com.driverspa.util.otto.ws.AdminStaffResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminStaffFragment extends AdminBaseFragment{

	public static AdminStaffFragment newInstance(String staff){
		AdminStaffFragment fragment = new AdminStaffFragment();
		Bundle b = new Bundle();
		b.putString(Constants.STAFF_INFO,staff);
		fragment.setArguments(b);
		return fragment;
	}

	private ActivityActions activityActions;

	public interface ActivityActions{
	}

	@BindView(R.id.staffName)
	TextView staffName;
	@BindView(R.id.phone)
	TextView phone;
	@BindView(R.id.salaryPercentage)
	TextView salaryPercentage;
	@BindView(R.id.depositAmount)
	TextView depositAmount;
	@BindView(R.id.staffRole)
	TextView staffRole;
	@BindView(R.id.staffStatus)
	TextView staffStatus;
	Washer washer;
	Staff staff;
	DecimalFormat formatter = new DecimalFormat("#,###.##");

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_admin_staff, container,false);
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
		setData();
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

	private void setData(){
		if(staff!=null) {
//			staffName.setText(staff.getFirstName() + " " + (staff.getLastName()!=null?staff.getLastName():""));
//			phone.setText(staff.getMobile().substring(2));
			phone.setText(Functions.formatPhoneNumber("+7 ### ### ## ##",staff.getMobile()));
			staffRole.setText(Constants.staffRole.get(staff.getRole()));
			staffStatus.setText(Constants.staffStatus.get(staff.getStatus()));
			salaryPercentage.setText(formatter.format(staff.getSalaryPercentage())+"%");
			depositAmount.setText(formatter.format(staff.getAmount()) + " ₸");
		}
	}

	@Subscribe
	public void onStaffResponseReceived(AdminStaffResponseEvent event){
		setWaitScreen(false);
		if(event.getData() != null){
			if(BaseAssist.isSuccess(event.getData())){
				staff = event.getData().getResponse();
				setData();
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
	Activity context;
}
