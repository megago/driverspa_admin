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

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.api.request.AuthVerificationRequest;
import com.driverspa.model.api.request.ResendActivationRequest;
import com.driverspa.util.Converters;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.SmsRecoveryReceivedEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.ResendActivationRequestEvent;
import com.driverspa.util.otto.ws.ResendActivationResponseEvent;

public class AdminVerificationFragment extends AdminAuthBaseFragment {
	public interface ActivityActions {
		public void openAdminHomeActivity();
		public void openWizardActivity();
		public void openCreatePassword();
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
		UserPreferences.putRecoverySmsSentTime(getActivity(), System.currentTimeMillis());
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
			verificationCode.setError(BA.str(R.string.required_field));
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
			dialog.setTitle(BA.str(R.string.wait_a_bit));
			dialog.setMessage(BA.str(R.string.sms_every_30s));
			dialog.setPositiveButton(BA.str(R.string.ok_word),null);
			dialog.show();
		}
		else {
			activationTime = System.currentTimeMillis();
			setWaitScreen(true);
			// Resend the OTP over the channel the user chose (WhatsApp by default).
			String channel = UserPreferences.getOtpChannel(BA.getContext());
			BA.getEventBus().post(new ResendActivationRequestEvent(new ResendActivationRequest(phone, channel)));
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
            if(event.isPasswordRequired())
               activityActions.openCreatePassword();
            else if(handleCityRequired(event))
               return;
            else if(event.isActiveCarwash())
    		   activityActions.openAdminHomeActivity();
			else
			   activityActions.openWizardActivity();
    	  }else{
    		  ToastUtil.display(getActivity(), event.getAuthLoginResponse().getMessage());
    	   }
        }
		else
		  ToastUtil.display(getActivity(), BA.str(R.string.err_try_again));
	}	
	
	@Subscribe
	public void onResendActivationResponseReceived(ResendActivationResponseEvent event) {
		setWaitScreen(false);
		if (event.getData() == null || !"success".equals(event.getData().getStatus())) {
			ToastUtil.display(getActivity(), BA.str(R.string.err_try_again));
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