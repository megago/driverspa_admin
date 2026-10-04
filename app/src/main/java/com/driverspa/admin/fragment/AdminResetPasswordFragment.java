package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.api.request.ResetPasswordRequest;
import com.driverspa.util.Converters;
import com.driverspa.util.Functions;
import com.driverspa.util.PasswordUtil;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.ResetPasswordRequestEvent;

/**
 * Forgot-password completion: OTP code + new password (reset_password). On
 * success the user is signed in with the new password.
 */
public class AdminResetPasswordFragment extends AdminAuthBaseFragment {

	public static final String ARG_PHONE = "ARG_PHONE";

	public interface ActivityActions {
		void openAdminHomeActivity();
		void openWizardActivity();
	}

	private ActivityActions activityActions;
	private String phoneNumber;

	@BindView(R.id.verificationCode)
	EditText verificationCode;
	@BindView(R.id.password)
	EditText password;
	@BindView(R.id.password_confirm)
	EditText passwordConfirm;
	@BindView(R.id.txtPhoneNo)
	TextView txtPhoneNo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_reset_password, container, false);
		ButterKnife.bind(this, view);
		if (getArguments() != null) {
			phoneNumber = getArguments().getString(ARG_PHONE);
		}
		txtPhoneNo.setText(Functions.maskPhoneNumber(phoneNumber, Converters.PHONE_PATTERN));
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

	@OnClick(R.id.btnSave)
	protected void processReset() {
		String code = verificationCode.getText().toString().replaceAll("-", "").replaceAll(" ", "");
		if (TextUtils.isEmpty(code)) {
			verificationCode.setError(BA.str(R.string.enter_code));
			return;
		}
		String pass = password.getText().toString();
		String passConfirm = passwordConfirm.getText().toString();
		String error = PasswordUtil.validate(pass, passConfirm);
		if (error != null) {
			ToastUtil.display(getActivity(), error);
			return;
		}
		setWaitScreen(true);
		BA.getEventBus().post(new ResetPasswordRequestEvent(
				new ResetPasswordRequest(phoneNumber, code, pass, passConfirm)));
	}

	@Subscribe
	public void onResetResponse(AuthAdminVerificationResponseEvent event) {
		setWaitScreen(false);
		if (event.getAuthLoginResponse() != null) {
			if (BaseAssist.isSuccess(event.getAuthLoginResponse())) {
				if (handleCityRequired(event))
					return;
				if (event.isActiveCarwash())
					activityActions.openAdminHomeActivity();
				else
					activityActions.openWizardActivity();
			} else {
				String message = event.getAuthLoginResponse().getMessage();
				ToastUtil.display(getActivity(), TextUtils.isEmpty(message) ? BA.str(R.string.invalid_code) : message);
			}
		} else {
			ToastUtil.display(getActivity(), BA.str(R.string.err_try_again));
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
