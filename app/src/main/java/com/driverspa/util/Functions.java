package com.driverspa.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.Reference;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Box;
import com.driverspa.model.Staff;
import com.driverspa.model.User;
import com.driverspa.model.Washer;

public class Functions {

	private static final String PREF = "JewUPPPref";
	public static final String DATE = "DATE";
	public static final String TIME = "TIME";

	static public void savePreferences(Context context, String key, String value) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	static public String loadPreferences(Context context, String key) {
		SharedPreferences mySharedPreferences = context.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
		return mySharedPreferences.getString(key, null);
	}

	public static String Split(String str, String delim, int valindx) {
		int i = 0;
		int itemindx = 0;
		String strOut = new String("");
		while (str.indexOf(delim, i) > -1 || i <= str.length()) {
			if (str.indexOf(delim, i) > -1)
				strOut = str.substring(i, str.indexOf(delim, i));
			else
				strOut = str.substring(i);
			if (itemindx == valindx)
				return strOut;
			i = str.indexOf(delim, i) + delim.length();
			itemindx = itemindx + 1;
		}
		return "";
	}

	public static String shortenName(String name){
		if(!TextUtils.isEmpty(name)){
			if(name.length() >=20 ){
				return name.substring(0,20);
			}
		}
		return name;
	}

	public static String getUserRole(Washer washer,String userId){
		if(washer!= null && washer.getStaffs() != null && washer.getStaffs().size() > 0){
			return washer.getStaffs().stream()
					.filter(staff -> staff.getUser() != null && staff.getUser().contains(userId))
					.map(Staff::getRole)
					.findFirst()
					.orElse("");
		}
		return "";
	}

	public static String getUserStatus(Washer washer,String userId){
		if(washer!= null && washer.getStaffs() != null && washer.getStaffs().size() > 0){
			return washer.getStaffs().stream()
					.filter(staff -> staff.getUser() != null && staff.getUser().contains(userId))
					.map(Staff::getStatus)
					.findFirst()
					.orElse("");
		}
		return "";
	}

	public static boolean isUserHasAccess(Washer washer, User user,Context context){
		if(user != null && !user.isOwner() && washer!= null && washer.getStaffs() != null && washer.getStaffs().size() > 0){
			boolean noAccess = washer.getStaffs().stream()
					.anyMatch(staff -> staff.getUser() != null && staff.getUser().contains(user.getId()) && "operator".equals(staff.getRole()));
			if(noAccess){
				ToastUtil.display(context,"У вас нет прав доступа");
				return false;
			}
		}
		return true;
	}




	public static boolean isUserHasAccess(Washer washer, User user){
		if(user != null && !user.isOwner() && washer!= null && washer.getStaffs() != null && washer.getStaffs().size() > 0){
			return washer.getStaffs().stream()
					.noneMatch(staff -> staff.getUser() != null && staff.getUser().contains(user.getId()) && "operator".equals(staff.getRole()));
		}
		return true;
	}

	public static Staff getWasherStaff(Washer washer,String userId){
		if(washer!= null && washer.getStaffs() != null && washer.getStaffs().size() > 0){
			return washer.getStaffs().stream()
					.filter(staff -> staff.getUser() != null && staff.getUser().contains(userId))
					.findFirst()
					.orElse(null);
		}
		return null;
	}

	public static LinkedHashMap<Integer,Integer> getGroupedServices(List<Integer> services){
		if(services == null){
			return new LinkedHashMap<>();
		}
		return services.stream().collect(Collectors.toMap(
				service -> service,
				service -> 1,
				Integer::sum,
				LinkedHashMap::new
		));
	}

	public static String getStaffName(List<Staff> staffs, String staffId){
		if(staffs != null) {
			return staffs.stream()
					.filter(staff -> staffId.equals(staff.getUid()))
					.map(staff -> staff.getFirstName() + " " + (!TextUtils.isEmpty(staff.getLastName().trim()) ? staff.getLastName() : ""))
					.findFirst()
					.orElse("");
		}
		return "";
	}


	public static LinkedHashMap<String,Integer> getGroupedGroupServices(List<String> services){
		if(services == null){
			return new LinkedHashMap<>();
		}
		return services.stream().collect(Collectors.toMap(
				service -> service,
				service -> 1,
				Integer::sum,
				LinkedHashMap::new
		));
	}


