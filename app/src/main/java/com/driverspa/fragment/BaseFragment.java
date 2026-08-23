package com.driverspa.fragment;
import com.driverspa.R;
import com.driverspa.BA;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.driverspa.listener.OnActionbarTitleChange;

public abstract class BaseFragment extends Fragment {

	protected boolean gpsSettingsShown;
	private OnActionbarTitleChange actionbarTitleChange;
	private ProgressBar progressBar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void closeKeyboard() {
		try {
			InputMethodManager inputManager = (InputMethodManager)
					getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

			inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
		catch (Exception e){}
	}
	protected void addProgressBar(ListView listView) {
		listView.setEmptyView(getProgressBar());
	}
	
	protected void removeProgressBar() {
		ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
		root.removeView(progressBar);
	}
	
	protected void addProgressBar(GridView gridView) {
		gridView.setEmptyView(getProgressBar());
	}
	
	private ProgressBar getProgressBar() {
		FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		flp.gravity = Gravity.CENTER;

		// Create a progress bar to display while the list loads
		progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleLarge);
		progressBar.setIndeterminate(true);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
		root.addView(progressBar, flp );
		
		return progressBar;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		actionbarTitleChange = (OnActionbarTitleChange) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		actionbarTitleChange = null;
	}

	/**
	 * Sets action bar title
	 */
	public void setTitle(String title) {
		if ( actionbarTitleChange != null )
			actionbarTitleChange.setTitle(title);
	}

	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new MaterialAlertDialogBuilder(getActivity());

		// Setting Dialog Title
		alertDialog.setTitle(BA.str(R.string.gps_settings));

		// Setting Dialog Message
		alertDialog.setMessage(BA.str(R.string.gps_off_enable));

		// On pressing the Settings button.
		alertDialog.setPositiveButton(BA.str(R.string.menu_settings), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog,int which) {
//            	mContext.startActivity(new Intent(mContext, ClientSettingsActivity.class).putExtra(ClientBaseActivity.OPENING_ANIMATION, false));
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				getActivity().startActivity(intent);
				gpsSettingsShown = true;
			}
		});

		// On pressing the cancel button
		alertDialog.setNegativeButton(BA.str(R.string.cancel_word), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				gpsSettingsShown = false;
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}
	
}
