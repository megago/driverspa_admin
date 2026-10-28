package com.driverspa.admin.shift;

import android.content.Context;

import androidx.core.content.FileProvider;

/**
 * The app's FileProvider for files it shares (the shift Z-report PDF). A subclass of its own,
 * so it never clashes with a library's FileProvider in the merged manifest.
 */
public class ShiftFileProvider extends FileProvider {

	public static String authority(Context context) {
		return context.getPackageName() + ".shiftfiles";
	}
}
