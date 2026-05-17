package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.otto.ws.AddReviewRequestEvent;
import com.driverspa.util.otto.ws.AddReviewResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ClientReviewAssist extends BaseAssist {

	public ClientReviewAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onAddReviewRequested(AddReviewRequestEvent event) {
		getApi().addClientReview(
						event.getRequest(),
						new Callback<BaseResponseHolder>() {
			@Override
			public void success(BaseResponseHolder data, Response response) {
				    BA.getEventBus().post(new AddReviewResponseEvent(data));
			}
			@Override
			public void failure(RetrofitError error) {								
				onRetrofitError(error);
				BA.getEventBus().post(new AddReviewResponseEvent(null));
			}
		});
	}
}

