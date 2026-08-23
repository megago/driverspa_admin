package com.driverspa.assist;
import com.driverspa.R;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.request.AuthAdminRegistrationRequest;
import com.driverspa.model.api.response.AuthAdminRegistrationResponseHolder;
import com.driverspa.model.api.response.AuthClientRegistrationResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AuthAdminRegistrationRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminRegistrationResponseEvent;
import com.driverspa.util.otto.ws.AuthClientRegistrationRequestEvent;
import com.driverspa.util.otto.ws.AuthClientRegistrationResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Event for logging in
 */
public class AuthRegistrationAssist extends BaseAssist {
	
	public AuthRegistrationAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}
	
	/**
	 * Fired when user requests to login
	 * @param event - login data
	 */
	@Subscribe
	public void onClientAuthLoginRequested(final AuthClientRegistrationRequestEvent event) {
		logD("onAuthLoginRequested");
				
		getApi().authClientRegistration(event.getAuthLoginRequest(), new Callback<AuthClientRegistrationResponseHolder>() {
			@Override
			public void success(AuthClientRegistrationResponseHolder data, Response response) {
				if( isSuccess(data) ) {
					logD("onAuthLoginRequested: success");					
					String userId = data.getResponse().getId();									
					String userPhone = data.getResponse().getPhone();
					
					// write down user id and token
					UserPreferences.onTempUserLogin(BA.getContext(), userId, userPhone);
					getEventsBus().post(new AuthClientRegistrationResponseEvent(data));
				} else {
					logE( "onAuthLoginRequested: error: " + data.getStatus());					
					onDataError(data);
					getEventsBus().post(new AuthClientRegistrationResponseEvent(data));
				}
			}
			@Override
			public void failure(RetrofitError error) {									
				onRetrofitError(error);				
				getEventsBus().post(new AuthClientRegistrationResponseEvent(new AuthClientRegistrationResponseHolder()));
			}
		});
	}

	/**
	 * Fired when admin requests to login
	 * @param event - login data
	 */
	@Subscribe
	public void onAdminAuthLoginRequested(final AuthAdminRegistrationRequestEvent event) {
		logD("onAuthLoginRequested");
		sendAdminRegistration(event.getAuthLoginRequest());
	}

	/**
	 * Sends register with the requested OTP channel ("whatsapp" by default).
	 * If a WhatsApp send fails, automatically retries once over SMS.
	 */
	private void sendAdminRegistration(final AuthAdminRegistrationRequest request) {
		getApi().authAdminRegistration(request, new Callback<AuthAdminRegistrationResponseHolder>() {
			@Override
			public void success(AuthAdminRegistrationResponseHolder data, Response response) {
				if( isSuccess(data) ) {
					logD("onAuthLoginRequested: success");
					String userId = data.getResponse().getId();
					String userPhone = data.getResponse().getPhone();
					// write down admin id
					UserPreferences.onTempAdminLogin(BA.getContext(), userId, userPhone);
					getEventsBus().post(new AuthAdminRegistrationResponseEvent(data));
				} else if (!UserPreferences.CHANNEL_SMS.equals(request.getChannel())) {
					// Email/WhatsApp delivery failed — fall back to SMS automatically.
					displayToast(BA.str(R.string.code_fallback_sms));
					request.setChannel(UserPreferences.CHANNEL_SMS);
					sendAdminRegistration(request);
				} else {
					logE( "onAuthLoginRequested: error: " + data.getStatus());
					onDataError(data);
					getEventsBus().post(new AuthAdminRegistrationResponseEvent(data));
				}
			}
			@Override
			public void failure(RetrofitError error) {
				onRetrofitError(error);
				getEventsBus().post(new AuthAdminRegistrationResponseEvent(new AuthAdminRegistrationResponseHolder()));
			}
		});
	}

}
