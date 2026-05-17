package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.response.AboutUSResponseHolder;
import com.driverspa.model.api.response.ReferenceResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AboutUSRequestEvent;
import com.driverspa.util.otto.ws.AboutUSResponseEvent;
import com.driverspa.util.otto.ws.InitRequestEvent;
import com.driverspa.util.otto.ws.InitResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class InitilizeAssist extends BaseAssist {

	public InitilizeAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onInitilizeRequested(InitRequestEvent event) {
//		onAboutUSRequested(new AboutUSRequestEvent());
		getApi().getReference(
						event.getLang(),
						new Callback<ReferenceResponseHolder>() {
			@Override
			public void success(ReferenceResponseHolder data, Response response) {
				if( isSuccess(data) ) {
					logD("onPeopleOnlineRequested: success " + response.getUrl());
					BA.getEventBus().post(new InitResponseEvent(data.getResponse()));					
				} else {
					BA.getEventBus().post(new InitResponseEvent(null));
					logE("onPeopleOnlineRequested: error");
				}
			}
			@Override
			public void failure(RetrofitError error) {								
				onRetrofitError(error);
				BA.getEventBus().post(new InitResponseEvent(null));
			}
		});
	}

	@Subscribe
	public void onAboutUSRequested(AboutUSRequestEvent event) {
		  getApi().getAboutUS(
						new Callback<AboutUSResponseHolder>() {
			@Override
			public void success(AboutUSResponseHolder data, Response response) {
				    UserPreferences.putAboutUS(BA.getContext(),data.getResponse().getAboutUS());
					BA.getEventBus().post(new AboutUSResponseEvent(data));
			}
			@Override
			public void failure(RetrofitError error) {
				onRetrofitError(error);
				BA.getEventBus().post(new AboutUSResponseEvent(null));
			}
		});
	}

}
