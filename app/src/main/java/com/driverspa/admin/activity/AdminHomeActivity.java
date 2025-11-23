package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.APPROVED;
import static com.driverspa.util.Constants.EXTRA_BOOK;
import static com.driverspa.util.Constants.EXTRA_BOOKING_ID;
import static com.driverspa.util.Constants.EXTRA_BOOK_DAY;
import static com.driverspa.util.Constants.EXTRA_BOX_ID;
import static com.driverspa.util.Constants.EXTRA_BOX_NAME;
import static com.driverspa.util.Constants.EXTRA_PUSH_MESSAGE;
import static com.driverspa.util.Constants.EXTRA_PUSH_TYPE;
import static com.driverspa.util.Constants.GRID_TIME;
import static com.driverspa.util.Constants.GRID_TITLE;
import static com.driverspa.util.Constants.RESIGNED;
import static com.driverspa.util.Constants.URL_PREFIX;
import static com.driverspa.util.Constants.VACATION;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Callback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminTabPagerAdapter;
import com.driverspa.admin.fragment.AdminBooksFragment;
import com.driverspa.admin.fragment.AdminClientsFragment;
import com.driverspa.admin.fragment.AdminExpressWashFragment;
import com.driverspa.admin.fragment.AdminFareRequestFragment;
import com.driverspa.admin.fragment.AdminNotRegisteredClientsFragment;
import com.driverspa.admin.fragment.AdminRegisteredClientsFragment;
import com.driverspa.admin.fragment.AdminSettingsFragment;
import com.driverspa.admin.fragment.AdminStatisticsFragment;
import com.driverspa.admin.fragment.AdminTimeTableFragment;
import com.driverspa.model.BookInfo;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.FareRequest;
import com.driverspa.model.FareRequestList;
import com.driverspa.model.NotificationType;
import com.driverspa.model.PushData;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.WasherList;
import com.driverspa.model.WasherPublic;
import com.driverspa.model.api.request.FareRequestBid;
import com.driverspa.navigation.admin.NavigationDrawerCallbacks;
import com.driverspa.navigation.admin.NavigationDrawerFragment;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.HttpClient;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.TransformationCircle;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.FareRequestBidAcceptedEvent;
import com.driverspa.util.otto.FareRequestBidNotAcceptedEvent;
import com.driverspa.util.otto.FareRequestUpdateEvent;
import com.driverspa.util.otto.LocalWasherChangeRequestEvent;
import com.driverspa.util.otto.LocalWasherChangeResponseEvent;
import com.driverspa.util.otto.MakeBidForRequestEvent;
import com.driverspa.util.otto.NewBookingPushResponseEvent;
import com.driverspa.util.otto.NewInformationPushRequestEvent;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.StopPlayRingtoneRequestEvent;
import com.driverspa.util.otto.ws.AdminRequestMakeBidRequestEvent;
import com.driverspa.util.otto.ws.AdminRequestMakeBidResponseEvent;
import com.driverspa.util.otto.ws.AdminWasherInfoRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminLogoutRequestEvent;
import com.driverspa.util.otto.ws.DeleteDeviceRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.CustomViewPager;
import com.driverspa.view.NDSpinner;

/**
 * Client home activity.
 *
 * @author Yerzhan
 */

