package com.driverspa;

import android.app.Application;
import android.content.Context;
//import android.support.multidex.MultiDex;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.ThreadEnforcer;

import com.driverspa.model.NotificationType;
import com.driverspa.util.HttpClient;
import com.driverspa.util.JsonUtil;
import com.driverspa.util.LocaleManager;
import com.driverspa.util.MainOttoThreadBus;
import com.driverspa.util.UserPreferences;

public class BaseApplication extends Application {

	public static boolean DEBUG = true;
	private static MainOttoThreadBus eventBus; // otto - for event registration
	private static Context context;
	private static Reference reference;
	private static NotificationType notificationType = NotificationType.NOTHING;
	
	@Override 
	public void onCreate() {
		super.onCreate();
		context = this;
		init();	
	}

	private void init() {
		HttpClient.init(getApplicationContext(), getCacheSize());
//		HttpClient.setPicassoDebugging(DEBUG);		
		eventBus = new MainOttoThreadBus(ThreadEnforcer.MAIN);			
	}

	private int getCacheSize() {
		return getResources().getInteger(R.integer.image_cache_size);
	}
	
	public static Context getContext(){
        return context;
    }
	
	public static MainOttoThreadBus getEventBus() {
        return eventBus;
    }

	public static Reference getReference(){
		return JsonUtil.deserializeReference(UserPreferences.getUserDictionary(context));
	}
	
	public static void setReference(Reference reference){
		BaseApplication.reference = reference;
		UserPreferences.putUserDictionary(context, JsonUtil.serialize(reference));
	}
	
	public static OkHttpClient getHttpClient() {
		return HttpClient.getHttpClient();
	}

	/**
	 * Switch the app UI language. Delegates to {@link LocaleManager}, which uses the
	 * AndroidX per-app language API (persisted + auto-applied to every activity).
	 *
	 * @param lang a resource tag: {@link LocaleManager#KK}, {@link LocaleManager#RU}
	 *             or {@link LocaleManager#EN}.
	 */
	public static void setLocale(String lang){
		LocaleManager.apply(lang);
	}

	/**
	 * Called once at startup. Defaults fresh installs to Kazakh and keeps the
	 * backend Accept-Language code in sync with the chosen UI language.
	 */
	public static void initLocale(){
		LocaleManager.ensureDefault(context);
	}

	@Override
	protected void attachBaseContext(Context context) {
		super.attachBaseContext(context);
//		MultiDex.install(this);
	}

	public static void setNotificationType(NotificationType notificationType) {
		BaseApplication.notificationType = notificationType;
	}
	public static NotificationType getNotificationType() {
		return notificationType;
	}

}