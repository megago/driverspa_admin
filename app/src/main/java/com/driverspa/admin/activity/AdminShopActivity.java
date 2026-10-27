package com.driverspa.admin.activity;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.fragment.ShopItemsFragment;
import com.driverspa.admin.fragment.ShopReportFragment;
import com.driverspa.admin.fragment.ShopSalesFragment;
import com.driverspa.admin.fragment.ShopSellFragment;
import com.driverspa.admin.shop.Shop;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Магазин (docs/shop.md): Продажа, Товары, Продажи and Отчёт for the owner and directors;
 * Продажа and Продажи for operators. The tabs are built once the user and the car wash are known.
 */
public class AdminShopActivity extends AdminBaseActivity {

	private static final String STATE_TAB = "STATE_TAB";

	private static final int TAB_SELL = 0;
	private static final int TAB_ITEMS = 1;
	private static final int TAB_SALES = 2;
	private static final int TAB_REPORT = 3;

	@BindView(R.id.pager)
	ViewPager viewPager;
	@BindView(R.id.tabBar)
	LinearLayout tabBar;

	private final List<Integer> tabKinds = new ArrayList<>();
	private final List<TextView> tabViews = new ArrayList<>();
	private Boolean manager;
	private boolean userReceived;
	private int selectedTab;
	private User user;
	private Washer washer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_shop);
		Toolbar toolbar = findViewById(R.id.toolbar_actionbar);
		TextView titleView = toolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.menu_shop));
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
		ButterKnife.bind(this);
		if (savedInstanceState != null) selectedTab = savedInstanceState.getInt(STATE_TAB, 0);

		viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				selectedTab = position;
				selectTabButton(position);
			}
		});
		installFinishOnBackCallback();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_TAB, selectedTab);
	}

	@Override
	public void onResume() {
		super.onResume();
		BA.getEventBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		setupTabs();
	}

	@Subscribe
	public void onUserReceived(UserGetSelfResponseEvent event) {
		user = event.getUser();
		userReceived = true;
		setupTabs();
	}

	/**
	 * Builds the tabs once, when both the car wash and the user have arrived (Otto delivers the
	 * two produced events in any order). Building them once keeps the pages restored after a
	 * process death attached to the same adapter, with their saved state (the cart).
	 */
	private void setupTabs() {
		if (manager != null || washer == null || !userReceived) return;
		boolean isManager = Shop.isManager(washer, user);
		manager = isManager;

		tabKinds.clear();
		tabKinds.add(TAB_SELL);
		if (isManager) tabKinds.add(TAB_ITEMS);
		tabKinds.add(TAB_SALES);
		if (isManager) tabKinds.add(TAB_REPORT);

		tabBar.removeAllViews();
		tabViews.clear();
		for (int i = 0; i < tabKinds.size(); i++) {
			final int position = i;
			TextView tab = new TextView(this);
			tab.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
			tab.setGravity(Gravity.CENTER);
			int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
			tab.setPadding(padding / 2, padding, padding / 2, padding);
			tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_normal));
			tab.setTextColor(ContextCompat.getColorStateList(this, R.drawable.button_bar_state_color));
			tab.setTypeface(tab.getTypeface(), android.graphics.Typeface.BOLD);
			tab.setMaxLines(1);
			tab.setText(BA.str(tabTitle(tabKinds.get(i))));
			tab.setOnClickListener(v -> viewPager.setCurrentItem(position));
			tabBar.addView(tab);
			tabViews.add(tab);
		}

		viewPager.setOffscreenPageLimit(tabKinds.size());
		viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
		selectedTab = Math.max(0, Math.min(selectedTab, tabKinds.size() - 1));
		viewPager.setCurrentItem(selectedTab, false);
		selectTabButton(selectedTab);
	}

	private static int tabTitle(int kind) {
		switch (kind) {
			case TAB_ITEMS:
				return R.string.shop_tab_items;
			case TAB_SALES:
				return R.string.shop_tab_sales;
			case TAB_REPORT:
				return R.string.shop_tab_report;
			default:
				return R.string.shop_tab_sell;
		}
	}

	private void selectTabButton(int position) {
		for (int i = 0; i < tabViews.size(); i++) tabViews.get(i).setSelected(i == position);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransitionWithCommonCloseTransition();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setWaitScreen(boolean set) {
		if (isFinishing() || isDestroyed()) return;
		super.setWaitScreen(set);
	}

	private class PagerAdapter extends FragmentStatePagerAdapter {

		PagerAdapter(FragmentManager fm) {
			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		}

		@Override
		public Fragment getItem(int position) {
			switch (tabKinds.get(position)) {
				case TAB_ITEMS:
					return new ShopItemsFragment();
				case TAB_SALES:
					return new ShopSalesFragment();
				case TAB_REPORT:
					return new ShopReportFragment();
				default:
					return new ShopSellFragment();
			}
		}

		@Override
		public int getCount() {
			return tabKinds.size();
		}
	}
}
