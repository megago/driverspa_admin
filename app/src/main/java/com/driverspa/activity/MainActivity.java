package com.driverspa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.splunk.mint.Mint;

import java.util.ArrayList;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminHomeActivity;
import com.driverspa.admin.activity.AdminNewWasherActivity;
import com.driverspa.admin.fragment.AdminRegistrationFragment;
import com.driverspa.admin.fragment.AdminVerificationFragment;
import com.driverspa.db.WashmeOrmLiteSqlHelper;
import com.driverspa.fragment.MainFragment;
//import com.driverspa.gcm.RegistrationIntentService;
import com.driverspa.model.CurrentGeoPosition;
import com.driverspa.model.UserLocation;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.UserLocationUpdateEvent;

/**
 * Used for logging in, checking whether user is logged in.
 * @author Yerzhan
 *
 */
public class MainActivity extends BaseActivity implements
		MainFragment.ActivityActions,
		AdminRegistrationFragment.ActivityActions,
		AdminVerificationFragment.ActivityActions{

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
		setContentView(R.layout.activity_main);

		// check if user is logged in
		boolean adminLoggedIn = processAdminLoginCheck();
		if( adminLoggedIn )
			return;

		if( savedInstanceState == null ) {
			openAdminRegistration();
		}

		// create database tables. Next time db request won't take much time
		new WashmeOrmLiteSqlHelper(BA.getContext());
	}

	private boolean processAdminLoginCheck() {

		boolean isLoggedIn = UserPreferences.isAdminLoggedIn(this);
		if( isLoggedIn ){
			openAdminHomeActivity();
		}
		return isLoggedIn;
	}

	@Override
	public void openAdminRegistration() {
		replaceFragment(new AdminRegistrationFragment());
	}

	@Override
	public void openVerification() {
		replaceFragment(new AdminVerificationFragment());
	}

	@Override
	public void openAdminHomeActivity() {
		registerGCM();
		finish();
		overridePendingTransition(0, 0);
		Intent intent = new Intent(this, AdminHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(OPENING_ANIMATION, false);
		startActivity(intent);
	}

	@Override
	public void openWizardActivity() {
//		registerGCM();
//		finish();
//		overridePendingTransition(0, 0);
		openAdminHomeActivity();
		Intent intent = new Intent(this, AdminNewWasherActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(OPENING_ANIMATION, false);
		startActivity(intent);
	}

	public void registerGCM() {

		UserLocation userLocation = new UserLocation();
		ArrayList<Double> lonLat;
		if (!TextUtils.isEmpty(UserPreferences.getGPSData(BA.getContext()))) {
			CurrentGeoPosition gpsData = CurrentGeoPosition.deserialize(UserPreferences.getGPSData(BA.getContext()));
			lonLat = new ArrayList<Double>();
			lonLat.add(gpsData.getLng());
			lonLat.add(gpsData.getLat());
			userLocation.setLonLat(lonLat);
		}
		String city;
		if (!TextUtils.isEmpty(UserPreferences.getCity(BA.getContext()))) {
			city = UserPreferences.getCity(BA.getContext());
			userLocation.setCity(city);
		}
		BA.getEventBus().post(new UserLocationUpdateEvent(userLocation));

//		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String PUSH_TOKEN = UserPreferences.getPushToken(this);//sharedPreferences.getString(RegistrationIntentService.PUSH_TOKEN, "");

//		if (TextUtils.isEmpty(PUSH_TOKEN) && checkPlayServices()) {
//			Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//				startForegroundService(intent); } else {
//				startService(intent); }
//		}

	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				ToastUtil.displayAtTop(this,"This device is not supported.");
			}
			return false;
		}
		return true;
	}

	@Override
	public void showChooseCity() {
		startActivity(new Intent(this, CityChooseActivity.class));
	}

}