@SuppressLint("NewApi")
public class AdminHomeActivity extends AdminBaseActivity implements NavigationDrawerCallbacks,MenuItemCompat.OnActionExpandListener,
        AdminBooksFragment.ActivityActions,AdminSettingsFragment.ActivityActions,AdminStatisticsFragment.ActivityActions,
        AdminTimeTableFragment.ActivityActions,AdminClientsFragment.ActivityActions, AdminRegisteredClientsFragment.ActivityActions, AdminNotRegisteredClientsFragment.ActivityActions,
        AdminExpressWashFragment.ActivityActions,
        AdminFareRequestFragment.ActivityActions,
        SearchView.OnQueryTextListener{

    private AdminTabPagerAdapter TabAdapter;
    @BindView(R.id.pager)
    CustomViewPager pagerTab;
    int selectedTab;

    @BindView(R.id.button_express)
    View expressButton;
    @BindView(R.id.button_orders)
    View orderButton;
    @BindView(R.id.button_timetable)
    View timetableButton;
    @BindView(R.id.button_clients)
    View clientButton;
    @BindView(R.id.button_statistics)
    View statisticsButton;

    @BindView(R.id.toolbar_actionbar)
    View toolbarActionBar;
    @BindView(R.id.container)
    View homeContainer;
    @BindView(R.id.newMessageCountLayout)
    View newMessageCountLayout;
    @BindView(R.id.newMessageCount)
    TextView newMessageCount;
    @BindView(R.id.requestsCountLayout)
    View requestsCountLayout;
    @BindView(R.id.requestsCount)
    TextView requestsCount;

    @BindView(R.id.fragment_container)
    View fragmentContainer;
    TextView titleView;
    @BindView(R.id.settings)
    TextView settings;
    @BindView(R.id.warningText)
    TextView warningText;

    @BindView(R.id.notApprovedLayout)
    View notApprovedLayout;
    @BindView(R.id.spinnerLayout)
    View spinnerLayout;

    @BindView (R.id.car)
    TextView carName;
    @BindView (R.id.services)
    TextView services;
    @BindView (R.id.price)
    TextView fare;
    @BindView (R.id.comment)
    TextView comment;
    @BindView (R.id.name)
    TextView name;
    @BindView (R.id.imgClientAvatar)
    ImageView clientAvatar;

    @BindView (R.id.requestAcceptLayout)
    View requestAcceptLayout;
    @BindView (R.id.requestOfferText)
    TextView requestOfferText;
    @BindView (R.id.requestInfoMainLayout)
    View requestInfoMainLayout;
    @BindView (R.id.seekBarLayout)
    View seekBarLayout;
    @BindView(R.id.seekBar)
    SeekBar seekBar;
    @BindView (R.id.requestButtonsLayout)
    View requestButtonsLayout;
    @BindView (R.id.mainAccept)
    Button mainAccept;
    @BindView (R.id.accept1)
    Button accept1;
    @BindView (R.id.accept2)
    Button accept2;
    @BindView (R.id.accept3)
    Button accept3;
    @BindView (R.id.closeAcceptLayout)
    View closeAcceptLayout;
    @BindView (R.id.txtDistance)
    TextView distance;

    HashMap<String,PushData> bookingPushData;
    HashMap<String,PushData> reviewPushData;

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    //Search menu
    Menu menu;
    private int selectedMenu;
    boolean isSearchable = false;
    String innerQuery = "";
    MenuItem searchMenuItem;
    private SearchView mSearchView;
    long lastSearchTime = (Calendar.getInstance()).getTimeInMillis();
    DrawerLayout drawerLayout;
    @BindView(R.id.washer_home_spinner)
    NDSpinner spinner;
    private boolean isSpinnerTouched = false;
    Washer washer;
    User user;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        ButterKnife.bind(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.fragment_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer,drawerLayout, mToolbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);

        TabAdapter = new AdminTabPagerAdapter(getSupportFragmentManager());
        pagerTab.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                setTabButtons(position);
                invalidateOptionsMenu();
            }
        });

        pagerTab.setOffscreenPageLimit(2);
        pagerTab.setAdapter(TabAdapter);
        selectedTab = 0;
        pagerTab.setCurrentItem(selectedTab);
        setTabButtons(selectedTab);

        if( savedInstanceState == null ) {

            String pushType = getIntent().getStringExtra(EXTRA_PUSH_TYPE);
            String pushMessage = getIntent().getStringExtra(EXTRA_PUSH_MESSAGE);

            if(!TextUtils.isEmpty(pushType)) {
                if ((pushType.equals(Constants.INFORMATION) || pushType.equals(Constants.INFORMATION_WITH_UPDATE)) && !TextUtils.isEmpty(pushMessage)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
                    dialog.setTitle(pushMessage);
                    dialog.setPositiveButton("Ок", null);
                    dialog.show();
                } else if (pushType.contains("book")) {
                    String bookId = getIntent().getStringExtra(EXTRA_BOOKING_ID);
                    if (!TextUtils.isEmpty(bookId))
                        openBookInfo(bookId);
                } else if (pushType.contains("review_created")) {
                    onNavigationDrawerItemSelected(6);
                }
            }
        }

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        switch (selectedTab){
            case 0: //main
                getMenuInflater().inflate(R.menu.menu_clean, menu);
                break;
            case 1: //Timetable
                getMenuInflater().inflate(R.menu.menu_clean, menu);
                break;
            case 2: //Clients
                getMenuInflater().inflate(R.menu.menu_clean, menu);
//                getMenuInflater().inflate(R.menu.menu_main, menu);
//                getMenuInflater().inflate(R.menu.menu_options_home, menu);
//                searchMenuItem = menu.findItem(R.id.action_search);
//                mSearchView = (SearchView) searchMenuItem.getActionView();
//                mSearchView.setOnQueryTextListener(this);
//                MenuItemCompat.setOnActionExpandListener(searchMenuItem, this);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.messages:
                Intent intent = new Intent(this,AdminNotificationsActivity.class);
                startActivity(intent);
                overridePendingTransitionWithCommoOpeningTransition(this);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWaitScreen(false);
        setRequestsCountLayout();
        BA.getEventBus().post(new StopPlayRingtoneRequestEvent());

        boolean isLoggedIn = UserPreferences.isAdminLoggedIn(this);
        BA.getEventBus().register(this);
        BA.setNotificationType(NotificationType.HOME);

        if (!isLoggedIn) finish();

        if (washer != null) {
            if(user != null && !user.isOwner() && washer != null){
                boolean restrictUser = false;
                if(!(Functions.getUserRole(washer,user.getId()).equals(Constants.DIRECTOR) || Functions.getUserRole(washer,user.getId()).equals(Constants.OPERATOR))){
                    warningText.setText("Уровень доступа вашего пользователя изменен. Вы не можете пользоваться программой.");
                    restrictUser = true;
                }
                else if(Functions.getUserStatus(washer,user.getId()).equals(VACATION)){
                    warningText.setText("Ваша смена окончена. Вы не можете пользоваться программой.");
                    restrictUser = true;
                }
                else if(Functions.getUserStatus(washer,user.getId()).equals(RESIGNED)){
                    warningText.setText("Вы больше не работаете на этой мойке. Вы не можете пользоваться программой.");
                    restrictUser = true;
                }
                if(restrictUser){
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    settings.setVisibility(View.GONE);
                    spinnerLayout.setVisibility(View.GONE);
                    settings.setPaintFlags(settings.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    notApprovedLayout.setVisibility(View.VISIBLE);
                }
                else{
                    if (washer.getStatus().equals(APPROVED)) {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                }
            }
            else{
                if (washer.getStatus().equals(APPROVED)) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
               }
            }
          }
        }

    @Override
    public void onPause() {
        super.onPause();
        BA.setNotificationType(NotificationType.NOTHING);
        BA.getEventBus().unregister(this);
    }

    private void setTabButtons(int position){
        switch(position){
//            case 0:
//                expressButton.setSelected(true);
//                orderButton.setSelected(false);
//                timetableButton.setSelected(false);
//                clientButton.setSelected(false);
//                statisticsButton.setSelected(false);
//                break;
            case 0:
                expressButton.setSelected(false);
                orderButton.setSelected(true);
                timetableButton.setSelected(false);
                clientButton.setSelected(false);
                statisticsButton.setSelected(false);
                titleView.setText("");
                break;
            case 1:
                expressButton.setSelected(false);
                orderButton.setSelected(false);
                timetableButton.setSelected(true);
                clientButton.setSelected(false);
                statisticsButton.setSelected(false);
//                titleView.setText("");
                break;
            case 2:
                expressButton.setSelected(false);
                orderButton.setSelected(false);
                timetableButton.setSelected(false);
                clientButton.setSelected(true);
                statisticsButton.setSelected(false);
//                titleView.setText("Отзывы");
                break;
            case 3:
                expressButton.setSelected(false);
                orderButton.setSelected(false);
                timetableButton.setSelected(false);
                clientButton.setSelected(false);
                statisticsButton.setSelected(true);
                titleView.setText("Статистика");
                break;
        }
    }

    @OnClick(R.id.button_orders)
    public void onOrderButtonClicked(){
        pagerTab.setCurrentItem(0);
    }

    @OnClick(R.id.button_timetable)
    public void onTimetableButtonClicked(){
        pagerTab.setCurrentItem(1);
    }

    @OnClick(R.id.button_clients)
    public void onClientButtonClicked(){
        pagerTab.setCurrentItem(2);
    }

    @OnClick(R.id.button_statistics)
    public void onStatisticsButtonClicked(){
        pagerTab.setCurrentItem(3);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        selectedMenu = position;
        switch (position){
            case 0: //Home
                if(homeContainer != null)
                    homeContainer.setVisibility(View.VISIBLE);
                if(fragmentContainer != null)
                    fragmentContainer.setVisibility(View.GONE);
                if(titleView != null) {
                    setTabButtons(selectedTab);
                    titleView.setVisibility(View.VISIBLE);
                }
                if(mNavigationDrawerFragment!=null)
                    mNavigationDrawerFragment.closeDrawer();
                break;
            case 1: //clients
                startActivity(new Intent(this,AdminClientsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .putExtra(OPENING_ANIMATION,false)
                );
                break;
            case 2: //staff
                startActivity(new Intent(this,AdminStaffsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .putExtra(OPENING_ANIMATION,false)
                );
                break;

//            case 2: //work shifts
//                startActivity(new Intent(this,AdminWorkShiftsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
//                        .putExtra(OPENING_ANIMATION,false)
//                );

//                break;
            case 3: //financial
                startActivity(new Intent(this,AdminFinancialActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .putExtra(OPENING_ANIMATION,false)
                );
                break;
//            case 4: //campaign
//                if(!Functions.isUserHasAccess(washer,user,this)) return;
//                startActivity(new Intent(this,AdminCampaignActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
//                        .putExtra(OPENING_ANIMATION,false)
//                );
//                break;
            case 4: //statistics

                startActivity(new Intent(this,AdminAnalyticsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .putExtra(OPENING_ANIMATION,false)
                );
                break;
            case 5: //review
                startActivity(new Intent(this,AdminWasherReviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .putExtra(OPENING_ANIMATION,false)
                );

                break;
            case 6: //settings
                if(!Functions.isUserHasAccess(washer,user,this)) return;
                startActivity(new Intent(this,AdminProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .putExtra(OPENING_ANIMATION,false)
                );
                break;
            case 7: //About us
                if(titleView != null) {
                    titleView.setText("О нас");
                }
                startActivity(new Intent(this,AdminAboutUSActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        .putExtra(OPENING_ANIMATION,false)
                );
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    public void openBookInfo(String bookId) {
        Intent intent = new Intent(this,AdminBookingInfoActivity.class);
        intent.putExtra(EXTRA_BOOKING_ID, bookId );
        startActivity(intent);
    }

    @Override
    public void openQueueInfo(BookInfo bookInfo) {
        Intent intent = new Intent(this,AdminEditQueuedBookActivity.class);
        intent.putExtra(EXTRA_BOOK, bookInfo.serialize() );
        startActivity(intent);
    }

    @Override
    public void openQueueInfoWithTransition(BookInfo bookInfo, View sharedView) {
        String transitionName = "title_transition";
        ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(this, sharedView, transitionName);
        Intent intent = new Intent(this,AdminEditQueuedBookActivity.class);
        intent.putExtra(EXTRA_BOOK, bookInfo.serialize() );
        startActivity(intent,transitionActivityOptions.toBundle ());
    }

    @Override
    public void openAddBook(String boxName, String boxId, Long gridTime, String gridTitle, String bookDay) {
        Intent intent = new Intent(this,AdminAddBookActivity.class);
        intent.putExtra(EXTRA_BOX_NAME, boxName );
        intent.putExtra(EXTRA_BOX_ID, boxId );
        intent.putExtra(GRID_TIME, gridTime);
        intent.putExtra(GRID_TITLE, gridTitle );
        intent.putExtra(EXTRA_BOOK_DAY, bookDay );
        intent.putExtra(OPENING_ANIMATION,false);
        startActivity(intent);
    }

    @Subscribe
    public void onBookingPushReceived(NewBookingPushResponseEvent event){
        if(event != null && event.getPush() != null){
            bookingPushData = event.getPush().getData();
            if(bookingPushData!=null && bookingPushData.size() > 0){
                newMessageCountLayout.setVisibility(View.VISIBLE);
                newMessageCount.setText(bookingPushData.size()+"");
            }
            else
                newMessageCountLayout.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onInformationPushReceived(NewInformationPushRequestEvent event){
        if(event != null && event.getData() != null){
            if(!TextUtils.isEmpty(event.getData().getText())){
                AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
                dialog.setTitle(event.getData().getText());
                dialog.setPositiveButton("Ок", null);
                dialog.show();
            }
        }
    }

    @Override
    public void openAddQuee(){
        Intent intent = new Intent(this,AdminAddQueuedBookActivity.class);
        intent.putExtra(OPENING_ANIMATION,false);
        startActivity(intent);
    }

    @Override
    public void editEntry(BookInfo bookInfo) {
        Intent intent = new Intent(this,AdminEditEntryActivity.class);
        intent.putExtra(EXTRA_BOOK, bookInfo.serialize() );
        startActivity(intent);
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
        Intent intent = new Intent(this,AdminAddClientActivity.class);
        startActivity(intent);
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

    @Subscribe
    public void onUserReceived(UserGetSelfResponseEvent event) {
        user = event.getUser();
        if(user != null && !user.isOwner() && washer != null){
            boolean restrictUser = false;
            if(!(Functions.getUserRole(washer,user.getId()).equals(Constants.DIRECTOR) || Functions.getUserRole(washer,user.getId()).equals(Constants.OPERATOR))){
                warningText.setText("Уровень доступа вашего пользователя изменен. Вы не можете пользоваться программой.");
                restrictUser = true;
            }
            else if(Functions.getUserStatus(washer,user.getId()).equals(VACATION)){
                warningText.setText("Ваша смена окончена. Вы не можете пользоваться программой.");
                restrictUser = true;
            }
            else if(Functions.getUserStatus(washer,user.getId()).equals(RESIGNED)){
                warningText.setText("Вы больше не работаете на этой мойке. Вы не можете пользоваться программой.");
                restrictUser = true;
            }
            if(restrictUser){
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                settings.setVisibility(View.GONE);
                spinnerLayout.setVisibility(View.GONE);
                settings.setPaintFlags(settings.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                notApprovedLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {

            if(!TextUtils.isEmpty(washer.getName()) && !washer.getName().equals("NA")){
                createWasherListSpinner(washer.getName(),washer.getId());
            }
            else{
                createWasherListSpinner("",washer.getId());
            }
            if(washer.getStatus().equals(APPROVED)){
                //Current user status check
                if(user != null && !user.isOwner()){
                   boolean restrictUser = false;
                   if(!(Functions.getUserRole(washer,user.getId()).equals(Constants.DIRECTOR) || Functions.getUserRole(washer,user.getId()).equals(Constants.OPERATOR))){
                       warningText.setText("Уровень доступа вашего пользователя изменен. Вы не можете пользоваться программой.");
                       restrictUser = true;
                   }
                   else if(Functions.getUserStatus(washer,user.getId()).equals(VACATION)){
                       warningText.setText("Ваша смена окончена. Вы не можете пользоваться программой.");
                       restrictUser = true;
                   }
                   else if(Functions.getUserStatus(washer,user.getId()).equals(RESIGNED)){
                        warningText.setText("Вы больше не работаете на этой мойке. Вы не можете пользоваться программой.");
                        restrictUser = true;
                   }
                   if(restrictUser){
                       drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                       settings.setVisibility(View.GONE);
                       spinnerLayout.setVisibility(View.GONE);
                       settings.setPaintFlags(settings.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                       notApprovedLayout.setVisibility(View.VISIBLE);
                   }
                    else{
                       drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                       notApprovedLayout.setVisibility(View.GONE);
                   }
                }
                else{
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    notApprovedLayout.setVisibility(View.GONE);
                }
            }
            else{
                settings.setVisibility(View.VISIBLE);
                if(user != null && !user.isOwner()){spinnerLayout.setVisibility(View.GONE);}
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                settings.setPaintFlags(settings.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                notApprovedLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Subscribe
    public void onWasherChangeRequested(LocalWasherChangeRequestEvent event){
        setWaitScreen(true);
    }

    @Subscribe
    public void onWasherChangeRequested(LocalWasherChangeResponseEvent event){
        setWaitScreen(false);
    }

    @OnClick(R.id.settings)
    public void onSettingsButtonClicked(){
//        startActivity(new Intent(this,AdminProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra(OPENING_ANIMATION,false));
        startActivity(new Intent(this,AdminNewWasherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION).putExtra(OPENING_ANIMATION,false));
    }

    @OnClick(R.id.logout_home)
    public void onLogoutButtonClicked(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
        dialog.setTitle("Вы действительно хотите выйти?");
        dialog.setNegativeButton("Отмена", null);
        dialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BA.getEventBus().post(new DeleteDeviceRequestEvent());
                BA.getEventBus().post(new AuthAdminLogoutRequestEvent());
            }
        });
        dialog.show();
    }

    @Subscribe
    public void onUserSelfReceived(UserGetSelfResponseEvent event) {
        spinner.setVisibility(View.GONE);
        User user = event.getUser();
        if (user != null) {
            if(user.isOwner()){
                spinner.setVisibility(View.VISIBLE);
            }
        }
    }

    private void createWasherListSpinner(String washerName, String washerId){
        if(spinner != null) {
            if(UserPreferences.getOwnerWasherListData(BA.getContext()) != null) {
                List<WasherPublic> washerList = WasherList.deserialize(UserPreferences.getOwnerWasherListData(BA.getContext())).getWasherList();
                if(washerList.size() == 1) spinnerLayout.setVisibility(View.GONE);
                else spinnerLayout.setVisibility(View.VISIBLE);

                final String[] valuesArray = new String[washerList.size()];
                final String[] idArray = new String[washerList.size()];
                int i = 0;
                int j = 0;
                valuesArray[0] = washerName!=null? Functions.shortenName(washerName):"";
                idArray[0] = washerId;
                i++;

                for(WasherPublic wp : washerList){
                    if(!wp.getId().equals(washerId)){
                        idArray[i] = wp.getId();
                        valuesArray[i++] = wp.getName()!=null?Functions.shortenName(wp.getName()):"";
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valuesArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        if (!isSpinnerTouched) return;
                             if(pos > 0){
                            changeDefaultWasher(valuesArray[pos],idArray[pos]);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                spinner.setAdapter(adapter);
            }
        }
    }

    private void changeDefaultWasher(String washerName, final String washerId){
        BA.getEventBus().post(new AdminWasherInfoRequestEvent(washerId));
        BA.getEventBus().post(new LocalWasherChangeRequestEvent());
    }

    @Subscribe
    public void onMakeBidRequestEventReceived(MakeBidForRequestEvent event){
        mHandler = new Handler();
        updateRemainingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long timeDiff = currentTime - bidTime;
                seekBar.setProgress((int)timeDiff);
                if(timeDiff/1000 >= 20) {
                    seekBar.setProgress((int) 20 * 1000);
                    requestAcceptLayout.setVisibility(View.GONE);
                    stopTimer();
                }
            }
        };

        if(event != null && event.getRequest()!=null){
            final DecimalFormat formatter = new DecimalFormat("#,###.##");
            final FareRequest item = event.getRequest();
            if(!TextUtils.isEmpty(item.getClientAvatar())){
                HttpClient.getPicasso()
                        .load(RetrofitClient.API_URL_IMAGES+item.getClientAvatar())
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.ic_avatar_round)
                        .transform(new TransformationCircle(AdminHomeActivity.this))
                        .into(clientAvatar, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                clientAvatar.setVisibility(View.VISIBLE);
                                clientAvatar.setImageResource(R.drawable.ic_avatar_round);
                            }
                        });
            }
            else{
                clientAvatar.setVisibility(View.VISIBLE);
                clientAvatar.setImageResource(R.drawable.ic_avatar_round);
            }

            carName.setText("");
            name.setText(item.getClientName()!=null?item.getClientName().trim():"");
            if(!TextUtils.isEmpty(item.getClientKey())){
                carName.append(item.getClientKey());
                carName.append(", ");
            }
            carName.append(BA.getReference().getCarType().get(item.getCarType()));

            String servicesTxt = "";
            if(item.getServices() != null && item.getServices().size() > 0) {
                LinkedHashMap<Integer,Integer> groupedServices = Functions.getGroupedServices(item.getServices());
                for (LinkedHashMap.Entry<Integer, Integer> entry : groupedServices.entrySet()) {
                    Integer serviceId = entry.getKey();
                    Integer count = entry.getValue();
                    servicesTxt = servicesTxt + (BA.getReference().getServices().get(serviceId)+""+(count>1?" x "+count:"")) + "/";
                }
                servicesTxt = servicesTxt.substring(0, servicesTxt.length() - 1);
            }
            if(washer != null && washer.getLonLat() != null && washer.getLonLat().size() > 0 &&  item.getLonLat() != null && item.getLonLat().size() > 0){
                try{
                    Location washerLocation = new Location("A");
                    washerLocation.setLatitude(Double.parseDouble(washer.getLonLat().get(1).toString()));
                    washerLocation.setLongitude(Double.parseDouble(washer.getLonLat().get(0).toString()));

                    Location clientLocation = new Location("B");
                    clientLocation.setLatitude(Double.parseDouble(item.getLonLat().get(1).toString()));
                    clientLocation.setLongitude(Double.parseDouble(item.getLonLat().get(0).toString()));
                    float dist = clientLocation.distanceTo(washerLocation);
                    distance.setText(String.format("%s km", formatter.format(dist/1000).replaceAll(",", " ")));
                }
                catch(Exception e){
                }
            }

            services.setText(servicesTxt);

            fare.setText(formatter.format(item.getFare())+" ₸");
            if(!TextUtils.isEmpty(item.getComment()))
                comment.setText(item.getComment());
            else{
                comment.setVisibility(View.GONE);
            }

            requestAcceptLayout.setVisibility(View.VISIBLE);
            requestOfferText.setVisibility(View.GONE);
            seekBarLayout.setVisibility(View.GONE);
            requestButtonsLayout.setVisibility(View.VISIBLE);


            mainAccept.setText("Принять за "+formatter.format(item.getFare())+" ₸");
            mainAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestOfferText.setText("Предлагаем клиенту\n" +
                            "за услугу "+
                            formatter.format(item.getFare())+" ₸\nпожалуйста подождите");
                    startUpdateTimer();
                    BA.getEventBus().post(new AdminRequestMakeBidRequestEvent(new FareRequestBid(URL_PREFIX + item.getId(),URL_PREFIX + washer.getId(),item.getFare())));
                }
            });
            accept1.setText(formatter.format(item.getFare()+100)+" ₸");
            accept1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpdateTimer();
                    requestOfferText.setText("Предлагаем клиенту\n" +
                            "за услугу "+
                            formatter.format(item.getFare()+100)+" ₸\nпожалуйста подождите");
                    BA.getEventBus().post(new AdminRequestMakeBidRequestEvent(new FareRequestBid(URL_PREFIX + item.getId(),URL_PREFIX + washer.getId(),item.getFare()+100)));
                }
            });
            accept2.setText(formatter.format(item.getFare()+200)+" ₸");
            accept2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpdateTimer();
                    requestOfferText.setText("Предлагаем клиенту\n" +
                            "за услугу "+
                            formatter.format(item.getFare()+200)+" ₸\nпожалуйста подождите");
                    BA.getEventBus().post(new AdminRequestMakeBidRequestEvent(new FareRequestBid(URL_PREFIX + item.getId(),URL_PREFIX + washer.getId(),item.getFare()+200)));
                }
            });
            accept3.setText(formatter.format(item.getFare()+300)+" ₸");
            accept3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpdateTimer();
                    requestOfferText.setText("Предлагаем клиенту\n" +
                            "за услугу "+
                            formatter.format(item.getFare()+300)+" ₸\nпожалуйста подождите");
                    BA.getEventBus().post(new AdminRequestMakeBidRequestEvent(new FareRequestBid(URL_PREFIX + item.getId(),URL_PREFIX + washer.getId(),item.getFare()+300)));
                }
            });

            closeAcceptLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestAcceptLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Subscribe
    public void onAdminRequestMakeBidRequestEventReceived(AdminRequestMakeBidResponseEvent event){
        setWaitScreen(false);
        if (event.getResponse() != null) {
            if (BaseAssist.isSuccess(event.getResponse())) {
            } else {
                requestAcceptLayout.setVisibility(View.GONE);

                ToastUtil.display(this, event.getResponse().getMessage());
            }
        } else {
            ToastUtil.display(this, "Ошибка при отправки предложении");
        }
    }

    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
                long currentTime = System.currentTimeMillis();
                long timeDiff = currentTime - bidTime;
                seekBar.setProgress((int)timeDiff);
                if(timeDiff/1000 >= 20) {
                    seekBar.setProgress((int) 20 * 1000);
                    requestAcceptLayout.setVisibility(View.GONE);
                    stopTimer();
                }
        }
    };

    private void startUpdateTimer() {
        mHandler = new Handler();
        seekBar.setProgress(0);
        seekBar.setPadding(0, 0, 0, 0);
        seekBar.setMax((int)20*1000-1000);
        bidTime = System.currentTimeMillis();
        requestAcceptLayout.setVisibility(View.VISIBLE);
        requestOfferText.setVisibility(View.VISIBLE);
        seekBarLayout.setVisibility(View.VISIBLE);
        requestButtonsLayout.setVisibility(View.GONE);

        tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mHandler != null)
                   mHandler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);
    }

    public void stopTimer() {
        if (mHandler != null && updateRemainingTimeRunnable != null)
            mHandler.removeCallbacks(updateRemainingTimeRunnable);
        if(tmr != null)
           tmr.cancel();
        updateRemainingTimeRunnable = null;
        mHandler = null;
    }

    @Subscribe
    public void onBidAcceptedEventReceived(FareRequestBidAcceptedEvent event){
        stopTimer();
        ToastUtil.display(AdminHomeActivity.this, "Предложение принято клиентом");
        requestAcceptLayout.setVisibility(View.GONE);
        openBookInfo(event.getBookId());
    }

    @Subscribe
    public void onBidNotAcceptedEventReceived(FareRequestBidNotAcceptedEvent event){
        stopTimer();
        ToastUtil.display(AdminHomeActivity.this, "Предложение не принято клиентом");
        requestAcceptLayout.setVisibility(View.GONE);
    }
    Timer tmr;
    long bidTime = System.currentTimeMillis();

    @Subscribe
    public void onFareRequestsUpdateEventReceived(FareRequestUpdateEvent event) {
        setRequestsCountLayout();
    }

    private void setRequestsCountLayout(){
        FareRequestList fareRequestList;
        if (!TextUtils.isEmpty(UserPreferences.getActiveFareRequest(BA.getContext()))) {
            fareRequestList = FareRequestList.deserialize(UserPreferences.getActiveFareRequest(BA.getContext()));
        } else {
            fareRequestList = new FareRequestList();
            fareRequestList.setFareRequests(new ArrayList<FareRequest>());
        }
        List<FareRequest> fareRequests = fareRequestList.getFareRequests();
        if (fareRequests == null) {
            fareRequests = new ArrayList<>();
        }
        if(fareRequests.size() >0){
            requestsCountLayout.setVisibility(View.VISIBLE);
            requestsCount.setText(fareRequests.size()+"");
        }
        else{
            requestsCountLayout.setVisibility(View.GONE);
            requestsCount.setText(fareRequests.size()+"");
        }
    }
}
