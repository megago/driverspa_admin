package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.api.request.SetPasswordRequest;
import com.driverspa.util.PasswordUtil;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AuthAdminVerificationResponseEvent;
import com.driverspa.util.otto.ws.SetPasswordRequestEvent;
import com.driverspa.util.otto.ws.SetPasswordResponseEvent;

/**
 * "Create password" step for new users. The account is already signed in
 * (token stored by activate). After set_password succeeds we collect the city,
 * then trigger the carwash bootstrap and enter the app.
 */
public class AdminCreatePasswordFragment extends AdminAuthBaseFragment {

	public interface ActivityActions {
		void openAdminHomeActivity();
		void openWizardActivity();
	}

	private ActivityActions activityActions;

	@BindView(R.id.password)
	EditText password;
	@BindView(R.id.password_confirm)
	EditText passwordConfirm;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_admin_create_password, container, false);
		ButterKnife.bind(this, view);
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
	protected void processSetPassword() {
		String pass = password.getText().toString();
		String passConfirm = passwordConfirm.getText().toString();
		String error = PasswordUtil.validate(pass, passConfirm);
		if (error != null) {
			ToastUtil.display(getActivity(), error);
			return;
		}
		setWaitScreen(true);
		BA.getEventBus().post(new SetPasswordRequestEvent(new SetPasswordRequest(pass, passConfirm)));
	}

	@Subscribe
	public void onSetPasswordResponse(SetPasswordResponseEvent event) {
		setWaitScreen(false);
		if (event.getData() != null && BaseAssist.isSuccess(event.getData())
				&& event.getData().getResponse() != null && event.getData().getResponse().isOk()) {
			// Password saved. Now collect the city (needed to create the carwash),
			// then continue from onResume once the city activity returns.
			chooseCityThenBootstrap();
		} else {
			ToastUtil.display(getActivity(), event.getData() != null && event.getData().getMessage() != null
					? event.getData().getMessage() : BA.str(R.string.err_try_again));
		}
	}

	@Subscribe
	public void onBootstrapResponse(AuthAdminVerificationResponseEvent event) {
		setWaitScreen(false);
		if (event.getAuthLoginResponse() != null && BaseAssist.isSuccess(event.getAuthLoginResponse())) {
			if (handleCityRequired(event))
				return;
			if (event.isActiveCarwash())
				activityActions.openAdminHomeActivity();
			else
				activityActions.openWizardActivity();
		} else {
			ToastUtil.display(getActivity(), BA.str(R.string.err_create_wash_retry));
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
