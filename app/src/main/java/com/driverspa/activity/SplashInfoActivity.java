package com.driverspa.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.ImageView;

import com.splunk.mint.Mint;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.fragment.SplashInfoFragment;
import com.driverspa.util.UserPreferences;

/**
 * @author Yerzhan
 *
 */

public class SplashInfoActivity extends BaseActivity implements SplashInfoFragment.ActivityActions{

	@BindView(R.id.pager)
	ViewPager viewPager;
	@BindView(R.id.dot1)
	ImageView dot1;
	@BindView(R.id.dot2)
	ImageView dot2;
	@BindView(R.id.dot3)
	ImageView dot3;
	@BindView(R.id.btnNext)
	Button nextButton;

	SplashInfoPagerAdapter viewPagerAdapter;
	int selectedTab;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
		setContentView(R.layout.activity_splash_info);
		BA.getEventBus().register(this);
		ButterKnife.bind(this);

		viewPagerAdapter = new SplashInfoPagerAdapter(getSupportFragmentManager());
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if(position > 2){
					start();
				}
				selectedTab = position;
				setAnketaDots(selectedTab);
			}
		});

		viewPager.setOffscreenPageLimit(3);
		viewPager.setAdapter(viewPagerAdapter);
		viewPager.setCurrentItem(selectedTab);
	}
		

	@Override
	protected void onDestroy() {
		BA.getEventBus().register(this);
		super.onDestroy();
	}

	private void setAnketaDots(int tab){
		if(dot1!=null)
			switch (tab){
				case 0:
					dot1.setImageResource(R.drawable.ic_selected_dot);
					dot2.setImageResource(R.drawable.ic_unselected_dot);
					dot3.setImageResource(R.drawable.ic_unselected_dot);
					nextButton.setText("Далее");
					break;
				case 1:
					dot1.setImageResource(R.drawable.ic_unselected_dot);
					dot2.setImageResource(R.drawable.ic_selected_dot);
					dot3.setImageResource(R.drawable.ic_unselected_dot);
					nextButton.setText("Далее");
					break;
				case 2:
					dot1.setImageResource(R.drawable.ic_unselected_dot);
					dot2.setImageResource(R.drawable.ic_unselected_dot);
					dot3.setImageResource(R.drawable.ic_selected_dot);
					nextButton.setText("Начать");
					break;
			}
	}

	@Override
	public void nextPage() {
		viewPager.setCurrentItem(selectedTab+1);
	}

	@Override
	public void start() {
		finish();
		overridePendingTransition(0,0);
		UserPreferences.onInfoPageShown(this);
		startActivity(new Intent(this,MainActivity.class).putExtra(OPENING_ANIMATION, false).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
	}

	@OnClick(R.id.btnNext)
	public void onButtonNextClicked(){
		if(selectedTab < 2)
			nextPage();
		else
			start();
	}

}
