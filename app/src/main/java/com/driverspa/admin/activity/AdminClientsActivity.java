package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import android.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.fragment.AdminNotRegisteredClientsFragment;
import com.driverspa.admin.fragment.AdminRegisteredClientsFragment;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;


public class AdminClientsActivity extends AdminBaseActivity implements AdminRegisteredClientsFragment.ActivityActions,AdminNotRegisteredClientsFragment.ActivityActions,
        SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {

    Washer washer;

    @BindView(R.id.pager)
    ViewPager viewPager;
    PagerAdapter adapter;

    int selectedTab = 0;

    @BindView(R.id.registered)
    View registered;
    @BindView(R.id.notRegistered)
    View notRegistered;
    NDSpinner spinner;

    //Search menu
    Menu menu;
    boolean isSearchable = false;
    String innerQuery = "";
    MenuItem searchMenuItem;
    private SearchView mSearchView;
    long lastSearchTime = (Calendar.getInstance()).getTimeInMillis();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        setContentView(R.layout.activity_admin_clients);
        ButterKnife.bind(this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Клиенты");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        spinner = (NDSpinner) mToolbar.findViewById(R.id.spinner);
//        spinner.setVisibility(View.VISIBLE);
//        createActionBarSpinner();
//        titleView.setVisibility(View.GONE);

//        mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);
//        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        registered.setSelected(true);
        adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                setTabButtons(position);
            }
        });

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(selectedTab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
//        getMenuInflater().inflate(R.menu.menu_options_home, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.menu_options_home, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, this);

        return true;
    }

    private void createActionBarSpinner(){
        if(spinner != null) {
            String[] valuesArray = new String[]{"Зарегистрированные клиенты", "Незарегистрированные клиенты"};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
            adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        switch (pos) {
                            case 0:
                                viewPager.setCurrentItem(0);
                                break;
                            case 1:
                                viewPager.setCurrentItem(1);
                                break;
                        }
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spinner.setAdapter(adapter);
            if(viewPager != null)
             spinner.setSelection(viewPager.getCurrentItem());
        }
    }

    @OnClick(R.id.registered)
    public void onTodayButtonClick(){
         viewPager.setCurrentItem(0);
    }

    @OnClick(R.id.notRegistered)
    public void onTomorrowButtonClick(){
        viewPager.setCurrentItem(1);
    }

    private void setTabButtons(int pos){
        switch (pos){
            case 0:
                registered.setSelected(true);
                notRegistered.setSelected(false);
                break;
            case 1:
                registered.setSelected(false);
                notRegistered.setSelected(true);
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
            case R.id.messages:
                Intent intent = new Intent(this,AdminNotificationsActivity.class);
                startActivity(intent);
                overridePendingTransitionWithCommoOpeningTransition(this);
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

    @Override
    public void openClientInfo(ClientInfo client) {
        Intent intent = new Intent(this,AdminClientActivity.class);
        intent.putExtra(AdminClientActivity.CLIENT_INFO,client.serialize());
        startActivity(intent);
        overridePendingTransitionWithCommoOpeningTransition(this);
    }

    @Override
    public void addToTheBox(ClientInfo client) {
        Intent intent = new Intent(this,AdminAddClientQueueActivity.class);
        intent.putExtra(Constants.CLIENT_INFO,client.serialize());
        startActivity(intent);
    }

    @Override
    public void addNewClient() {
        onAddClicked();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        long actualSearchTime = (Calendar.getInstance()).getTimeInMillis();
        // Only one search every second to avoid key-down & key-up
        if (actualSearchTime > lastSearchTime + 1000)
        {
            if(!TextUtils.isEmpty(query)){
                innerQuery = query;
                searchQuery(query);
            }
            lastSearchTime=actualSearchTime;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if(TextUtils.isEmpty(query) && isSearchable){
        }
        return false;
    }

    private void searchQuery(String query){
        BA.getEventBus().post(new SearchEvent(query));
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        if (!TextUtils.isEmpty(innerQuery)) {
            searchQuery("");
        }
        isSearchable = false;
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        isSearchable = true;
        if (!TextUtils.isEmpty(innerQuery))
            searchQuery("");
        innerQuery = "";
        return true;
    }

    @OnClick(R.id.btnAdd)
    public void onAddClicked(){
        if(!Functions.isUserHasAccess(washer,user,this)) return;
        Intent intent = new Intent(this,AdminAddClientActivity.class);
        startActivityForResult(intent, ActivityForResult.ACTIVITY_ADDITIONAL_INFO);
        overridePendingTransitionWithCommoOpeningTransition(this);
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
                    return  new AdminRegisteredClientsFragment();
                case 1:
                    //Fragment available washers
                    return new AdminNotRegisteredClientsFragment();
            }
            return null;
        }
        @Override
        public int getCount() {
            return 1; //No of Tabs
        }
    }

    @Subscribe
    public void onUserReceived(UserGetSelfResponseEvent event) {
        user = event.getUser();
    }
    User user;

}
