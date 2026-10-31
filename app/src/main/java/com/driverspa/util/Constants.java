/**
 * 
 */
package com.driverspa.util;
import com.driverspa.R;
import com.driverspa.BA;

import android.graphics.Typeface;

import java.util.HashMap;

/**
 * @author Yerzhan Tanatov
 * date: 09.10.2014
 * time: 17:36:10
 */
public class Constants {

    public final static int VIEW_HEADER = 2;
    public final static int VIEW_FOOTER = 1;
    public final static int VIEW_NORMAL = 0;

    public final static String WASHER_BASE = "WASHER_BASE";
	public final static String CAR_TYPE = "CAR_TYPE";	
	public final static String SERVICE = "SERVICE";
	public final static String GROUP = "GROUP";
	public final static String EXTRA_WASHER_ID = "EXTRA_WASHER_ID";
    public static final String CLIENT_INFO = "CLIENT_INFO";
    public static final String STAFF_INFO = "STAFF_INFO";
    public static final String WORKSHIFT_INFO = "WORKSHIFT_INFO";
    public final static String EXTRA_BOOK = "EXTRA_BOOK";
	public final static String EXTRA_BOX_ID = "EXTRA_BOX_ID";
	public final static String EXTRA_BOX_NAME = "EXTRA_BOX_NAME";
    public final static String EXTRA_BOX_WASHER_PERSON = "EXTRA_BOX_WASHER_PERSON";
    public static final String EXTRA_BOOKING_ID = "book_id";
    public static final String EXTRA_PUSH_TYPE = "EXTRA_PUSH_TYPE";
    public static final String EXTRA_PUSH_MESSAGE = "EXTRA_PUSH_MESSAGE";
    public static final String EXTRA_BOOK_DAY = "EXTRA_BOOK_DAY";
    public static final String BOOKING_INFO_TYPE = "book_info_type";
    public static final String FIRST_BOOK = "first_book";    
    public static final String PENDING = "pending";
    public static final String FINISHED = "finished";
    public static final String APPROVED = "approved";
    public static final String TEST = "test";
    public static final String REJECTED = "rejected";
    public static final String STARTED = "started";
    public static final String NOCOME = "nocome";    
    public static final String CANCELED = "canceled";
    public static final String STATUS = "STATUS";
    public static final String QUEUED = "queued";
    public static final String QUEUED_APPROVED = "queued_approved";
    public static final String QUEUED_REJECTED = "queued_rejected";
    public static final String QUEUED_FINISHED = "queued_finished";
    public static final String EXPRESS = "express";
    public static final String EXPRESS_REJECTED = "express_approved";
    public static final String EXPRESS_CANCELED = "express_rejected";

    /** Payment types of a booking's price_details. */
    public static final String PAYMENT_CASH = "C";
    public static final String PAYMENT_CARD = "CD";
    public static final String PAYMENT_KASPI_QR = "QR";
    public static final String PAYMENT_DEPOSIT = "DA";
    public static final String PAYMENT_BONUS = "B";
    /** On account of a corporate client (docs/corporate-clients.md): no money taken. */
    public static final String PAYMENT_CORPORATE = "CO";
    /** Kind of a car wash's payment QR (image_upload/payment_qr). */
    public static final String PAYMENT_QR_KASPI = "kaspi_qr";

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    public static final String HYBRID = "hybrid";
    public static final String BOTH = "both";

    public static final int ZOOM_LEVEL = 14;    
    public static final String URL_PREFIX = "/api/v1/public_carwash/";
    public static final String URL_COMPANY_CLIENT_PREFIX = "/api/v1/company_clients/";
    public static final int GRID_TIME_INTERVAL = 30;
    public static final int SLOT_TIME_INTERVAL = 15;
    public static final int DAY_MINUTES = 1440;
    public static final String SLOT_AVAILABLE = "+";
    public static final String SLOT_NOT_AVAILABLE = "-";
    public static final String SLOT_BUSY = "*";
    public static final String TODAY = "today";
    public static final String TOMORROW = "tomorrow";
    public static final String FOR_DATE = "for_date";
    public final static String GRID_ID = "grid_id";
    public final static String GRID_TIME = "grid_time";
    public final static String GRID_BOX_ID = "grid_box_id";
    public final static String GRID_EXPIRED = "grid_box_id";
    public final static String GRID_TITLE = "grid_title";
    public final static String GRID_STATUS = "grid_status";
    public final static String GRID_BOOK_ID = "grid_book_id";

    public final static String ITEM_NAME = "item_name";
    public final static String ITEM_VALUE = "item_value";

    public static final String WASHER_DATA_TO_BOOK = "WASHER_DATA";
    public static final String WASHER_BOOKING_REQUEST_DATA = "WASHER_BOOKING_DATA";
    
    public static final String WASHER_ID = "WASHER_ID";
    public static final String WASHER_NAME = "WASHER_NAME";
    public static final String REQUEST_CODE = "REQUEST_CODE";    
	public static final String WASHER_DATA = "WASHER_DATA";

