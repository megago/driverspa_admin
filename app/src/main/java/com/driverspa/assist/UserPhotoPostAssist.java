package com.driverspa.assist;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.driverspa.BA;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.PhotoResponseHolder;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.otto.ws.AdminPhotoPostRequestEvent;
import com.driverspa.util.otto.ws.AdminPhotoPostResponseEvent;
import com.driverspa.util.otto.ws.ClientPhotoPostRequestEvent;
import com.driverspa.util.otto.ws.ClientPhotoPostResponseEvent;
import com.driverspa.util.otto.ws.PhotoDeleteRequestEvent;
import com.driverspa.util.otto.ws.PhotoDeleteResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Assistant class for uploading photo to server
 */
public class UserPhotoPostAssist extends BaseAssist {

	public UserPhotoPostAssist(DriverSpaApi api, Bus eventBus) {
		super(api, eventBus);
	}

	/**
	 * Upload photo on request
	 * @param event - file path and user id to be uploaded 
	 */
	@Subscribe
	public void onUserPhotoPostRequested(final ClientPhotoPostRequestEvent event) {
		String mimeType = getMimeType(event.getFile().getAbsolutePath());
		if(mimeType == null){
			mimeType = "jpg";
		}
		getApi().clientPhotoPost(new TypedFile(mimeType, event.getFile() ), 
				new Callback<PhotoResponseHolder>() {
			
			@Override
			public void success(PhotoResponseHolder data, Response response) {
				logD(data.toString());
				BA.getEventBus().post(new ClientPhotoPostResponseEvent(data));
			}
			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new ClientPhotoPostResponseEvent(null));
				onRetrofitError(error);
			}
		} );
	}


	@Subscribe
	public void onUserPhotoDeleteRequested(final PhotoDeleteRequestEvent event) {
		getApi().photoDelete(event.getPhotoId(),new Callback<BaseResponseHolder>() {
					@Override
					public void success(BaseResponseHolder data, Response response) {
						logD(data.toString());
						BA.getEventBus().post(new PhotoDeleteResponseEvent(data));
					}
					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new PhotoDeleteResponseEvent(null));
						onRetrofitError(error);
					}
				}
		);
	}

	/**
	 * Upload photo on request
	 * @param event - file path and user id to be uploaded 
	 */
	@Subscribe
	public void onUserPhotoPostRequested(final AdminPhotoPostRequestEvent event) {
		String mimeType = getMimeType(event.getFile().getAbsolutePath());
		if(mimeType == null){
			mimeType = "jpg";
		}
		getApi().adminPhotoPost(new TypedFile(mimeType, event.getFile()),
								event.getWasherId(), 
				new Callback<PhotoResponseHolder>() {
			
			@Override
			public void success(PhotoResponseHolder data, Response response) {
				logD(data.toString());
				BA.getEventBus().post(new AdminPhotoPostResponseEvent(data));
			}
			@Override
			public void failure(RetrofitError error) {
				BA.getEventBus().post(new AdminPhotoPostResponseEvent(null));
				onRetrofitError(error);
			}
		} );
	}
	
	
}
