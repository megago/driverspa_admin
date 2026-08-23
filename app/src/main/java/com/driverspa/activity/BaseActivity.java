package com.driverspa.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.listener.OnActionbarTitleChange;

@SuppressLint("NewApi")
public class BaseActivity extends AppCompatActivity implements OnActionbarTitleChange {

	private final String TAG = "YERZHAN";
	public static final String OPENING_ANIMATION = "OPENING_ANIMATION";
	private ProgressDialog pd = null;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		applySystemBarInsetsAsPadding(this);
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		applySystemBarInsetsAsPadding(this);
	}

	@Override
	public void setContentView(View view, android.view.ViewGroup.LayoutParams params) {
		super.setContentView(view, params);
		applySystemBarInsetsAsPadding(this);
	}

	/**
	 * Android 15+ forces apps targeting SDK 35 into edge-to-edge, and Android 16
	 * ignores the windowOptOutEdgeToEdgeEnforcement theme flag and
	 * setDecorFitsSystemWindows(true). This legacy UI isn't inset-aware, so the
	 * top Toolbar ends up drawn behind the status bar. Pad the content frame by
	 * the system-bar insets (the pre-edge-to-edge layout) and consume them so
	 * inner inset-aware views (DrawerLayout / ScrimInsetsFrameLayout) don't add
	 * the same padding again. Must run after setContentView, when the content
	 * view actually exists.
	 */
	static void applySystemBarInsetsAsPadding(AppCompatActivity activity) {
		final View content = activity.findViewById(android.R.id.content);
		if (content == null) return;
		ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
			Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
			return WindowInsetsCompat.CONSUMED;
		});
		ViewCompat.requestApplyInsets(content);
	}

	public void startActivity(Intent intent) {
		hideSoftKeyboard();
		super.startActivity(intent);
		boolean openingAnimation = intent.getBooleanExtra(OPENING_ANIMATION, true);
//		LogUtil.d(TAG, "open activity with animation : " + String.valueOf(openingAnimation) + " " + getClass().getSimpleName());
		if( openingAnimation )
			overridePendingTransitionWithCommoOpeningTransition(this);
	}

	public void startActivityForResult(Intent intent, int requestCode ) {
		hideSoftKeyboard();
		super.startActivityForResult(intent, requestCode);
		boolean openingAnimation = intent.getBooleanExtra(OPENING_ANIMATION, true);
//		LogUtil.d(TAG, "open activity with animation : " + String.valueOf(openingAnimation) + " " + getClass().getSimpleName());
		if( openingAnimation )
			overridePendingTransitionWithCommoOpeningTransition(this);
	}

	public void hideSoftKeyboard() {
	   try {
		   if (getCurrentFocus() != null) {
			   InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			   inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		   }
	   }
	   catch (Exception e){}
	}

	@Override
	protected void onResume() {
		super.onResume();
		dismissLingeringKeyboard();
	}

	/**
	 * Hides a soft keyboard left over from a previous screen so the first back
	 * press closes the activity instead of just dismissing the keyboard. Active
	 * text fields are left alone, so real typing is unaffected.
	 */
	protected void dismissLingeringKeyboard() {
		if (getCurrentFocus() instanceof EditText) return;
		try {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			View decor = getWindow() != null ? getWindow().getDecorView() : null;
			if (imm != null && decor != null && decor.getWindowToken() != null) {
				imm.hideSoftInputFromWindow(decor.getWindowToken(), 0);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		if( ! (this instanceof MainActivity) )
			overridePendingTransitionWithCommonCloseTransition();
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
	public static void overridePendingTransitionWithCommoOpeningTransition(BaseActivity baseActivity) {
		baseActivity.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
	}

	@Override
	public void setTitle(String title) {
		setTitle(title);
	}
	
	protected void startAnimation(View view, Animation anim){
		view.setAnimation(anim);
		anim.start();		
	}

	public void setWaitScreen(boolean set) {
		if(pd == null) {
			pd = new ProgressDialog(this);
			pd.setTitle("");
			pd.setIndeterminate(true);
			pd.setMessage(BA.str(R.string.loading_dots));
		}

		try {
			if (set) pd.show();
			else pd.dismiss();
		}
		catch(Exception e){}
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
}
