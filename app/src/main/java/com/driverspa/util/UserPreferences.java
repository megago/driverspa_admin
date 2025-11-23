package com.driverspa.util;

import android.content.Context;

public class UserPreferences extends Preferences {
	
	private static final String USER_DATA_APP_VERSION = "USER_DATA_APP_VERSION";
	private static final String IS_USER_LOGGED_IN = "IS_USER_LOGGED_IN";
	private static final String IS_ADMIN_LOGGED_IN = "IS_ADMIN_LOGGED_IN";
	private static final String USER_TOKEN = "USER_TOKEN";
	private static final String USER_ID = "USER_ID";
	private static final String USER_PHONE = "USER_PHONE";
	private static final String LANG = "lang";
	private static final String LOCATION = "LOCATION";
	private static final String NOTIFICATION = "NOTIFICATION";
	private static final String USER_DICTIONARY = "USER_DICTIONARY";	
	private static final String WASHER_DATA = "WASHER_DATA";
	private static final String BOOKS_DATA = "BOOKS_DATA";
	private static final String LOCAL_BOOKING_LIST = "LOCAL_BOOKING_LIST";
	private static final String CITY = "CITY";
	private static final String CITY_FOUND_BY_GPS = "CITY_FOUND_BY_GPS";
	private static final String PUSH_TOKEN = "PUSH_TOKEN";
	private static final String PUSH_BOOKING = "PUSH_BOOKING";
	private static final String PUSH_REVIEW = "PUSH_BOOKING";
	private static final String INFO_PAGE_SHOWN = "INFO_PAGE_SHOWN";
	private static final String GPSDATA = "GPSDATA";
	private static final String SMS_FOR_CLIENTS_DATA = "SMS_FOR_CLIENTS_DATA";
	private static final String PUSH_FOR_CLIENTS_DATA = "PUSH_FOR_CLIENTS_DATA";
	private static final String OWNER_WASHER_LIST = "OWNER_WASHER_LIST";
	private static final String ACTIVE_FARE_REQUESTS = "ACTIVE_FARE_REQUEST";


	private static final String ABOUT_US = "ABOUT_US";
	private static final String RECOVERY_SMS_SENT_TIME = "RECOVERY_SMS_SENT_TIME";


	public static void putRecoverySmsSentTime(Context context, long recoveryTime) {
		putLong(context, RECOVERY_SMS_SENT_TIME, recoveryTime);
	}

	public static void putActiveFareRequestBooking(Context context, String activeRequest) {
		putString(context, ACTIVE_FARE_REQUESTS, activeRequest);
	}

	public static long getRecoverySmsSentTime(Context context) {return getLong(context, RECOVERY_SMS_SENT_TIME,0);}


	private static void putLoggedInUserDataAppVersion(Context context) {
		putAppVersion(context, USER_DATA_APP_VERSION);
	}
	
	public static int getLoggedInUserDataAppVersion(Context context) {
		return getInt(context, USER_DATA_APP_VERSION, 0);
	}

	private static void putUserId(Context context, String userId) {
		putString(context, USER_ID, userId);
	}

	public static void putCityFoundByGPS(Context context, boolean check) {
		putBoolean(context, CITY_FOUND_BY_GPS, check);
	}

	public static void putLocalBookingList(Context context, String bookList) {
		putString(context, LOCAL_BOOKING_LIST, bookList);
	}

	public static void putPushToken(Context context, String token) {
		putString(context, PUSH_TOKEN, token);
	}

	public static String getActiveFareRequest(Context context) {
		return getString(context, ACTIVE_FARE_REQUESTS);
	}

	public static void putPushBooking(Context context, String token) {
		putString(context, PUSH_BOOKING, token);
	}
	public static void putPushReview(Context context, String token) {
		putString(context, PUSH_REVIEW, token);
	}

	public static void putBooksData(Context context, String data) {
		putString(context, BOOKS_DATA, data);
	}

	public static void putSMSForClientsData(Context context, String data) {
		putString(context, SMS_FOR_CLIENTS_DATA, data);
	}

	public static void putPUSHForClientsData(Context context, String data) {
		putString(context, PUSH_FOR_CLIENTS_DATA, data);
	}

	public static void putDeviceId(Context context, String token) {
		putString(context, PUSH_TOKEN, token);
	}

	public static void putAboutUS(Context context, String about) {
		putString(context, ABOUT_US, about);
	}

	public static boolean isCityFoundByGPS(Context context) {
		return getBoolean(context, CITY_FOUND_BY_GPS, false);
	}

	public static void putCity(Context context, String city) {putString(context, CITY, city);}

	public static String getCity(Context context) {
		return getString(context, CITY);
	}

	public static String getSMSForClientsData(Context context) {
		return getString(context, SMS_FOR_CLIENTS_DATA);
	}

	public static String getPUSHForClientsData(Context context) {
		return getString(context, PUSH_FOR_CLIENTS_DATA);
	}

	public static String getPushBooking(Context context) {
		return getString(context, PUSH_BOOKING);
	}

	public static String getBooksData(Context context) {
		return getString(context, BOOKS_DATA);
	}

