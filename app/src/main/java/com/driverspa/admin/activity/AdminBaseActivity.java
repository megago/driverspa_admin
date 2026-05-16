package com.driverspa.admin.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.driverspa.R;
import com.driverspa.listener.OnActionbarTitleChange;

@SuppressLint("NewApi")
public class AdminBaseActivity extends AppCompatActivity implements OnActionbarTitleChange {

    private ProgressDialog pd;
	private final String TAG = "YERZHAN";
	public static final String OPENING_ANIMATION = "OPENING_ANIMATION";

	public void startActivity(Intent intent) {
		super.startActivity(intent);
		boolean openingAnimation = intent.getBooleanExtra(OPENING_ANIMATION, true);
		if( openingAnimation )
			overridePendingTransitionWithCommoOpeningTransition(this);
	}
			
	public void startActivityForResult(Intent intent, int requestCode ) {
		super.startActivityForResult(intent, requestCode);
		boolean openingAnimation = intent.getBooleanExtra(OPENING_ANIMATION, true);
		if( openingAnimation )
			overridePendingTransitionWithCommoOpeningTransition(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if( ! (this instanceof AdminHomeActivity) ) {
				if (getSupportFragmentManager().getBackStackEntryCount() == 1 || getSupportFragmentManager().getBackStackEntryCount() == 0){
					overridePendingTransitionWithCommonCloseTransition();
				}
				else {
					getSupportFragmentManager().popBackStackImmediate();
				}
		}
	}

	/**
	 * Play activity close animation
	 */
	protected void overridePendingTransitionWithCommonCloseTransition() {
		overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
	}

	/**
	 * Play activity open animation
	 */
	public static void overridePendingTransitionWithCommoOpeningTransition(Activity activity) {
		activity.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
	}

	@Override
	public void setTitle(String title) {
		getActionBar().setTitle(title);
	}
	
	protected void startAnimation(View view, Animation anim){
		view.setAnimation(anim);
		anim.start();		
	}

	  /**
		 * Shows wait dialog
		 */
	  void setWaitScreen(boolean set) {
		  if(pd == null) {
			  pd = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
			  pd.setTitle("");
			  pd.setIndeterminate(true);
			  pd.setMessage("Загрузка...");
		  }

		  if(set) pd.show();
		  else  pd.dismiss();
	  }

	public void addFragmentContent(int resource,Fragment fragment){
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.add(resource, fragment);
		fragmentTransaction.commit();
	}

	public void replaceFragment(Fragment fragment){
		String backStateName = fragment.getClass().getName();
		FragmentManager manager = getSupportFragmentManager();
		boolean fragmentPopped = manager.popBackStackImmediate (backStateName, 0);
		if (!fragmentPopped){ //fragment not in back stack, create it.
			FragmentTransaction ft = manager.beginTransaction();
			ft.replace(R.id.fragment_container, fragment);
			ft.addToBackStack(backStateName);
			ft.commit();
		}
	}

	protected void addEmptyTextView(ListView listView,String text) {
		View view = listView.getEmptyView();
		if(view != null)
			view.setVisibility(View.GONE);
		listView.setEmptyView(getEmtpyTextView(text));
	}

	private TextView getEmtpyTextView(String text){
		TextView textView;
		FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		flp.gravity = Gravity.CENTER;
		int horizontalMargin = (int) getResources().getDimension(R.dimen.horizontal_margin_double);
		flp.leftMargin = horizontalMargin;
		flp.rightMargin = horizontalMargin;

		textView = new TextView(this);
		textView.setText(text);
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(getResources().getColor(R.color.White));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));

		ViewGroup root = (ViewGroup)this.findViewById(android.R.id.content);
		root.addView(textView, flp );

		return textView;
	}

	public void closeKeyboard() {
		if(getCurrentFocus()!=null) {
			try {
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
			}
			catch(Exception e){
			}
		}
	}
}