	public static final String BOOKING_CREATED = "booking_created";
	public static final String INFORMATION_WITH_UPDATE = "info_with_update";
	public static final String INFORMATION = "info";
	public static final String REVIEW_CREATED = "review_created";
	public static final String BOOKING_FINISH_INFORM = "booking_finish_inform";


	public static final String CLIENT_ALL_SMS = "client_all_sms";
	public static final String CLIENT_DRIVERSPA_PUSH = "client_driverspa_push";
	public static final String CLIENT_SMS = "client_sms";
	public static final String COMPANY = "company";
	public static final String ALL = "all";

	public static final String INFO = "info";
	public static final String CAMPAIGN = "campaign";
	public static final String CARWASH = "carwash";
	public static final String PRICE_DECREASE = "price_decrease";


    public static final String NEWS_PENDING = "pending";
    public static final String NEWS_SENT = "sent";
    public static final String NEWS_ERROR = "error";
    public static final String NEWS_PARTIAL_SENT = "partial_sent";

    public static final String DIRECTOR = "director";
    public static final String OPERATOR = "operator";
    public static final String CARWASHER = "carwasher";

    public static final String WORKS = "works";
    public static final String VACATION = "vacation";
    public static final String RESIGNED = "resigned";

    public static final String LOG_TAG = "project_debug";
	
	public static Typeface PFSquareSansPro_Bold;
	public static Typeface PFSquareSansPro_Light;
	public static Typeface PFSquareSansPro_Regular;
	
	public static String host = "HOST";
	public class request_url{		
		public static final String add = "http://";		
	}
	
	public static final HashMap<String, String> bookStatus;
	public static final HashMap<String, String> newsStatus;
	public static final HashMap<String, String> newsType;
	public static final HashMap<String, String> newsTarget;
    public static final HashMap<String, String> staffRole;
    public static final HashMap<String, String> staffStatus;
    public static HashMap<String,String> boxTypeMap = new HashMap<String,String>();

	static{

        //Book status map
		bookStatus = new HashMap<String,String>();
		bookStatus.put(QUEUED, BA.str(R.string.in_queue));
		bookStatus.put(QUEUED_APPROVED, BA.str(R.string.in_progress_status));
		bookStatus.put(QUEUED_REJECTED, BA.str(R.string.cancelled_status));
		bookStatus.put(QUEUED_FINISHED, BA.str(R.string.completed_status));
		bookStatus.put(PENDING, BA.str(R.string.pending_status));
		bookStatus.put(FINISHED, BA.str(R.string.completed_status));
		bookStatus.put(APPROVED, BA.str(R.string.approved_status));
		bookStatus.put(REJECTED, BA.str(R.string.rejected_status));
		bookStatus.put(STARTED, BA.str(R.string.started_status));
		bookStatus.put(NOCOME, BA.str(R.string.client_no_show_status));
		bookStatus.put(CANCELED, BA.str(R.string.cancelled_by_client));
		bookStatus.put(EXPRESS, BA.str(R.string.quick_booking));
		bookStatus.put(EXPRESS_CANCELED, BA.str(R.string.booking_cancelled_status));
		bookStatus.put(EXPRESS_REJECTED, BA.str(R.string.booking_rejected_status));

        //Box type map
        boxTypeMap.put(HYBRID,BA.str(R.string.hybrid));
        boxTypeMap.put(ONLINE,BA.str(R.string.online_booking_live_queue));
        boxTypeMap.put(OFFLINE,BA.str(R.string.live_queue));

        newsTarget = new HashMap<String,String>();
        newsTarget.put(CLIENT_ALL_SMS, BA.str(R.string.sms_all_clients));
        newsTarget.put(CLIENT_DRIVERSPA_PUSH, BA.str(R.string.push_registered_clients));
        newsTarget.put(CLIENT_SMS, BA.str(R.string.sms_unregistered_clients));
        newsTarget.put(COMPANY, BA.str(R.string.company_notification));
        newsTarget.put(ALL, BA.str(R.string.all_driverspa_clients));

        newsType = new HashMap<String,String>();
        newsType.put(INFO, BA.str(R.string.informational));
        newsType.put(CAMPAIGN, BA.str(R.string.promo));
        newsType.put(CARWASH, BA.str(R.string.new_wash));
        newsType.put(PRICE_DECREASE, BA.str(R.string.price_dumping));

        newsStatus = new HashMap<String,String>();
        newsStatus.put(NEWS_PENDING, BA.str(R.string.sending_dots));
        newsStatus.put(NEWS_SENT, BA.str(R.string.sent_status));
        newsStatus.put(NEWS_ERROR, BA.str(R.string.err_send));
        newsStatus.put(NEWS_PARTIAL_SENT, BA.str(R.string.sent_partially));


        staffRole = new HashMap<String,String>();
        staffRole.put(DIRECTOR, BA.str(R.string.role_director));
        staffRole.put(OPERATOR, BA.str(R.string.role_operator));
        staffRole.put(CARWASHER, BA.str(R.string.role_washer));

        staffStatus = new HashMap<String,String>();
        staffStatus.put(WORKS, BA.str(R.string.status_working));
        staffStatus.put(VACATION, BA.str(R.string.status_on_leave));
        staffStatus.put(RESIGNED, BA.str(R.string.status_dismissed));

    }

}
