package com.driverspa.util;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.util.HashMap;

import com.driverspa.BA;
import com.driverspa.assist.AuthRegistrationAssist;
import com.driverspa.assist.AuthVerificationAssist;
import com.driverspa.assist.BookAssist;
import com.driverspa.assist.BoxAssist;
import com.driverspa.assist.ClientReviewAssist;
import com.driverspa.assist.InitilizeAssist;
import com.driverspa.assist.LocationAssist;
import com.driverspa.assist.UserPhotoPostAssist;
import com.driverspa.assist.UserSelfAssist;
import com.driverspa.assist.WasherAssist;
import com.driverspa.db.WashmeOrmLiteSqlHelper;
import com.driverspa.model.NewPushInformation;
import com.driverspa.model.PushData;
import com.driverspa.model.SearchFilter;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.service.Service;
import com.driverspa.util.otto.BookingPushUpdateRequestEvent;
import com.driverspa.util.otto.NewBookingPushRequestEvent;
import com.driverspa.util.otto.NewBookingPushResponseEvent;
import com.driverspa.util.otto.NewReviewPushRequestEvent;
import com.driverspa.util.otto.NewReviewPushResponseEvent;
import com.driverspa.util.otto.RemoveBookingPushRequestEvent;
import com.driverspa.util.otto.RemoveBulkBookingPushRequestEvent;
import com.driverspa.util.otto.ReviewPushUpdateRequestEvent;
import com.driverspa.util.otto.StartPlayRingtoneRequestEvent;
import com.driverspa.util.otto.StopPlayRingtoneRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminLogoutRequestEvent;
import com.driverspa.util.otto.ws.AuthClientLogoutRequestEvent;
import com.driverspa.util.otto.ws.FilterGetResponseEvent;
import com.driverspa.util.otto.ws.FilterResponseEvent;
import com.driverspa.util.otto.ws.FilterUpdateRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.UserUpdateSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class Singleton {

	private static Singleton instance = null;
	private AuthRegistrationAssist authClientRegistrationAssist;
	private AuthVerificationAssist authClientVerificationAssist;
	private WasherAssist washerGetAssist;
	private LocationAssist locationAssist;
	private InitilizeAssist initAssist;
	private BookAssist bookAssist;
	private UserPhotoPostAssist clientPhotoPostAssist;
	private ClientReviewAssist clientReviewAssist;
	private UserSelfAssist userSelfAssist;
	private BoxAssist boxAssist;
	private User user;
	private Washer washer;
	private SearchFilter searchFilter;
    private NewPushInformation bookingPush;
    private NewPushInformation reviewPush;
	private static Ringtone r;

	public Singleton() {				
		authClientRegistrationAssist = new AuthRegistrationAssist(RetrofitClient.getRestAdapter(), BA.getEventBus());
		authClientVerificationAssist = new AuthVerificationAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		washerGetAssist = new WasherAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		locationAssist = new LocationAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		initAssist = new InitilizeAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		bookAssist  = new BookAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		clientPhotoPostAssist = new UserPhotoPostAssist(RetrofitClient.getRestAdapterForUpload(), BA.getEventBus());
		clientReviewAssist = new ClientReviewAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		userSelfAssist = new UserSelfAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		boxAssist = new BoxAssist(RetrofitClient.getRestAdapterWithBody(), BA.getEventBus());
		
		BA.getEventBus().register(authClientRegistrationAssist);
		BA.getEventBus().register(authClientVerificationAssist);
		BA.getEventBus().register(washerGetAssist);
		BA.getEventBus().register(locationAssist);
		BA.getEventBus().register(initAssist);
		BA.getEventBus().register(bookAssist);
		BA.getEventBus().register(clientPhotoPostAssist);
		BA.getEventBus().register(clientReviewAssist);
		BA.getEventBus().register(userSelfAssist);
		BA.getEventBus().register(boxAssist);
		BA.getEventBus().register(this);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			BA.getContext().startForegroundService(new Intent(BA.getContext(), Service.class));
		} else {
			BA.getContext().startService(new Intent(BA.getContext(), Service.class));
		}
	}
	
	/**
	 * Runs on user logout.
	 * Cleans shared preferences, memory (singleton), and runs this application from beginning
	 * @param event
	 */
	@Subscribe
	public void onClientUserLogout(AuthClientLogoutRequestEvent event) {
		UserPreferences.onUserLogout(BA.getContext());
		user = null;
		WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
		dbHelper.removeAll();
		dbHelper.close();
		BA.restart();
	}

	@Subscribe
	public void onAdminUserLogout(AuthAdminLogoutRequestEvent event) {
		UserPreferences.onAdminLogout(BA.getContext());
		WashmeOrmLiteSqlHelper dbHelper = new WashmeOrmLiteSqlHelper(BA.getContext());
		dbHelper.removeAll();
		dbHelper.close();
		user = null;
		washer = null;
		BA.restart();
	}

	@Produce
	public UserGetSelfResponseEvent produceUser() {
		if(user == null){
			user = UserSelfAssist.getUserFromDb(UserPreferences.getUserId(BA.getContext()));
		}
		return new UserGetSelfResponseEvent(user);
	}

	@Subscribe
	public void onWasherUpdateReceived(WasherLocalUpdateRequestEvent event) {
		if(event != null) {
			washer = event.getWasher();
			UserPreferences.putWasherData(BA.getContext(),washer.serialize());
			BA.getEventBus().post(new WasherGetResponseEvent(washer));
		}
	}

	@Produce
	public WasherGetResponseEvent produceWasher() {
		if(washer == null){
			washer = Washer.deserialize(UserPreferences.getWasherData(BA.getContext()));
		}
		return new WasherGetResponseEvent(washer);
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
	}

	@Subscribe
	public void onUserSelfReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
	}

	@Subscribe
	public void onUserUpdateReceived(UserUpdateSelfResponseEvent event) {
	  if(event != null && event.getData() != null && event.getData().getResponse() != null)
		  user = event.getData().getResponse();
	}

	@Subscribe
	public void onFilterUpdateReceived(FilterUpdateRequestEvent event) {
		this.searchFilter = event.getSearchFilter();
		searchFilter.setMobile(UserPreferences.getUserPhone(BA.getContext()));
		BA.getEventBus().post(new FilterGetResponseEvent(searchFilter));
	}

	@Produce
	public FilterResponseEvent produceFilter() {
		if(searchFilter == null){
			searchFilter = new SearchFilter();
			searchFilter.setMobile(UserPreferences.getUserPhone(BA.getContext()));
		}
		return new FilterResponseEvent(searchFilter);
	}

	@Subscribe
	public void onNewBookingPushReceived(NewBookingPushRequestEvent event) {
		if(bookingPush == null && !TextUtils.isEmpty(UserPreferences.getPushBooking(BA.getContext())))
			bookingPush = NewPushInformation.deserialize(UserPreferences.getPushBooking(BA.getContext()));
		else if(bookingPush == null){
			bookingPush = new NewPushInformation(new HashMap<String,PushData>());
		}
//		if(bookingPush.getData().get(event.getData().getObjectId()) != null
//				&& !TextUtils.isEmpty(bookingPush.getData().get(event.getData().getObjectId()).getText()) ) {
		bookingPush.getData().put(event.getData().getObjectId(), event.getData());
		BA.getEventBus().post(new NewBookingPushResponseEvent(bookingPush));
		onBookingPushUpdateRequestEvent(new BookingPushUpdateRequestEvent());
//		}
	}

	@Subscribe
	public void onNewReviewPushReceived(NewReviewPushRequestEvent event) {
		if(reviewPush == null && !TextUtils.isEmpty(UserPreferences.getPushReview(BA.getContext())))
			reviewPush = NewPushInformation.deserialize(UserPreferences.getPushReview(BA.getContext()));
		else if(reviewPush == null){
			reviewPush = new NewPushInformation(new HashMap<String,PushData>());
		}

		reviewPush.getData().put(event.getData().getObjectId(),event.getData());
		onReviewPushUpdateRequestEvent(new ReviewPushUpdateRequestEvent());
		BA.getEventBus().post(new NewReviewPushResponseEvent(reviewPush));
	}

	@Subscribe
	public void onRemoveBookingPushReceived(RemoveBookingPushRequestEvent event) {

		if(bookingPush!= null && bookingPush.getData() != null && bookingPush.getData().get(event.getData().getObjectId()) != null) {
			bookingPush.getData().remove(event.getData().getObjectId());
			onBookingPushUpdateRequestEvent(new BookingPushUpdateRequestEvent());
			BA.getEventBus().post(new NewBookingPushResponseEvent(bookingPush));
		}
	}

	@Subscribe
	public void onRemoveBulkBookingPushReceived(RemoveBulkBookingPushRequestEvent event) {
		if(bookingPush!= null && bookingPush.getData() != null) {
			bookingPush.getData().clear();
			onBookingPushUpdateRequestEvent(new BookingPushUpdateRequestEvent());
			BA.getEventBus().post(new NewBookingPushResponseEvent(bookingPush));
		}
	}

	@Subscribe
	public synchronized void onBookingPushUpdateRequestEvent(BookingPushUpdateRequestEvent event){
		UserPreferences.putPushBooking(BA.getContext(),bookingPush.serialize());
	}

	@Subscribe
	public synchronized void onReviewPushUpdateRequestEvent(ReviewPushUpdateRequestEvent event){
		UserPreferences.putPushReview(BA.getContext(),reviewPush.serialize());
	}

	@Produce
	public NewBookingPushResponseEvent produceBookingPush() {
		if(bookingPush == null && !TextUtils.isEmpty(UserPreferences.getPushReview(BA.getContext()))){
			bookingPush = NewPushInformation.deserialize(UserPreferences.getPushBooking(BA.getContext()));
		}else if(bookingPush == null){
			bookingPush = new NewPushInformation(new HashMap<String,PushData>());
		}

		return new NewBookingPushResponseEvent(bookingPush);
	}

	@Produce
	public NewReviewPushResponseEvent produceReviewPush() {
		if(reviewPush == null && !TextUtils.isEmpty(UserPreferences.getPushReview(BA.getContext())))
			reviewPush = NewPushInformation.deserialize(UserPreferences.getPushReview(BA.getContext()));
		else if(reviewPush == null){
			reviewPush = new NewPushInformation(new HashMap<String,PushData>());
		}
		return new NewReviewPushResponseEvent(reviewPush);
	}

	public static Singleton getInstance() {
		if ( instance == null )
			instance = new Singleton();
		return instance;
	}

	@Subscribe
	public void onPlayRingtoneRequested(StartPlayRingtoneRequestEvent event){
		//TODO Temproary solution for long ringtone
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		if(alert == null){
			// alert is null, using backup
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

			// I can't see this ever being null (as always have a default notification)
			// but just incase
			if(alert == null) {
				// alert backup is null, using 2nd backup
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}
		}
		if(r == null || !(r != null && r.isPlaying())) {
			r = RingtoneManager.getRingtone(BA.getContext(), alert);
			r.play();
		}
	}

	@Subscribe
	public void onStopPlayRingtoneRequested(StopPlayRingtoneRequestEvent event) {
	  if(r != null && r.isPlaying()){
		  r.stop();
	  }
   }

	public static void displayToast(final String message) {
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
}
