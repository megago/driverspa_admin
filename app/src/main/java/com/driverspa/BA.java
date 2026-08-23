package com.driverspa;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

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
					BA.str(R.string.notif_channel_name),
					NotificationManager.IMPORTANCE_HIGH);
			channel.setDescription(BA.str(R.string.notif_channel_desc));
			NotificationManager nm = getSystemService(NotificationManager.class);
			if (nm != null) {
				nm.createNotificationChannel(channel);
			}
		}
	}

	public static Singleton getSingleton() {
		return Singleton.getInstance();
	}

	/**
	 * Locale-aware string lookup usable from anywhere (activities, adapters,
	 * plain classes). Resolves against the language chosen via the per-app
	 * language API so strings are correct even when read from a non-activity
	 * context, on all supported API levels.
	 */
	public static String str(int resId) {
		return localizedResources().getString(resId);
	}

	/** Locale-aware {@link #str(int)} with format arguments. */
	public static String str(int resId, Object... formatArgs) {
		return localizedResources().getString(resId, formatArgs);
	}

	private static android.content.res.Resources localizedResources() {
		Context ctx = getContext();
		LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
		Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		Configuration cfg = new Configuration(ctx.getResources().getConfiguration());
		cfg.setLocale(locale);
		return ctx.createConfigurationContext(cfg).getResources();
	}

	@Subscribe
	public void onApiError(ApiErrorEvent event) {
		ToastUtil.displayShort(this, BA.str(R.string.connection_error));
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
