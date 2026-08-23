package com.driverspa.admin.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.fragment.AdminGroupServicePriceFragment;
import com.driverspa.admin.fragment.AdminOwnServicePriceFragment;
import com.driverspa.admin.fragment.AdminServicePriceFragment;
import com.driverspa.model.Washer;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminPricesActivity extends AdminBaseActivity implements AdminServicePriceFragment.ActivityActions, AdminOwnServicePriceFragment.ActivityActions,AdminGroupServicePriceFragment.ActivityActions{

    Washer washer;
    @BindView(R.id.pager)
    ViewPager viewPager;
    PagerAdapter adapter;
    int selectedTab = 0;
    @BindView(R.id.services)
    View services;
    @BindView(R.id.servicesOwn)
    View servicesOwn;
    @BindView(R.id.servicesGroup)
    View servicesGroup;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        setContentView(R.layout.activity_admin_service_prices);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText(BA.str(R.string.services_and_prices));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ButterKnife.bind(this);
        services.setSelected(true);
        adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                setTabButtons(position);
            }
        });

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(selectedTab);

        installFinishOnBackCallback();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @OnClick(R.id.services)
    public void onServicesButtonClick(){
         viewPager.setCurrentItem(0);
    }

    @OnClick(R.id.servicesOwn)
    public void onOwnServicesButtonClick(){
        viewPager.setCurrentItem(1);
    }

    @OnClick(R.id.servicesGroup)
    public void onGroupServicesButtonClick(){
        viewPager.setCurrentItem(2);
    }

    private void setTabButtons(int pos){
        switch (pos){
            case 0:
                services.setSelected(true);
                servicesOwn.setSelected(false);
                servicesGroup.setSelected(false);
                break;
            case 1:
                services.setSelected(false);
                servicesOwn.setSelected(true);
                servicesGroup.setSelected(false);
                break;
            case 2:
                services.setSelected(false);
                servicesOwn.setSelected(false);
                servicesGroup.setSelected(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransitionWithCommonCloseTransition();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransitionWithCommonCloseTransition();
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
        if (washer != null) {
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    //Books Fragment
                    return  new AdminServicePriceFragment();
                case 1:
                    //Books Fragment
                    return  new AdminOwnServicePriceFragment();
                case 2:
                    //Fragment available washers
                    return new AdminGroupServicePriceFragment();
            }
            return null;
        }
        @Override
        public int getCount() {
            return 3; //No of Tabs
        }
    }
}
