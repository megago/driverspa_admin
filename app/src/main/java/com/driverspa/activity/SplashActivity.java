package com.driverspa.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.driverspa.assist.UserSelfAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Device;
import com.driverspa.model.User;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AboutUSRequestEvent;
import com.driverspa.util.otto.ws.InitRequestEvent;
import com.driverspa.util.otto.ws.InitResponseEvent;
import com.driverspa.util.otto.ws.OwnerWasherListGetRequestEvent;
import com.driverspa.util.otto.ws.PushRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfRequestEvent;


/**
 * @author Yerzhan
 *
 */

//fa7fe554
public class SplashActivity extends BaseActivity implements AnimationListener{

	@BindView(R.id.noConnectionLayout)
	View noConnectionLayout;
	@BindView (R.id.logo)
	View logo;
	@BindView (R.id.circle1)
	ImageView circle1;
	@BindView (R.id.circle2)
	ImageView circle2;
	@BindView (R.id.circle3)
	ImageView circle3;
	@BindView (R.id.versionUpdateWarning)
	View versionUpdateWarning;

	Animation animCircle1;
	Animation animCircle2;
	Animation animCircle3;

	boolean referencesReceived = false;
	boolean washerListReceived = false;
	boolean isLoggedIn = false;
	String appVersion = "";

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		setContentView(R.layout.activity_splash);
		ButterKnife.bind(this);

		animCircle1 = AnimationUtils.loadAnimation(this,R.anim.circle1_anim);
		animCircle2 = AnimationUtils.loadAnimation(this,R.anim.circle2_anim);
		animCircle3 = AnimationUtils.loadAnimation(this,R.anim.circle3_anim);

		circle1.startAnimation(animCircle1);
		circle2.startAnimation(animCircle2);
		circle3.startAnimation(animCircle3);
		washerListReceived = (UserPreferences.getOwnerWasherListData(BA.getContext()) != null);
		isLoggedIn = UserPreferences.isAdminLoggedIn(this);
		User user = UserSelfAssist.getUserFromDb(UserPreferences.getUserId(BA.getContext()));
		if(isLoggedIn) {
			if(user != null) {
				String token = UserPreferences.getPushToken(this);
				String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
				String phoneModel = android.os.Build.MODEL;
				String androidVersion = android.os.Build.VERSION.RELEASE;
				appVersion = "";
				try {
					appVersion = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
				BA.getEventBus().post(new PushRequestEvent(deviceId, new Device(phoneModel, "android", androidVersion, appVersion, token, "gcm", user.getDefaultCarwashId(), true, !user.isOwner())));
		  }
		  else if(user == null) {
			  //If database was recreated then ask for user data again
			  String userId = UserPreferences.getUserId(BA.getContext());
			  if (!TextUtils.isEmpty(userId)) {
				  BA.getEventBus().post(new UserGetSelfRequestEvent(userId));
			  }
		  }
			BA.getEventBus().post(new OwnerWasherListGetRequestEvent());
		}

	}

	@Override
	public void onAnimationEnd(Animation animation) {
	}

	@Override
	protected void onDestroy() {
		BA.getEventBus().register(this);
		super.onDestroy();
	}

	@Override
	public void onAnimationStart(Animation animation) {}
	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	protected void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
		BA.getEventBus().post(new InitRequestEvent(UserPreferences.getUserLocale(this)));
		BA.getEventBus().post(new AboutUSRequestEvent());
	}
	@Override
	protected void onPause() {
		super.onPause();
		try {
			BA.getEventBus().unregister(this);
		}
		catch(Exception e){}
	}

	@Subscribe
	public void onReferenceReceived(InitResponseEvent event){
		if(event.getReference() != null){
			versionUpdateWarning.setVisibility(View.GONE);
			referencesReceived = true;
			BA.setReference(event.getReference());
			if(!TextUtils.isEmpty(appVersion) && BA.getReference()!=null && BA.getReference().getAppVersionCodes().get("android_admin") != null && !BA.getReference().getAppVersionCodes().get("android_admin").equals(appVersion)){
			  versionUpdateWarning.setVisibility(View.VISIBLE);
			}
			else goToMainActivity();
		}
		else{
			noConnectionLayout.setVisibility(View.VISIBLE);
			logo.setVisibility(View.GONE);
			ToastUtil.display(this, "Ошибка при инициализации, проверьте соединение");
		}
	}

	@OnClick (R.id.repeatConnect)
	public void onRepeatConnect(){
		logo.setVisibility(View.VISIBLE);
		noConnectionLayout.setVisibility(View.GONE);
		BA.getEventBus().post(new InitRequestEvent(UserPreferences.getUserLocale(this)));
		BA.getEventBus().post(new AboutUSRequestEvent());
	}

	@OnClick (R.id.update)
	public void onUpdateButtonClicked(){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=com.driverspaAdmin"));
		startActivity(intent);
	}

	private void goToMainActivity(){
		finish();
		boolean splashShown = UserPreferences.isInfoPageShown(this);
		if(splashShown)
			startActivity(new Intent(this,MainActivity.class).putExtra(OPENING_ANIMATION, false).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
		else
			startActivity(new Intent(this,SplashInfoActivity.class).putExtra(OPENING_ANIMATION, false).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
	}
}

	
