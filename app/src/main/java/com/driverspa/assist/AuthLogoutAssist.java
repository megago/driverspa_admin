package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.otto.ws.AuthClientLogoutRequestEvent;
import com.driverspa.util.otto.ws.AuthClientLogoutResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AuthLogoutAssist extends BaseAssist {

	public AuthLogoutAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onLogoutRequested(AuthClientLogoutRequestEvent event) {
		getApi().authLogout(new Callback<BaseResponseHolder>() {
			@Override
			public void success(BaseResponseHolder data, Response response) {
				logD("onLogoutRequested: success" );								
				BA.getEventBus().post(new AuthClientLogoutResponseEvent());
			}
			@Override
			public void failure(RetrofitError error) {
				logE("onLogoutRequested: error" );				
				onRetrofitError(error);				
				BA.getEventBus().post(new AuthClientLogoutResponseEvent());
			}
		});
	}
	
}
