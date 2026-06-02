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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.squareup.otto.Subscribe;

import java.text.ParseException;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.api.request.AuthAdminRegistrationRequest;
import com.driverspa.model.api.request.CheckPhoneRequest;
import com.driverspa.model.api.request.ResendActivationRequest;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AuthAdminRegistrationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminRegistrationResponseEvent;
import com.driverspa.util.otto.ws.CheckPhoneRequestEvent;
import com.driverspa.util.otto.ws.CheckPhoneResponseEvent;
import com.driverspa.util.otto.ws.ResendActivationRequestEvent;
import com.driverspa.util.otto.ws.ResendActivationResponseEvent;

/**
 * Entry screen of the auth flow. Collects a phone number + OTP delivery channel
 * (WhatsApp by default, SMS optional) and calls check_phone, then branches:
 *   has_password == true   -> password login screen
 *   exists == false        -> register (send OTP) + OTP screen
 *   exists, no password    -> resend_activation (send OTP) + OTP screen
 */
public class AdminPhoneFragment extends AdminBaseFragment {

	public interface ActivityActions {
		void openPasswordLogin(String phone);
		void openVerification();
	}

	private ActivityActions activityActions;
	private String fullPhone;
	private String channel;

	@BindView(R.id.login_phone)
	EditText phone;
	@BindView(R.id.channelGroup)
	RadioGroup channelGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_phone, container, false);
		ButterKnife.bind(this, view);
		try {
			MaskedWatcher maskedWatcher = new MaskedWatcher("### ###-##-##");
			phone.addTextChangedListener(maskedWatcher);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// SMS is temporarily disabled — only WhatsApp can be selected for now.
		channelGroup.check(R.id.channelWhatsapp);
		phone.requestFocus();
		phone.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					processPhone();
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

	private String selectedChannel() {
		return channelGroup.getCheckedRadioButtonId() == R.id.channelSms
				? UserPreferences.CHANNEL_SMS : UserPreferences.CHANNEL_WHATSAPP;
	}

	@OnClick(R.id.btnOk)
	protected void processPhone() {
		String phoneStr = phone.getText().toString()
				.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "")
				.replaceAll("-", "").replaceAll(" ", "");
		if (TextUtils.isEmpty(phoneStr)) {
			phone.setError("Введите номер телефона");
			return;
		}
		if (phoneStr.length() != 10) {
			phone.setError("Неправильный номер телефона");
			return;
		}
		fullPhone = "+7" + phoneStr;
		channel = selectedChannel();
		UserPreferences.putOtpChannel(BA.getContext(), channel);
		setWaitScreen(true);
		BA.getEventBus().post(new CheckPhoneRequestEvent(new CheckPhoneRequest(fullPhone)));
	}

	@Subscribe
	public void onCheckPhoneResponse(CheckPhoneResponseEvent event) {
		if (event.getData() == null || event.getData().getResponse() == null
				|| !"success".equals(event.getData().getStatus())) {
			setWaitScreen(false);
			ToastUtil.display(getActivity(), event.getData() != null && event.getData().getMessage() != null
					? event.getData().getMessage() : "Ошибка, попробуйте еще раз");
			return;
		}

		if (event.getData().getResponse().isHasPassword()) {
			// Returning user with a password -> password login.
			setWaitScreen(false);
			activityActions.openPasswordLogin(fullPhone);
		} else if (!event.getData().getResponse().isExists()) {
			// Brand-new user -> create account + send OTP.
			BA.getEventBus().post(new AuthAdminRegistrationRequestEvent(
					new AuthAdminRegistrationRequest("NA", fullPhone, channel)));
		} else {
			// Existing user without a password -> resend OTP.
			BA.getEventBus().post(new ResendActivationRequestEvent(
					new ResendActivationRequest(fullPhone, channel)));
		}
	}

	@Subscribe
	public void onRegistrationResponse(AuthAdminRegistrationResponseEvent event) {
		setWaitScreen(false);
		if (event.getAuthLoginResponse() != null
				&& event.getAuthLoginResponse().getStatus() != null
				&& !event.getAuthLoginResponse().getStatus().equals("error")) {
			activityActions.openVerification();
		} else {
			ToastUtil.display(getActivity(), "Ошибка, попробуйте еще раз");
		}
	}

	@Subscribe
	public void onResendActivationResponse(ResendActivationResponseEvent event) {
		setWaitScreen(false);
		if (event.getData() != null && "success".equals(event.getData().getStatus())) {
			activityActions.openVerification();
		} else {
			ToastUtil.display(getActivity(), event.getData() != null && event.getData().getMessage() != null
					? event.getData().getMessage() : "Ошибка, попробуйте еще раз");
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
