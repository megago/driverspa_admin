package com.driverspa;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.squareup.otto.Subscribe;

import com.driverspa.activity.MainActivity;
import com.driverspa.fcm.MyFirebaseMessagingService;
import com.driverspa.util.Singleton;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.ApiErrorEvent;

public class BA extends BaseApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		getSingleton(); // initialize singleton
		getEventBus().register(this); // register for OTTO events
		initLocale();
		createDefaultNotificationChannel();
	}

	/**
	 * Android 8+ requires every notification to declare a channel up front.
	 * MyFirebaseMessagingService posts to channel id "default" — register it
	 * here once so the first push that arrives can find it.
	 */
	private void createDefaultNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					MyFirebaseMessagingService.DEFAULT_CHANNEL_ID,
					"DriverSpa уведомления",
					NotificationManager.IMPORTANCE_HIGH);
			channel.setDescription("Push-уведомления от DriverSpa");
			NotificationManager nm = getSystemService(NotificationManager.class);
			if (nm != null) {
				nm.createNotificationChannel(channel);
			}
		}
	}

	public static Singleton getSingleton() {
		return Singleton.getInstance();
	}

	@Subscribe
	public void onApiError(ApiErrorEvent event) {
		ToastUtil.displayShort(this, "Ошибка соединения");
		event.getRetrofitError().printStackTrace();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public static void restart() {
		final int DELAY = 200; //milliseconds
		Intent mStartActivity = new Intent(getContext(), MainActivity.class);
		mStartActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + DELAY, mPendingIntent);
		System.exit(2);
	}

}
