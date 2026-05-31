package com.driverspa.admin.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.R;
import com.driverspa.listener.OnActionbarTitleChange;

public abstract class AdminBaseFragment extends Fragment {
	
	private OnActionbarTitleChange actionbarTitleChange;
	private ProgressBar progressBar;
	private ProgressDialog pd = null;
	public static final String OPENING_ANIMATION = "OPENING_ANIMATION";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void closeKeyboard() {
		Activity activity = getActivity();
		if (activity == null) return;
		View focused = activity.getCurrentFocus();
		if (focused == null) return;
		InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputManager == null) return;
		inputManager.hideSoftInputFromWindow(focused.getWindowToken(),
                   InputMethodManager.HIDE_NOT_ALWAYS);
	}
	protected void addProgressBar(ListView listView) {		
//		getProgressBar().setVisibility(View.VISIBLE);
		listView.setEmptyView(getProgressBar());
	}

	/**
	 * Play activity open animation
	 */
	public static void overridePendingTransitionWithCommoOpeningTransition(Activity baseActivity) {
		baseActivity.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
	}
	
	/**
	 * Play activity close animation
	 */
	protected void overridePendingTransitionWithCommonCloseTransition() {
		getActivity().overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
	}

	protected void addEmptyTextView(ListView listView,String text) {
		View view = listView.getEmptyView();
		if(view != null)
		   view.setVisibility(View.GONE);
		listView.setEmptyView(getEmtpyTextView(text));
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
		progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyle);
		progressBar.setIndeterminate(true);

		// Must add the progress bar to the root of the layout
		ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
		root.addView(progressBar, flp );
		
		return progressBar;
	}

	private TextView getEmtpyTextView(String text){
		TextView textView;
		FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		flp.gravity = Gravity.CENTER;
		int horizontalMargin = (int) getResources().getDimension(R.dimen.horizontal_margin_double);
		flp.leftMargin = horizontalMargin;
		flp.rightMargin = horizontalMargin;

		textView = new TextView(getActivity());
		textView.setText(text);
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(getResources().getColor(R.color.White));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));

		ViewGroup root = (ViewGroup) getActivity().findViewById(android.R.id.content);
		root.addView(textView, flp );

		return textView;
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

	void setWaitScreen(boolean set) {
		if(pd == null) {
			pd = new ProgressDialog(getActivity());
			pd.setTitle("");
			pd.setIndeterminate(true);
			pd.setMessage("Загрузка...");
		}

		if(set) pd.show();
		else  pd.dismiss();
	}
}
