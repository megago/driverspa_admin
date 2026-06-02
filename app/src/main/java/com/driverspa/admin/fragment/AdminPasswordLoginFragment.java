package com.driverspa.admin.fragment;

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

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.api.request.ForgotPasswordRequest;
import com.driverspa.model.api.request.LoginPasswordRequest;
import com.driverspa.model.api.request.ResendActivationRequest;
import com.driverspa.util.Converters;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.ForgotPasswordRequestEvent;
import com.driverspa.util.otto.ws.ForgotPasswordResponseEvent;
import com.driverspa.util.otto.ws.LoginPasswordRequestEvent;
import com.driverspa.util.otto.ws.ResendActivationRequestEvent;
import com.driverspa.util.otto.ws.ResendActivationResponseEvent;

/**
 * Password login for returning users (check_phone -> has_password == true).
 */
public class AdminPasswordLoginFragment extends AdminBaseFragment {

	public static final String ARG_PHONE = "ARG_PHONE";
	// Server message that means "phone known but no password yet" -> OTP flow.
	private static final String MSG_PASSWORD_NOT_SET = "password not set, please activate via OTP first";

	public interface ActivityActions {
		void openVerification();
		void openResetPassword(String phone);
		void openAdminHomeActivity();
		void openWizardActivity();
	}

	private ActivityActions activityActions;
	private String phoneNumber;
	private boolean redirectingToOtp = false;

	@BindView(R.id.password)
	EditText password;
	@BindView(R.id.txtPhoneNo)
	TextView txtPhoneNo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_password_login, container, false);
		ButterKnife.bind(this, view);
		if (getArguments() != null) {
			phoneNumber = getArguments().getString(ARG_PHONE);
		}
		txtPhoneNo.setText(Functions.maskPhoneNumber(phoneNumber, Converters.PHONE_PATTERN));
		password.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					processLogin();
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

	@OnClick(R.id.btnLogin)
	protected void processLogin() {
		String pass = password.getText().toString();
		if (TextUtils.isEmpty(pass)) {
			password.setError("Введите пароль");
			return;
		}
		setWaitScreen(true);
		BA.getEventBus().post(new LoginPasswordRequestEvent(new LoginPasswordRequest(phoneNumber, pass)));
	}

	@OnClick(R.id.btnForgot)
	protected void onForgotClicked() {
		setWaitScreen(true);
		String channel = UserPreferences.getOtpChannel(BA.getContext());
		BA.getEventBus().post(new ForgotPasswordRequestEvent(new ForgotPasswordRequest(phoneNumber, channel)));
	}

	@Subscribe
	public void onLoginResponse(AuthAdminVerificationResponseEvent event) {
		setWaitScreen(false);
		if (event.getAuthLoginResponse() != null) {
			if (BaseAssist.isSuccess(event.getAuthLoginResponse())) {
				if (event.isActiveCarwash())
					activityActions.openAdminHomeActivity();
				else
					activityActions.openWizardActivity();
			} else {
				String message = event.getAuthLoginResponse().getMessage();
				if (MSG_PASSWORD_NOT_SET.equals(message)) {
					// Phone exists but has no password yet -> resend OTP and switch
					// to the verification flow.
					redirectingToOtp = true;
					setWaitScreen(true);
					String channel = UserPreferences.getOtpChannel(BA.getContext());
					BA.getEventBus().post(new ResendActivationRequestEvent(
							new ResendActivationRequest(phoneNumber, channel)));
				} else {
					ToastUtil.display(getActivity(), TextUtils.isEmpty(message) ? "Неверные данные" : message);
				}
			}
		} else {
			ToastUtil.display(getActivity(), "Ошибка, попробуйте еще раз");
		}
	}

	@Subscribe
	public void onForgotResponse(ForgotPasswordResponseEvent event) {
		setWaitScreen(false);
		// forgot_password always advances to the OTP + new-password screen.
		activityActions.openResetPassword(phoneNumber);
	}

	@Subscribe
	public void onResendActivationResponse(ResendActivationResponseEvent event) {
		if (!redirectingToOtp) return;
		redirectingToOtp = false;
		setWaitScreen(false);
		if (event.getData() != null && "success".equals(event.getData().getStatus())) {
			activityActions.openVerification();
		} else {
			ToastUtil.display(getActivity(), "Ошибка, попробуйте еще раз");
		}
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
