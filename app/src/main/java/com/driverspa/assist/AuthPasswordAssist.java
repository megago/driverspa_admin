package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.model.api.response.CheckPhoneResponseHolder;
import com.driverspa.model.api.response.OkResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.otto.ws.CheckPhoneRequestEvent;
import com.driverspa.util.otto.ws.CheckPhoneResponseEvent;
import com.driverspa.util.otto.ws.ForgotPasswordRequestEvent;
import com.driverspa.util.otto.ws.ForgotPasswordResponseEvent;
import com.driverspa.util.otto.ws.SetPasswordRequestEvent;
import com.driverspa.util.otto.ws.SetPasswordResponseEvent;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Handles the stateless password endpoints that do not need the post-sign-in
 * carwash bootstrap: check_phone, set_password and forgot_password.
 * login_password / reset_password live in {@link AuthVerificationAssist} because
 * they share that bootstrap.
 */
public class AuthPasswordAssist extends BaseAssist {

	public AuthPasswordAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onCheckPhoneRequested(final CheckPhoneRequestEvent event) {
		logD("onCheckPhoneRequested");
		getApi().checkPhone(event.getRequest(), new Callback<CheckPhoneResponseHolder>() {
			@Override
			public void success(CheckPhoneResponseHolder data, Response response) {
				if (isSuccess(data)) {
					getEventsBus().post(new CheckPhoneResponseEvent(data));
				} else {
					getEventsBus().post(new CheckPhoneResponseEvent(data));
					onDataError(data);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new CheckPhoneResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onSetPasswordRequested(final SetPasswordRequestEvent event) {
		logD("onSetPasswordRequested");
		getApi().setPassword(event.getRequest(), new Callback<OkResponseHolder>() {
			@Override
			public void success(OkResponseHolder data, Response response) {
				if (isSuccess(data)) {
					getEventsBus().post(new SetPasswordResponseEvent(data));
				} else {
					getEventsBus().post(new SetPasswordResponseEvent(data));
					onDataError(data);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new SetPasswordResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onForgotPasswordRequested(final ForgotPasswordRequestEvent event) {
		logD("onForgotPasswordRequested");
		getApi().forgotPassword(event.getRequest(), new Callback<OkResponseHolder>() {
			@Override
			public void success(OkResponseHolder data, Response response) {
				if (isSuccess(data)) {
					getEventsBus().post(new ForgotPasswordResponseEvent(data));
				} else {
					getEventsBus().post(new ForgotPasswordResponseEvent(data));
					onDataError(data);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new ForgotPasswordResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}
}