	public static String getPushReview(Context context) {
		return getString(context, PUSH_REVIEW);
	}

	public static String getPushToken(Context context) {
		return getString(context, PUSH_TOKEN);
	}

	public static void putWasherData(Context context, String washer) {putString(context, WASHER_DATA, washer);}

	public static void putOwnerWasherListData(Context context, String washer) {putString(context, OWNER_WASHER_LIST, washer);}


	public static String getWasherData(Context context) {
		return getString(context, WASHER_DATA);
	}

	public static String getUserId(Context context) {return getString(context, USER_ID);}

	public static String getAboutUs(Context context) {return getString(context, ABOUT_US);}

	public static String getLocalBookingList(Context context) {return getString(context, LOCAL_BOOKING_LIST);}

	public static String getGPSData(Context context) {
		return getString(context, GPSDATA);
	}

	public static void putGPSdata(Context context, String userdata) {
		putString(context, GPSDATA, userdata);
	}

	private static void putUserPhone(Context context, String phone) {
		putString(context, USER_PHONE, phone);
	}
	
	public static String getUserPhone(Context context) {
		return getString(context, USER_PHONE);
	}
	
	public static boolean isUserLoggedIn(Context context) {
		boolean loggedInPreferences = getBoolean(context, IS_USER_LOGGED_IN, false);
		if( ! loggedInPreferences )// if boolean is false, return not logged in
			return false;	
		return true;
	}

	public static boolean isInfoPageShown(Context context) {
		boolean infoPage = getBoolean(context, INFO_PAGE_SHOWN, false);
		if( ! infoPage )
			return false;
		return true;
	}

	public static void onInfoPageShown(Context context){
		putBoolean(context, INFO_PAGE_SHOWN, true);
	}
	public static void onInfoPageNotShown(Context context){
		putBoolean(context, INFO_PAGE_SHOWN, false);
	}

	public static boolean isAdminLoggedIn(Context context) {
		boolean loggedInPreferences = getBoolean(context, IS_ADMIN_LOGGED_IN, false);
		if( ! loggedInPreferences )// if boolean is false, return not logged in
			return false;		
		return true;
	}

	
	public static void onUserLogin(Context context, String id, String phone, String token) {
		putBoolean(context, IS_USER_LOGGED_IN, true);
		putUserId(context, id);
		putUserPhone(context, phone);
		putUserToken(context, token);
		putLoggedInUserDataAppVersion(context);

	}

	public static void onTempUserLogin(Context context, String id, String phone) {
		putUserId(context, id);
		putUserPhone(context, phone);
		putLoggedInUserDataAppVersion(context);
	}

	public static void onAdminLogin(Context context,String id, String phone, String token) {
		putBoolean(context, IS_ADMIN_LOGGED_IN, true);
		putUserId(context, id);
		putUserPhone(context, phone);
		putUserToken(context, token);
		putLoggedInUserDataAppVersion(context);
		setUserNotification(context,true);
	}

	public static void onTempAdminLogin(Context context,String id, String phone) {
		putUserId(context, id);
		putUserPhone(context, phone);
		putLoggedInUserDataAppVersion(context);
	}

	public static void onUserLogout(Context context) {
		putBoolean(context, IS_USER_LOGGED_IN, false);
		putUserId(context, null);
		putUserPhone(context, null);
		putUserToken(context, null);
		putPushToken(context,null);
		putPushBooking(context,null);
		putCity(context, null);
	}
	
	public static void onAdminLogout(Context context) {
		putBoolean(context, IS_ADMIN_LOGGED_IN, false);
		putUserId(context, null);
		putCity(context,null);
		putWasherData(context,null);
		putUserPhone(context, null);
		putUserToken(context, null);
		putPushToken(context,null);
		putPushBooking(context,null);
//		putCity(context, null);
		putOwnerWasherListData(context,null);
		setUserNotification(context,false);
	}
	
	public static void onLocaleChange(Context context, String lang){
		putUserLocale(context, lang);
	}
	
	public static String getUserLocale(Context context){
		return getString(context,LANG);
	}

	public static void putUserLocale(Context context, String lang){
    	putString(context,LANG,lang);
    }

	public static boolean isUserLocationEnabled(Context context){
		return getBoolean(context, LOCATION, false);
	}

	public static void setUserLocation(Context context, boolean location){
    	putBoolean(context,LOCATION,location);
    }

	public static boolean isUserNotificationEnabled(Context context){
		return getBoolean(context, NOTIFICATION, false);
	}

	public static void setUserNotification(Context context, boolean notification){
    	putBoolean(context,NOTIFICATION,notification);
    }
    
	public static String getUserDictionary(Context context){
		return getString(context,USER_DICTIONARY);
	}

	public static String getOwnerWasherListData(Context context){
		return getString(context,OWNER_WASHER_LIST);
	}

	public static void putUserDictionary(Context context, String dictionary){
    	putString(context,USER_DICTIONARY,dictionary);
    }
	private static void putUserToken(Context context, String token) {
		putString(context, USER_TOKEN, token);
	}

	public static String getUserToken(Context context) {
		return getString(context, USER_TOKEN);
	}
	
}
