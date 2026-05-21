package com.driverspa.util;

import android.text.TextUtils;
import android.util.Patterns;

import java.time.Instant;

public class UserInfoValidation {

	public final static boolean isValidEmail(CharSequence target) {
		if (TextUtils.isEmpty(target)) {
			return false;
		} else {
			return Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}
	
	public static boolean isValidPhoneNumber(CharSequence phoneNumber) {
	    if (!TextUtils.isEmpty(phoneNumber)) {
	        return Patterns.PHONE.matcher(phoneNumber).matches();
	    }
	    return false;
	}
	
	public static boolean isValidPassword(CharSequence password) {
		if( !TextUtils.isEmpty(password) ) {
			return password.length() > 0;
		}
		return false;	
	}
	
	/**
	 * Returns if user's name is valid
	 * @param name - user's name to be validated
	 * @return true if name is valid
	 */
	public static boolean isUserNameValid(CharSequence name) {
		if( !TextUtils.isEmpty(name) )
			return true;
		else
			return false;
	}
	
	/**
	 * Returns if city is valid
	 * @param city - city to be validated
	 * @return true if city is valid
	 */
	public static boolean isUserCityValid(CharSequence city) {
		if( !TextUtils.isEmpty(city) )
			return true;
		else
			return false;
	}
	
	public static boolean isUserBirhdayValid(Instant instant) {
		return instant != null;
	}
	
}
