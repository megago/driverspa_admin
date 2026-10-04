package com.driverspa.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.messaging.FirebaseMessaging;
import com.splunk.mint.Mint;

import java.util.ArrayList;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminHomeActivity;
import com.driverspa.admin.activity.AdminNewWasherActivity;
import com.driverspa.admin.fragment.AdminCreatePasswordFragment;
import com.driverspa.admin.fragment.AdminPasswordLoginFragment;
import com.driverspa.admin.fragment.AdminPhoneFragment;
import com.driverspa.admin.fragment.AdminResetPasswordFragment;
import com.driverspa.admin.fragment.AdminVerificationFragment;
import com.driverspa.admin.fragment.AdminWhatsappFragment;
import com.driverspa.assist.UserSelfAssist;
import com.driverspa.db.DriverSpaOrmLiteSqlHelper;
import com.driverspa.fragment.MainFragment;
import com.driverspa.model.CurrentGeoPosition;
import com.driverspa.model.Device;
import com.driverspa.model.User;
import com.driverspa.model.UserLocation;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.PushRequestEvent;
import com.driverspa.util.otto.ws.UserLocationUpdateEvent;

/**
 * Used for logging in, checking whether user is logged in.
 * @author Yerzhan
 *
 */
public class MainActivity extends BaseActivity implements
		MainFragment.ActivityActions,
		AdminPhoneFragment.ActivityActions,
		AdminPasswordLoginFragment.ActivityActions,
		AdminCreatePasswordFragment.ActivityActions,
		AdminResetPasswordFragment.ActivityActions,
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
		new DriverSpaOrmLiteSqlHelper(BA.getContext());
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
		replaceFragment(new AdminPhoneFragment());
	}

	@Override
	public void openVerification() {
		replaceFragment(new AdminVerificationFragment());
	}

	@Override
	public void openWhatsappRegistration(String phone, String email, String companyName) {
		AdminWhatsappFragment fragment = new AdminWhatsappFragment();
		Bundle args = new Bundle();
		args.putString(AdminWhatsappFragment.ARG_PHONE, phone);
		args.putString(AdminWhatsappFragment.ARG_EMAIL, email);
		args.putString(AdminWhatsappFragment.ARG_COMPANY, companyName);
		fragment.setArguments(args);
		replaceFragment(fragment);
	}

	@Override
	public void openPasswordLogin(String phone) {
		AdminPasswordLoginFragment fragment = new AdminPasswordLoginFragment();
		Bundle args = new Bundle();
		args.putString(AdminPasswordLoginFragment.ARG_PHONE, phone);
		fragment.setArguments(args);
		replaceFragment(fragment);
	}

	@Override
	public void openCreatePassword() {
		replaceFragment(new AdminCreatePasswordFragment());
	}

	@Override
	public void openResetPassword(String phone) {
		AdminResetPasswordFragment fragment = new AdminResetPasswordFragment();
		Bundle args = new Bundle();
		args.putString(AdminResetPasswordFragment.ARG_PHONE, phone);
		fragment.setArguments(args);
		replaceFragment(fragment);
	}

	@Override
	public void openAdminHomeActivity() {
		registerFcm();
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

	/**
	 * Posts the user's last-known location to the event bus and asks
	 * Firebase for the current FCM registration token. The token is stored
	 * in UserPreferences; SplashActivity is the place that POSTs it to the
	 * backend (it has the User / carwashId available there).
	 */
	public void registerFcm() {

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

		if (!checkPlayServices()) {
			return;
		}

		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						Log.w("FCM", "getToken failed", task.getException());
						return;
					}
					String token = task.getResult();
					if (!TextUtils.isEmpty(token)) {
						UserPreferences.putPushToken(this, token);
						Log.d("FCM", "FCM token cached: " + token);
						// Register the device with the backend right after login
						// so pushes work without waiting for the next app launch.
						registerDeviceWithBackend(token);
					}
				});
	}

	/**
	 * Posts the freshly-obtained FCM token to the backend (PUT /v1/devices/{id}).
	 * Mirrors the device payload built in SplashActivity so a just-logged-in user
	 * is registered for push immediately.
	 */
	private void registerDeviceWithBackend(String token) {
		User user = UserSelfAssist.getUserFromDb(UserPreferences.getUserId(BA.getContext()));
		if (user == null) {
			return;
		}
		String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		String phoneModel = android.os.Build.MODEL;
		String androidVersion = android.os.Build.VERSION.RELEASE;
		String appVersion = "";
		try {
			appVersion = getApplicationContext().getPackageManager()
					.getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		BA.getEventBus().post(new PushRequestEvent(deviceId,
				new Device(phoneModel, "android", androidVersion, appVersion, token, "fcm",
						user.getDefaultCarwashId(), true, !user.isOwner())));
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

}