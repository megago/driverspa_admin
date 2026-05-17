package com.driverspa.assist;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import com.driverspa.BA;
import com.driverspa.db.DriverSpaOrmLiteSqlHelper;
import com.driverspa.model.BookInfo;
import com.driverspa.model.BooksDataHolder;
import com.driverspa.model.CarItem;
import com.driverspa.model.Image;
import com.driverspa.model.PhotoOrm;
import com.driverspa.model.PhotoParcelable;
import com.driverspa.model.User;
import com.driverspa.model.api.response.BaseResponseHolder;
import com.driverspa.model.api.response.QueuedBooksResponseHolder;
import com.driverspa.util.LogUtil;
import com.driverspa.util.RetrofitClient.DriverSpaApi;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminStatisticsResponseEvent;
import com.driverspa.util.otto.ws.ApiErrorEvent;
import retrofit.RetrofitError;

public abstract class BaseAssist {

	private DriverSpaApi driverSpaApi;
	private Bus eventBus;

	public BaseAssist(DriverSpaApi api, Bus eventBus) {
		this.driverSpaApi = api;
		this.eventBus = eventBus;
	}

	public Bus getEventsBus() {
		return eventBus;
	}

	public static boolean isSuccess(BaseResponseHolder response) {
		return !response.getStatus().equals("error");
	}

	/**
	 * Actions taken when a response other than successful is received
	 * @param response
	 */
	protected void onDataError(BaseResponseHolder response) {
		displayToast(response.getMessage());
	}
	
	protected void displayToast(final String message) {
		// Get a handler that can be used to post to the main thread
		Handler mainHandler = new Handler(Looper.getMainLooper());

		Runnable myRunnable = new Runnable() {
			@Override
			public void run() {
				ToastUtil.display(BA.getContext(), message);
			}
		}; // This is your code
		mainHandler.post(myRunnable);
	}

	//	protected String getUserId() {
	//		return UserPreferences.getUserId(BA.getContext());
	//	}

	protected DriverSpaApi getApi() {
		return driverSpaApi;
	}

	public String getTag() {
		return getClass().getSimpleName();
	}

	public void logD(String message) {
		LogUtil.d(getTag(), message);
	}

	public void logE(String message) {
		LogUtil.e(getTag(), message);
	}

	protected void onRetrofitError(RetrofitError error) {
		onRetrofitError(error, true);
	}
	
	/**
	 * Set of actions taken on api request error
	 * @param error
	 * @param postError - post api error if true
	 */
	protected void onRetrofitError(RetrofitError error, boolean postError) {
		LogUtil.e( getTag(), getTag() + ": error " + error.getUrl());
		error.printStackTrace();
		if( postError )
			getEventsBus().post(new ApiErrorEvent(error));
	}

	protected void writeSelfToDb(User user) {
		DriverSpaOrmLiteSqlHelper dbHelper = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		RuntimeExceptionDao<User, String> userDao = dbHelper.getUserRuntimeExceptionDao();	
		ArrayList<PhotoParcelable> photos = new ArrayList<PhotoParcelable>(/*list.size()*/);
		for(Image image : user.getImages()){
			PhotoOrm photoOrm = image.getPhotoOrm(user.getId());
			photos.add(PhotoParcelable.newPhoto(photoOrm));
		}
		// insert if already data exists
		userDao.createOrUpdate(user);
		insertCars(user.getCars());
		insertPhotos(photos);
		dbHelper.close();
	}

	protected void writeBooks(List<BookInfo> books, QueuedBooksResponseHolder data) {
		DriverSpaOrmLiteSqlHelper dbHelper = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		RuntimeExceptionDao<BookInfo, String> bookingDao = dbHelper.getBookingRuntimeDao();
		DriverSpaOrmLiteSqlHelper db = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		db.deleteAllBooksData();
		BooksDataHolder holder = new BooksDataHolder(books);
		UserPreferences.putBooksData(BA.getContext(),holder.serialize());
		for(BookInfo book : books){
			bookingDao.createOrUpdate(book);
		}
		dbHelper.close();
		BA.getEventBus().post(new AdminStatisticsResponseEvent(data));
	}

	protected void writeBooks(List<BookInfo> books) {
		DriverSpaOrmLiteSqlHelper dbHelper = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		RuntimeExceptionDao<BookInfo, String> bookingDao = dbHelper.getBookingRuntimeDao();
		DriverSpaOrmLiteSqlHelper db = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		db.deleteAllBooksData();
		BooksDataHolder holder = new BooksDataHolder(books);
		UserPreferences.putBooksData(BA.getContext(),holder.serialize());
		for(BookInfo book : books){
			bookingDao.createOrUpdate(book);
		}
		dbHelper.close();
	}

	protected void removeAllDbData() {
		DriverSpaOrmLiteSqlHelper dbHelper = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		dbHelper.removeAll();
		dbHelper.close();
	}


	// url = file path or whatever suitable URL you want.
	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}
	
	public static String getMimeType2(Uri uri) {
		/*
	     * Get the file's content URI from the incoming Intent, then
	     * get the file's MIME type
	     */
	    String mimeType = BA.getContext().getContentResolver().getType(uri);
	    return mimeType;
	}
	
	/**
	 * Inserts photos into database
	 * @param photos - list of photos
	 */
	protected static void insertPhotos(final ArrayList<PhotoParcelable> photos) {
		DriverSpaOrmLiteSqlHelper dbHelper = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		final RuntimeExceptionDao<PhotoOrm, String> photoOrmDao = dbHelper.getUserPhotoRuntimeExceptionDao();
		dbHelper.removePhotos();
		for( PhotoParcelable photoParcelable : photos ) {
			PhotoOrm photoOrm = (PhotoOrm)photoParcelable;
			photoOrmDao.createOrUpdate(photoOrm);
		}
	}

	protected static void insertCars(final List<CarItem> cars) {
		DriverSpaOrmLiteSqlHelper dbHelper = new DriverSpaOrmLiteSqlHelper(BA.getContext());
		final RuntimeExceptionDao<CarItem, String> carsDao = dbHelper.getUserCarsRuntimeExceptionDao();
		if(cars != null)
		 for(CarItem car : cars) {
			if(!TextUtils.isEmpty(car.getCarNumber())&&!TextUtils.isEmpty(car.getCarModel())&&!TextUtils.isEmpty(car.getCarType()+""))
			    carsDao.createOrUpdate(car);
		 }
	}
}
