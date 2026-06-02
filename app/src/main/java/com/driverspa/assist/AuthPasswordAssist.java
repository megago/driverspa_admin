package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.model.api.request.ForgotPasswordRequest;
import com.driverspa.model.api.request.ResendActivationRequest;
import com.driverspa.model.api.response.CheckPhoneResponseHolder;
import com.driverspa.model.api.response.OkResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.CheckPhoneRequestEvent;
import com.driverspa.util.otto.ws.CheckPhoneResponseEvent;
import com.driverspa.util.otto.ws.ForgotPasswordRequestEvent;
import com.driverspa.util.otto.ws.ForgotPasswordResponseEvent;
import com.driverspa.util.otto.ws.ResendActivationRequestEvent;
import com.driverspa.util.otto.ws.ResendActivationResponseEvent;
import com.driverspa.util.otto.ws.SetPasswordRequestEvent;
import com.driverspa.util.otto.ws.SetPasswordResponseEvent;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Handles the stateless password endpoints that do not need the post-sign-in
 * carwash bootstrap: check_phone, set_password, forgot_password and
 * resend_activation. login_password / reset_password live in
 * {@link AuthVerificationAssist} because they share that bootstrap.
 *
 * OTP-sending calls (forgot_password, resend_activation) carry a delivery
 * channel ("whatsapp" by default). If a WhatsApp send fails they automatically
 * retry once over SMS.
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
				if (!isSuccess(data)) {
					onDataError(data);
				}
				getEventsBus().post(new CheckPhoneResponseEvent(data));
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
				if (!isSuccess(data)) {
					onDataError(data);
				}
				getEventsBus().post(new SetPasswordResponseEvent(data));
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
		sendForgot(event.getRequest());
	}

	private void sendForgot(final ForgotPasswordRequest request) {
		getApi().forgotPassword(request, new Callback<OkResponseHolder>() {
			@Override
			public void success(OkResponseHolder data, Response response) {
				if (isSuccess(data)) {
					getEventsBus().post(new ForgotPasswordResponseEvent(data));
				} else if (!UserPreferences.CHANNEL_SMS.equals(request.getChannel())) {
					// Email/WhatsApp delivery failed — fall back to SMS automatically.
					displayToast("Не удалось отправить код, отправляем по SMS");
					request.setChannel(UserPreferences.CHANNEL_SMS);
					sendForgot(request);
				} else {
					onDataError(data);
					getEventsBus().post(new ForgotPasswordResponseEvent(data));
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new ForgotPasswordResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onResendActivationRequested(final ResendActivationRequestEvent event) {
		logD("onResendActivationRequested");
		sendResend(event.getRequest());
	}

	private void sendResend(final ResendActivationRequest request) {
		getApi().resendActivation(request, new Callback<OkResponseHolder>() {
			@Override
			public void success(OkResponseHolder data, Response response) {
				if (isSuccess(data)) {
					getEventsBus().post(new ResendActivationResponseEvent(data));
				} else if (!UserPreferences.CHANNEL_SMS.equals(request.getChannel())) {
					// Email/WhatsApp delivery failed — fall back to SMS automatically.
					displayToast("Не удалось отправить код, отправляем по SMS");
					request.setChannel(UserPreferences.CHANNEL_SMS);
					sendResend(request);
				} else {
					onDataError(data);
					getEventsBus().post(new ResendActivationResponseEvent(data));
				}
			}

			@Override
			public void failure(RetrofitError error) {
				getEventsBus().post(new ResendActivationResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}
}
