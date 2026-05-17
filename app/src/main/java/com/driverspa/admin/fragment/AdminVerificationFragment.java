package com.driverspa.admin.fragment;

/**
 * @author Yerzhan
 *
 */

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.api.request.AuthAdminRegistrationRequest;
import com.driverspa.model.api.request.AuthVerificationRequest;
import com.driverspa.util.Converters;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.SmsRecoveryReceivedEvent;
import com.driverspa.util.otto.ws.AuthAdminRegistrationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminRegistrationResponseEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;

public class AdminVerificationFragment extends AdminBaseFragment {
	public interface ActivityActions {
		public void openAdminHomeActivity();
		public void openWizardActivity();
	}
	
	private final String TAG = "VerificationFragment";	
	private ActivityActions activityActions;
	String phone = UserPreferences.getUserPhone(BA.getContext());

	public static final int TIMER_SECONDS = 30;
	private long activationTime;

	@BindView(R.id.verificationCode)
	EditText verificationCode;
	@BindView(R.id.btnNext)
	Button nextButton;
	@BindView(R.id.btnRetry)
	Button retryButton;
	@BindView(R.id.txtPhoneNo)
	TextView txtPhoneNo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_verification, container, false);
		ButterKnife.bind(this, view);
		activationTime = System.currentTimeMillis();
		UserPreferences.putRecoverySmsSentTime(getActivity(), (new Date()).getTime());
		txtPhoneNo.setText(Functions.maskPhoneNumber(phone, Converters.PHONE_PATTERN));
		
        try {
            MaskedWatcher maskedWatcher = new MaskedWatcher(Converters.PIN_PATTERN);
            verificationCode.addTextChangedListener(maskedWatcher);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        verificationCode.setOnEditorActionListener(new OnEditorActionListener() {
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_NEXT) {
		        	processVerificationRequest();
		            return true;
		        }
		        return false;
		    }
		});        

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
		
	@OnClick(R.id.btnNext)
	protected void processVerificationRequest() {
		String verificationCodeStr = verificationCode.getText().toString();
		verificationCodeStr = verificationCodeStr.replaceAll("-", "").replaceAll(" ", "");
		if( verificationCode.length() > 0) {					
			processVerify(verificationCodeStr);			
		} else {
			verificationCode.setError("Обязательное поле");
//			ToastUtil.display(getActivity(), "Введите код подтверждение");
		}		
	}
	
	@OnClick(R.id.btnRetry)
	protected void retryVerificationRequest() {

		long currentTime = System.currentTimeMillis();
		long timeDiff = currentTime - activationTime;

		int seconds = (int) (timeDiff / 1000) % 60;

		if (seconds < TIMER_SECONDS){
			AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
			dialog.setTitle("Подождите немного");
			dialog.setMessage("Мы можем отправить смс каждые 30 секунд");
			dialog.setPositiveButton("Ок",null);
			dialog.show();
		}
		else {
			activationTime = System.currentTimeMillis();
			setWaitScreen(true);
			BA.getEventBus().post(new AuthAdminRegistrationRequestEvent(new AuthAdminRegistrationRequest("  ",phone)));
		}
	}
	
	private void processVerify(String code) {
		setWaitScreen(true);		
		BA.getEventBus().post(new AuthAdminVerificationRequestEvent(new AuthVerificationRequest(phone,code)));
	}
	
	/**
	 * Runs when user's just logged in (Open home activity)
	 * @param event
	 */
	@Subscribe
	public void onAuthVerificationResponseReceived(AuthAdminVerificationResponseEvent event) {
      setWaitScreen(false);
      if(event.getAuthLoginResponse() != null){  
          if(BaseAssist.isSuccess(event.getAuthLoginResponse())){
			UserPreferences.putRecoverySmsSentTime(getActivity(), 0);
            if(event.isActiveCarwash())
    		   activityActions.openAdminHomeActivity();
			else
			   activityActions.openWizardActivity();
    	  }else{
    		  ToastUtil.display(getActivity(), event.getAuthLoginResponse().getMessage());
    	   }
        }
		else
		  ToastUtil.display(getActivity(), "Ошибка, попробуйте еще раз");
	}	
	
	@Subscribe
	public void onAuthRetryRegistrationResponseReceived(AuthAdminRegistrationResponseEvent event) {
		setWaitScreen(false);
		 if(event.getAuthLoginResponse().getStatus() != null && !event.getAuthLoginResponse().getStatus().equals("error")){			
		 }else{
		   ToastUtil.display(getActivity(), "Ошибка, попробуйте еще раз");
	   }
	}

	@Subscribe
	public void onSmsParsed(SmsRecoveryReceivedEvent event){
		verificationCode.setText(event.getCode());
		processVerify(event.getCode());
		UserPreferences.putRecoverySmsSentTime(getActivity(), 0);
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
}