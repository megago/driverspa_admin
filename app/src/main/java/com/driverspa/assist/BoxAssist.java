package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.request.BoxOperationRequest;
import com.driverspa.model.api.response.AdminBoxResponseHolder;
import com.driverspa.model.api.response.AdminBoxesResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.otto.ws.AdminBoxAddRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxAddResponseEvent;
import com.driverspa.util.otto.ws.AdminBoxDeleteRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxDeleteResponseEvent;
import com.driverspa.util.otto.ws.AdminBoxListRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxListResponseEvent;
import com.driverspa.util.otto.ws.AdminBoxRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxResponseEvent;
import com.driverspa.util.otto.ws.AdminBoxTypeChangeRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxTypeChangeResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BoxAssist extends BaseAssist {

	public BoxAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onBoxListRequested(AdminBoxListRequestEvent event) {
		getApi().adminBoxList(event.getWasherId(),
							new Callback<AdminBoxesResponseHolder>() {
			@Override
			public void success(AdminBoxesResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminBoxListResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new AdminBoxListResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onBoxRequested(AdminBoxRequestEvent event) {
		getApi().adminBox(event.getWasherId(),
					      event.getBoxId(),					      
							new Callback<AdminBoxResponseHolder>() {
			@Override
			public void success(AdminBoxResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminBoxResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new AdminBoxResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onBoxTypeChangeRequested(AdminBoxTypeChangeRequestEvent event) {
		getApi().adminChangeBox(event.getWasherId(),
					            event.getUid(),
								new BoxOperationRequest(event.getBoxName(), event.getBookingType().getValue().toString(), event.getWasherPerson()),
				                new Callback<AdminBoxResponseHolder>() {
			@Override
			public void success(AdminBoxResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminBoxTypeChangeResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new AdminBoxTypeChangeResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}

	@Subscribe
	public void onBoxAddRequested(AdminBoxAddRequestEvent event) {
		getApi().adminAddBox(event.getWasherId(),
					         new BoxOperationRequest(event.getBoxName(), event.getBoxType(), event.getWasherPerson()),
				             new Callback<AdminBoxResponseHolder>() {
			
			@Override
			public void success(AdminBoxResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminBoxAddResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new AdminBoxAddResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}
	
	@Subscribe
	public void onBoxDeleteRequested(AdminBoxDeleteRequestEvent event) {
		getApi().adminDeleteBox(event.getWasherId(),
					            event.getBoxId(),
				                new Callback<AdminBoxResponseHolder>() {
			@Override
			public void success(AdminBoxResponseHolder data, Response response) {
					BA.getEventBus().post(new AdminBoxDeleteResponseEvent(data));					
			}
			
			@Override
			public void failure(RetrofitError error) {				
				BA.getEventBus().post(new AdminBoxDeleteResponseEvent(null));
				onRetrofitError(error);
			}
		});
	}




}