	public boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}

	public static boolean checkOnline(Activity activity) {
		ConnectivityManager connMgr = (ConnectivityManager) activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		boolean online = (networkInfo != null && networkInfo.isConnected());
		// if (!online)
		// Toast.makeText(activity,
		// "Настройте подключение к Интернету, чтобы пользоваться приложением",
		// Toast.LENGTH_SHORT).show();
		return online;
	}
	
	
	public static float dipToPixels(Context context, float dipValue) {
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}
	
	public static float dipToPixels(DisplayMetrics metrics, float dipValue) {	  
	    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}
	
	public static String maskPhoneNumber(String phoneNumber, String mask) {

		// format the number
		int index = 0;
		StringBuilder maskedNumber = new StringBuilder();
		for (int i = 0; i < mask.length(); i++) {
			char c = mask.charAt(i);
			if (c == '#') {
				maskedNumber.append(phoneNumber.charAt(index));
				index++;
			} else if (c == 'x') {
				maskedNumber.append(c);
				index++;
			} else {
				maskedNumber.append(c);
			}
		}

//		return cardNumber;
		// return the masked number
		return maskedNumber.toString();
	}
	
    public static void showSettingsAlert(final Context mContext){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Настройки GPS");

        // Setting Dialog Message
        alertDialog.setMessage("GPS отключен. Хотите включить?");

        // On pressing the Settings button.
        alertDialog.setPositiveButton("Настройки", new DialogInterface.OnClickListener() {
        	
            public void onClick(DialogInterface dialog,int which) {
//            	mContext.startActivity(new Intent(mContext, ClientSettingsActivity.class).putExtra(ClientBaseActivity.OPENING_ANIMATION, false));
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
	
	public static String formatTimeInterval(int fromH,int fromM,int toH,int toM){
		String returnStr = "";
		returnStr = String.format("%02d", fromH)+":"+String.format("%02d", fromM);  
		returnStr += " - "+String.format("%02d", toH)+":"+String.format("%02d", toM);
		return returnStr;	
	}
	
	public static HashMap<String,String> formatUTCDate(Date date){		 
		HashMap<String,String> returnMap = new HashMap<String,String>();		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String utcTimeStr = formatter.format(date.getTime());
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date utcDate;
		try {
			
 		utcDate = df.parse(utcTimeStr);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
		dateFormatter.setTimeZone(TimeZone.getDefault());
		SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
		timeFormatter.setTimeZone(TimeZone.getDefault());
		
		String dateStr = dateFormatter.format(utcDate);		
		String timeStr = timeFormatter.format(utcDate);
		
		returnMap.put(DATE, dateStr);
		returnMap.put(TIME, timeStr);
		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

      return returnMap;
	}

	public static Date getUTCDate(Date date){
		HashMap<String,String> returnMap = new HashMap<String,String>();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String utcTimeStr = formatter.format(date.getTime());
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date utcDate = null;
		try {

			utcDate = df.parse(utcTimeStr);
//			SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
//			dateFormatter.setTimeZone(TimeZone.getDefault());
//			SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
//			timeFormatter.setTimeZone(TimeZone.getDefault());
//
//			String dateStr = dateFormatter.format(utcDate);
//			String timeStr = timeFormatter.format(utcDate);
//
//			returnMap.put(DATE, dateStr);
//			returnMap.put(TIME, timeStr);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return utcDate;
	}


	public static String getCityDescription(String cityCode){
		return BA.getReference().getCities().stream()
				.filter(city -> city.getCode().equals(cityCode))
				.map(Reference.City::getTitle)
				.findFirst()
				.orElse("");
	}
	public static LatLng getCityLatLng(String cityCode){
		return BA.getReference().getCities().stream()
				.filter(city -> city.getCode().equals(cityCode))
				.findFirst()
				.map(city -> new LatLng(Double.parseDouble(city.getLonLat().get(1)), Double.parseDouble(city.getLonLat().get(0))))
				.orElse(null);
	}

	public static String LPad(String str, Integer length, char car) {
		return str
				+
				String.format("%" + (length - str.length()) + "s", "")
						.replace(" ", String.valueOf(car));
	}

	public static String RPad(String str, Integer length, char car) {
		return String.format("%" + (length - str.length()) + "s", "")
				.replace(" ", String.valueOf(car))
				+
				str;
	}

	public static String getBoxName(Washer washer, String boxId){
		if (washer != null && washer.getBoxSettings() != null) {
			return washer.getBoxSettings().stream()
					.filter(bs -> bs.getUid().equals(boxId))
					.map(Box::getBoxName)
					.findFirst()
					.orElse("");
		}
		return "";
	}

	public static String getRussianFineLabel(String digitStr, String [] labels) {
		long n = 0;
		try {
			n = Long.parseLong(digitStr);
		} catch (Exception e) {
			//do nothing, default values are given
		}

		n = Math.abs((int)n) % 100;
		long n1 = n % 10;
		if (n > 10 && n < 20) return labels[2];
		if (n1 > 1 && n1 < 5) return labels[1];
		if (n1 == 1) return labels[0];
		return labels[2];
	}

	public static String stripNonDigits(
			final CharSequence input /* inspired by seh's comment */){
		final StringBuilder sb = new StringBuilder(
				input.length() /* also inspired by seh's comment */);
		for(int i = 0; i < input.length(); i++){
			final char c = input.charAt(i);
			if(c > 47 && c < 58){
				sb.append(c);
			}
		}
		return sb.toString();
	}

    public static ArrayList<Box> filterDisableddBoxes(ArrayList<Box> boxes){
		if(boxes != null && boxes.size() > 0){
			return boxes.stream()
					.filter(b -> !b.isStatusDisabled())
					.collect(Collectors.toCollection(ArrayList::new));
		}
		else
			return boxes;
	}

	public static String formatPhoneNumber(String mask, String phoneNumber){
		if(!TextUtils.isEmpty(phoneNumber) && phoneNumber.length() > 2 && phoneNumber.substring(0,1).equals("+"))
		    phoneNumber = phoneNumber.substring(2,phoneNumber.length());
		String formatted = phoneNumber;
		try {
			MaskedFormatter formatter = new MaskedFormatter(mask);
			formatter.setValueContainsLiteralCharacters(false);
			formatter.setPlaceholderCharacter((char)1);
			// get a string with applied mask and placeholder chars
			String value = formatter.valueToString(phoneNumber);

			try{

				// find first placeholder
				value = value.substring(0, value.indexOf((char)1));

				//process a mask char
				if(value.charAt(value.length()-1) ==
						mask.charAt(value.length()-1)){
					value = value.substring(0, value.length() - 1);
				}
			}
			catch(Exception e){}
			formatted = value;

		} catch (ParseException e) {
			//the entered value does not match a mask
//			int offset = e.getErrorOffset();
//			formatted = removeCharAt(formatted, offset);
		}

		return formatted;
	}

	public static String formatPriceDetail(List<BookInfo.PriceDetail> priceDetails, Double price){
		    DecimalFormat decimalFormatter = new DecimalFormat("#,###.##");
		     String result = "";
		     if(priceDetails != null ){
				 for(int i = 0; i < priceDetails.size(); i++){
					BookInfo.PriceDetail p = priceDetails.get(i);
					if(i==0) result += "(";
					switch(p.getPaymentType()){
						case "C":
							result += "налич. ";
							break;
						case "DA":
							result += "переч. ";
							break;
						case "T":
							result += "переч. ";
							break;
						case "B":
							result += "бон. ";
							break;
						case "CD":
							result += "карт. ";
							break;
					}
					result += decimalFormatter.format(p.getAmount())+" ₸"+", ";
					if(i == priceDetails.size()-1) {
						result = result.substring(0,result.length()-2);
						result += ")";
					}
				  }
			 }
		    else{
				result = "(налич.: "+ decimalFormatter.format(price)+")";
			}
		return result;
	}

	public static String removeCharAt(String s, int pos) {

		StringBuffer buffer = new StringBuffer(s.length() - 1);
		buffer.append(s.substring(0, pos)).append(s.substring(pos + 1));
		return buffer.toString();

	}

	public static Drawable getBookStatusBGColor(Context context, String status){
		switch(status){
			case Constants.APPROVED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_green);
			case Constants.QUEUED_APPROVED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_green);
			case Constants.QUEUED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_orange);
			case Constants.REJECTED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_red);
			case Constants.NOCOME:
				return context.getResources().getDrawable(R.drawable.background_tag_item_red);
			case Constants.CANCELED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_red);
			case Constants.QUEUED_REJECTED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_red);
			case Constants.PENDING:
				return context.getResources().getDrawable(R.drawable.background_tag_item_orange);
			case Constants.FINISHED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_blue_light);
			case Constants.QUEUED_FINISHED:
				return context.getResources().getDrawable(R.drawable.background_tag_item_blue_light);
		}

		return context.getResources().getDrawable(R.drawable.background_tag_item_white);
	}

	public static Integer getBookStatusTextColor(Context context, String status){
		switch(status){
			case Constants.APPROVED:
				return context.getResources().getColor(R.color.Green);
			case Constants.QUEUED_APPROVED:
				return context.getResources().getColor(R.color.Green);
			case Constants.QUEUED:
				return context.getResources().getColor(R.color.OrangeRed);
			case Constants.REJECTED:
				return context.getResources().getColor(R.color.DarkRed);
			case Constants.NOCOME:
				return context.getResources().getColor(R.color.DarkRed);
			case Constants.QUEUED_REJECTED:
				return context.getResources().getColor(R.color.DarkRed);
			case Constants.CANCELED:
				return context.getResources().getColor(R.color.DarkRed);
			case Constants.PENDING:
				return context.getResources().getColor(R.color.OrangeRed);
			case Constants.FINISHED:
				return context.getResources().getColor(R.color.DarkBlue);
			case Constants.QUEUED_FINISHED:
				return context.getResources().getColor(R.color.DarkBlue);
		}

		return context.getResources().getColor(R.color.BlueLight);
	}


	public static Date getEndOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	public static Date getStartOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
