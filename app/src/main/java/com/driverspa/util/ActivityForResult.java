package com.driverspa.util;

public class ActivityForResult {

	// REQUEST CODES - Activities
	public static int ACTIVITY_MAIN = 10;
	public static final int CLIENT_PHOTO_UPLOAD = 11;
	public static final int ADMIN_PHOTO_UPLOAD = 12;
	public static final int ACTIVITY_REVIEW = 13;
	public static final int ACTIVITY_GALLERY_PHOTOS = 17;	
	public static final int ACTIVITY_PHOTO_CHANGE = 15;
	public static final int ACTIVITY_ADDRESS = 20;
	public static final int ACTIVITY_ADDRESS_MAP = 21;
	public static final int ACTIVITY_PRICE = 22;
	public static final int ACTIVITY_PRICE_DETAIL = 23;
	public static final int ACTIVITY_ADDITIONAL_INFO = 24;

	public static final int ACTIVITY_SEARCH = 77;
	public static final int ACTIVITY_SEARCH_HAS_RESULT = 78;
	public static final int ACTIVITY_SEARCH_HAS_NO_RESULT = 78;

	
	public static final int ACTIVITY_WASHER_INFO= 40;
	public static final int ACTIVITY_TIMETABLE_BOOK = 25;
	public static final int ACTIVITY_TIMETABLE_INFO = 26;
	
	public static final int ACTIVITY_TIME_DEFINITION = 27;
	
	//ACTIONS
	public static int PHOTO_CHANGED_YES = ACTIVITY_PHOTO_CHANGE * 100 + 1;
	public static int GALLERY_PHOTO_CHANGED_YES = ACTIVITY_GALLERY_PHOTOS * 100 + 1;
	public static int GALLERY_PHOTO_CHANGED_NO = GALLERY_PHOTO_CHANGED_YES + 1;

	public static int ACTIVITY_ADDRESS_CHANGED_YES = ACTIVITY_ADDRESS * 100 + 1;
	public static int ACTIVITY_ADDRESS_CHANGED_NO = ACTIVITY_ADDRESS * 100 + 2;
	public static int ACTIVITY_ADDRESS_MAP_CHANGED_YES = ACTIVITY_ADDRESS_MAP * 100 + 1;
	public static int ACTIVITY_ADDRESS_MAP_CHANGED_NO = ACTIVITY_ADDRESS_MAP * 100 + 2;
	public static int ACTIVITY_PRICE_CHANGED_YES = ACTIVITY_PRICE * 100 + 1;
	public static int ACTIVITY_PRICE_CHANGED_NO = ACTIVITY_PRICE * 100 + 2;	
	public static int ACTIVITY_PRICE_DETAIL_CHANGED_YES = ACTIVITY_PRICE * 100 + 1;
	public static int ACTIVITY_PRICE_DETAIL_CHANGED_NO = ACTIVITY_PRICE * 100 + 2;	
	public static int ACTIVITY_ADDITIONAL_INFO_CHANGED_YES = ACTIVITY_PRICE * 100 + 1;
	public static int ACTIVITY_ADDITIONAL_INFO_CHANGED_NO = ACTIVITY_PRICE * 100 + 2;
	
	public static int ACTIVITY_TIME_DEFINITION_CHANGED_YES = ACTIVITY_TIME_DEFINITION * 100 + 1;
	public static int ACTIVITY_TIME_DEFINITION_CHANGED_NO = ACTIVITY_TIME_DEFINITION * 100 + 2;
	

}
