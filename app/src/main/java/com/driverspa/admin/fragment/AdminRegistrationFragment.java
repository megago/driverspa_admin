package com.driverspa.admin.fragment;

/**
 * @author Yerzhan
 *
 */

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.Reference;
import com.driverspa.dialog.SingleSelectDialog;
import com.driverspa.model.api.request.AuthAdminRegistrationRequest;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AuthAdminRegistrationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminRegistrationResponseEvent;

public class AdminRegistrationFragment extends AdminBaseFragment {

	public interface ActivityActions {
		public void openVerification();
		public void showChooseCity();
	}	
	private final String TAG = "RegistrationFragment";	
	private ActivityActions activityActions;
	
	@BindView(R.id.login_phone)
	EditText phone;
	@BindView(R.id.login_company_name)
	EditText companyName;
	@BindView(R.id.txtCity)
	TextView cityText;
	SingleSelectDialog cityDialog;
	String selectedCity;
	ArrayList<Reference.City> allCities = BA.getReference().getCities();
	boolean cityDialogShown = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_registration, container, false);
		ButterKnife.bind(this, view);
        try {
            MaskedWatcher maskedWatcher = new MaskedWatcher("### ###-##-##");
            phone.addTextChangedListener(maskedWatcher);
        } catch (ParseException e) {
            e.printStackTrace();
        }
		phone.requestFocus();
        phone.setOnEditorActionListener(new OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_NEXT) {
		        	processRegistrationRequest();
		            return true;
		        }
		        return false;
		    }
		});
		companyName.setText("NA");
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityActions = (ActivityActions) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		activityActions = null;
	}
	
	@OnClick(R.id.btnOk)
	protected void processRegistrationRequest() {
		String phoneStr = phone.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");
		String companyNameStr = companyName.getText().toString();
		String cityStr = cityText.getText().toString();

//		if(TextUtils.isEmpty(companyNameStr)) {
//			ToastUtil.display(getActivity(), "Введите название компании");
//			companyName.requestFocus();
//			return;
//		}
//
//
		if (TextUtils.isEmpty(selectedCity)){
			ToastUtil.displayAtTop(getActivity(), BA.str(R.string.select_city));
			return;
		}

		if(!TextUtils.isEmpty(phoneStr)) {			
			if(!isPhoneValid(phoneStr) ) {
				phone.setError(BA.str(R.string.invalid_phone));
				return;
			}
		} else {
			phone.setError(BA.str(R.string.enter_phone));
			return;
		}

		UserPreferences.putCity(getActivity(), selectedCity);
		processRegistration(companyNameStr, "+7"+phoneStr);
		
	}
	
	private void processRegistration(String companyName, String phone) {
		setWaitScreen(true);
		BA.getEventBus().post(new AuthAdminRegistrationRequestEvent(new AuthAdminRegistrationRequest(companyName, phone)));
	}
	
	
	/**
	 * Runs when user's just registration (Open Verification activity)
	 * @param event
	 */
	@Subscribe
	public void onAuthRegistrationResponseReceived(AuthAdminRegistrationResponseEvent event) {
		setWaitScreen(false);
		 if(event.getAuthLoginResponse().getStatus() != null && !event.getAuthLoginResponse().getStatus().equals("error")){			
		   activityActions.openVerification();
		 }else{
		   ToastUtil.display(getActivity(), BA.str(R.string.err_try_again));
	   }
	}
	
	/**
	 * Check if provided fields are valid
	 * @param phone - phone of user
	 * @return
	 */
	private boolean isPhoneValid(String phone) {
		if(phone.length() == 10)
		 return true;
		else
	     return false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);

		if(cityText!=null) {
			selectedCity = UserPreferences.getCity(BA.getContext());
			if(!TextUtils.isEmpty(selectedCity))
			    cityText.setText(Functions.getCityDescription(selectedCity));
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@OnClick(R.id.cityLayout)
	public void showCityDialog(){
      activityActions.showChooseCity();
	}

}
