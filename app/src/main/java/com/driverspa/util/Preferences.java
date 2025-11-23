package com.driverspa.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class Preferences {

	private static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";
//	private static final String APP_VERSION = "APP_VERSION";
	
	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
	}
	
	public static void putString(Context context, String key, String value) {
		getPreferences(context).edit().putString(key, value).commit();
	}

	public static String getString(Context context, String key) {
		return getPreferences(context).getString(key, null);
	}
	
	public static void putInt(Context context, String key, int value) {
		getPreferences(context).edit().putInt(key, value).commit();
	}
	
	public static int getInt(Context context, String key) {
		return getInt(context, key, 0);
	}
	
	public static int getInt(Context context, String key, int defaultValue) {
		return getPreferences(context).getInt(key, defaultValue);
	}

	public static long getLong(Context context, String key) {
		return getLong(context, key, 0);
	}

	public static long getLong(Context context, String key, long defaultValue) {
		return getPreferences(context).getLong(key, defaultValue);
	}

	public static void putLong(Context context, String key, long value) {
		getPreferences(context).edit().putLong(key, value).commit();
	}


	public static void putBoolean(Context context, String key, boolean value) {
		getPreferences(context).edit().putBoolean(key, value).commit();
	}
	
	public static boolean getBoolean(Context context, String key, boolean defaultValue) {
		return getPreferences(context).getBoolean(key, defaultValue);
	}
	
	public static void putAppVersion(Context context, String key) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		putInt(context, key, versionCode);
	}
}
