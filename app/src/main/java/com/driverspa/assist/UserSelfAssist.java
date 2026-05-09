package com.driverspa.assist;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.driverspa.BA;
import com.driverspa.db.WashmeOrmLiteSqlHelper;
import com.driverspa.model.CarItem;
import com.driverspa.model.Image;
import com.driverspa.model.PhotoOrm;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.model.User;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.UserResponseHolder;
import com.driverspa.util.RetrofitClient.WashmeApi;
import com.driverspa.util.otto.ws.DeleteDeviceRequestEvent;
import com.driverspa.util.otto.ws.DeleteDeviceResponseEvent;
import com.driverspa.util.otto.ws.PushRequestEvent;
import com.driverspa.util.otto.ws.PushResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.UserLocationUpdateEvent;
import com.driverspa.util.otto.ws.UserPhotoChangeRequestEvent;
import com.driverspa.util.otto.ws.UserPhotoChangeResponseEvent;
import com.driverspa.util.otto.ws.UserUpdateSelfRequestEvent;
import com.driverspa.util.otto.ws.UserUpdateSelfResponseEvent;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserSelfAssist extends BaseAssist {

	public UserSelfAssist(WashmeApi api, Bus eventBus) {
		super(api, eventBus);
	}

	@Subscribe
	public void onUserSelfRequested(final UserGetSelfRequestEvent event) {

		getApi().userGet(event.getId(), new Callback<UserResponseHolder>() {
			@Override
			public void success(UserResponseHolder data, Response response) {
				logD("onUserSelfRequested: url: " + response.getUrl());
				if (isSuccess(data)) {
					writeSelfToDb(data.getResponse());
					BA.getEventBus().post(new UserGetSelfResponseEvent(data.getResponse()));
				} else {
					User inserted = getUserFromDb(event.getId());
					BA.getEventBus().post(new UserGetSelfResponseEvent(inserted));
				}
			}

			@Override
			public void failure(RetrofitError error) {
				onRetrofitError(error);
				User inserted = getUserFromDb(event.getId());
				BA.getEventBus().post(new UserGetSelfResponseEvent(inserted));
			}
		});
	}

	@Subscribe
	public void onUserSelfUpdateRequested(final UserUpdateSelfRequestEvent event) {

		getApi().userUpdate(event.getId(),
				event.getUserRequest(),
				new Callback<UserResponseHolder>() {
					@Override
					public void success(UserResponseHolder data, Response response) {
						logD("onUserUpdateSelfRequested: url: " + response.getUrl());
						if (isSuccess(data)) {
							writeSelfToDb(data.getResponse());
						}
						BA.getEventBus().post(new UserUpdateSelfResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						BA.getEventBus().post(new UserUpdateSelfResponseEvent(null));
						onRetrofitError(error);
					}
				});
	}


	/**
	 * Runs when user request to change photo
	 * @param event - holds user id and photo id to be used as avatar
	 */
	@Subscribe
	public void onUserPhotoChangeRequested(UserPhotoChangeRequestEvent event) {
		getApi().userPhotoChange(event.getPhotoId(),
				new Callback<BaseResponseHolder>() {

					@Override
					public void success(BaseResponseHolder data, Response response) {
						BA.getEventBus().post(new UserPhotoChangeResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
						BA.getEventBus().post(new UserPhotoChangeResponseEvent(null));
					}
				});
		}


	public static User getUserFromDb(String userId) {
		try {
			WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
			RuntimeExceptionDao<User, String> userDao = dbHelper.getUserRuntimeExceptionDao();
			User userInserted = userDao.queryForId(userId);

			ArrayList<PhotoParcelable> photos = getPhotos(userId);
			List<CarItem> cars = getCars();
			ArrayList<Image> images = photos.stream()
					.map(photo -> ((PhotoOrm) photo).getImage())
					.collect(Collectors.toCollection(ArrayList::new));
			userInserted.setCars(cars);
			userInserted.setImages(images);
			dbHelper.close();
			return userInserted;
		}catch(Exception e){
		}
		return null;
	}

	/**
	 * Read photos of given user from database
	 * @param userId
	 * @return
	 */
	protected static ArrayList<PhotoParcelable> getPhotos(String userId) {
		WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
		final RuntimeExceptionDao<PhotoOrm, String> photoOrmDao = dbHelper.getUserPhotoRuntimeExceptionDao();
		QueryBuilder<PhotoOrm, String> qb = photoOrmDao.queryBuilder();
		List<PhotoOrm> list = new ArrayList<PhotoOrm>();
		try {
			list = qb.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ArrayList<PhotoParcelable> photos = new ArrayList<PhotoParcelable>(/*list.size()*/);
		photos.addAll(list.stream().map(PhotoParcelable::newPhoto).collect(Collectors.toList()));

		return photos;
	}
	/**
	 * Read photos of given user from database
	 * @return
	 */
	protected static List<CarItem> getCars() {
		WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
		final RuntimeExceptionDao<CarItem, String> carsDao = dbHelper.getUserCarsRuntimeExceptionDao();
		QueryBuilder<CarItem, String> qb = carsDao.queryBuilder();
		List<CarItem> list = new ArrayList<CarItem>();
		try {
			list = qb.query();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}

//	/**
//	 * Runs when user request to send push token
//	 */
//	@Subscribe
//	public void onSettingsReceived(PushRequestEvent event) {
//		getApi().updateSettings(new Settings(event.getToken()),
//				new Callback<BaseResponseHolder>() {
//
//					@Override
//					public void success(BaseResponseHolder data, Response response) {
//						BA.getEventBus().post(new PushResponseEvent(data));
//					}
//
//					@Override
//					public void failure(RetrofitError error) {
//						onRetrofitError(error);
//						BA.getEventBus().post(new PushResponseEvent(null));
//					}
//				});
//
//	 	getApi().settingsList(new retrofit.Callback<BaseResponseHolder>(){
//			@Override
//			public void success(BaseResponseHolder washerResponseHolder, Response response) {
//				L.d("fucking here");
//			}
//
//			@Override
//			public void failure(RetrofitError retrofitError) {
//				L.d("fucking here fail");
//			}
//		});
//	}

	/**
	 * Runs when user request to send push token
	 */
	@Subscribe
	public void onAddDeviceReceived(PushRequestEvent event) {
		getApi().addDevice(
				event.getDeviceId(),
				event.getDevice(),
				new Callback<BaseResponseHolder>() {

					@Override
					public void success(BaseResponseHolder data, Response response) {
						BA.getEventBus().post(new PushResponseEvent(data));
					}



					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
						BA.getEventBus().post(new PushResponseEvent(null));
					}
				});
	}


	@Subscribe
	public void onDeleteDeviceReceived(DeleteDeviceRequestEvent event) {
		getApi().deleteDevice(
				android.provider.Settings.Secure.getString(BA.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID),
				new Callback<BaseResponseHolder>() {

					@Override
					public void success(BaseResponseHolder data, Response response) {
						BA.getEventBus().post(new DeleteDeviceResponseEvent(data));
					}

					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
						BA.getEventBus().post(new DeleteDeviceResponseEvent(null));
					}
				});
	}

	@Subscribe
	public void onUserLocationReceived(UserLocationUpdateEvent event) {
		getApi().updateLocation("location",event.getUserLocation(),
				new Callback<BaseResponseHolder>() {
					@Override
					public void success(BaseResponseHolder data, Response response) {
					}

					@Override
					public void failure(RetrofitError error) {
						onRetrofitError(error);
					}
				});
	}

}
