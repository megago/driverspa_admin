package com.driverspa.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {

	static Toast toast;

	public static void display(Context context, String message) {
		if(toast != null)
			toast.cancel();
		toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void displayAtTop(Context context, String message) {
		if(toast != null)
			toast.cancel();
		toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 60);
		toast.show();
	}

	public static void display(Context context, int messageResourceId) {
		if(toast != null)
			toast.cancel();
		toast = Toast.makeText(context.getApplicationContext(), messageResourceId, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void displayAtTop(Context context, int messageResourceId) {
		toast = Toast.makeText(context.getApplicationContext(), messageResourceId, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 60);
		toast.show();
	}

	public static void displayShort(Context context, String message) {
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static void displayShortAtTop(Context context, String message) {
		toast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 60);
		toast.show();
	}

}
