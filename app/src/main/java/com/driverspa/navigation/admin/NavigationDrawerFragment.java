package com.driverspa.navigation.admin;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.otto.Subscribe;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminNewWasherActivity;
import com.driverspa.admin.activity.AdminProfileActivity;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.WasherList;
import com.driverspa.model.WasherPublic;
import com.driverspa.util.Converters;
import com.driverspa.util.Functions;
import com.driverspa.util.HttpClient;
import com.driverspa.util.L;
import com.driverspa.util.RetrofitClient;
import com.driverspa.util.TransformationCircle;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.LocalWasherChangeRequestEvent;
import com.driverspa.util.otto.ws.AdminWasherInfoRequestEvent;
import com.driverspa.util.otto.ws.AuthAdminLogoutRequestEvent;
import com.driverspa.util.otto.ws.DeleteDeviceRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;
import com.driverspa.view.ScrimInsetsFrameLayout;
import com.driverspa.view.SimpleDividerItemDecoration;

/**
 * Created by Yerzhant on 26/10/2015.
 */
public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallbacks {
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREFERENCES_FILE = "my_app_settings"; //TODO: change this to your file
    private NavigationDrawerCallbacks mCallbacks;
    private View mFragmentContainerView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;
    private int mCurrentSelectedPosition;
    LinkedHashMap<String,String> userRoles = BA.getReference().getRoles();
    LinkedHashMap<String,String> userStatus = BA.getReference().getStaffStatuses();

    @BindView(R.id.profile_avatar)
    ImageView profileAvatar;
    @BindView(R.id.profile_avatar_invisible)
    ImageView avatarBackground;
    @BindView(R.id.fade_for_avatar)
    View fadeAvatar;
    @BindView(R.id.profile_name)
    TextView profileName;
    @BindView(R.id.version)
    TextView version;

    @BindView(R.id.profile_phone)
    TextView profilePhone;
    @BindView(R.id.user_role)
    TextView userRole;

    @BindView(R.id.drawerList)
    RecyclerView mDrawerList;
    NavigationDrawerAdapter adapter;
    List<NavigationItem> navigationItems;
    User user;

    @BindView(R.id.washer_spinner)
    NDSpinner spinner;
    @BindView(R.id.spinnerLayout)
    View spinnerLayout;
    Washer washer;
    private boolean isSpinnerTouched = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        ButterKnife.bind(this, view);

        String versionName = "";
        try {
            versionName = "v"+getActivity().getApplicationContext().getPackageManager().getPackageInfo(getActivity().getApplicationContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version.setText(versionName);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);

        navigationItems = getMenu();
        adapter = new NavigationDrawerAdapter(navigationItems);
        adapter.setNavigationDrawerCallbacks(this);
        mDrawerList.setAdapter(adapter);
        mDrawerList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        // Drawer items 1..n each launch a separate activity. onCreateView only
        // runs when the host is (re)created — e.g. coming back from one of those
        // screens, especially with "Don't keep activities" enabled. Restoring a
        // saved position here would re-fire selectItem and relaunch that screen,
        // making back appear to "reopen the same activity". A child screen must
        // only ever open from an explicit tap, so always start on Home (0).
        mCurrentSelectedPosition = 0;
        mFromSavedInstanceState = false;
        selectItem(mCurrentSelectedPosition);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });

        return view;
      }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserLearnedDrawer = Boolean.valueOf(readSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "false"));
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        L.d("calling onAttach ");
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
            L.d("here is creating call back ");
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    public void setActionBarDrawerToggle(ActionBarDrawerToggle actionBarDrawerToggle) {
        mActionBarDrawerToggle = actionBarDrawerToggle;
    }

    private void createWasherListSpinner(String washerName, String washerId){
        if(spinner != null) {
            if(UserPreferences.getOwnerWasherListData(BA.getContext()) != null) {

                List<WasherPublic> washerList = WasherList.deserialize(UserPreferences.getOwnerWasherListData(BA.getContext())).getWasherList();
                final String[] valuesArray = new String[washerList.size()+1];
                final String[] idArray = new String[washerList.size()+1];
                int i = 0;
                int j = 0;
                valuesArray[0] = washerName!=null?Functions.shortenName(washerName):"";
                idArray[0] = washerId;
                i++;

                for(WasherPublic wp : washerList){
                   if(!wp.getId().equals(washerId)){
                       idArray[i] = wp.getId();
                       valuesArray[i++] = wp.getName()!=null?Functions.shortenName(wp.getName()):"";
                   }
                }

                idArray[valuesArray.length-1] = "";
                valuesArray[valuesArray.length-1] = getString(R.string.add_washer);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.navigation_drawer_spinner_item, valuesArray);
                adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                       if (!isSpinnerTouched) return;
                       if(pos == valuesArray.length-1){
                           createNewWasher();
                           return;
                       }
                       else if(pos > 0){
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

    private void createNewWasher(){
        startActivity(new Intent(getActivity(),AdminNewWasherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        if(mFragmentContainerView.getParent() instanceof ScrimInsetsFrameLayout){
            mFragmentContainerView = (View) mFragmentContainerView.getParent();
        }
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setStatusBarBackgroundColor(
                getResources().getColor(R.color.myPrimaryDarkColor));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    saveSharedSetting(getActivity(), PREF_USER_LEARNED_DRAWER, "true");
                }

                getActivity().invalidateOptionsMenu();
            }
        };

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        L.d("calling detach ");
        mCallbacks = null;
    }

    public List<NavigationItem> getMenu() {
        List<NavigationItem> items = new ArrayList<NavigationItem>();
        String city = Functions.getCityDescription(UserPreferences.getCity(getActivity()));
        String orders = getString(R.string.menu_orders);
        items.add(new NavigationItem(TextUtils.isEmpty(city)?orders:orders+"("+city+")", getResources().getDrawable(R.drawable.ic_business_white_24dp)));
        // The shop (docs/shop.md), for all working staff. AdminHomeActivity numbers the items.
        items.add(new NavigationItem(getString(R.string.menu_shop), getResources().getDrawable(R.drawable.ic_shop_white_24dp)));
        items.add(new NavigationItem(getString(R.string.menu_clients), getResources().getDrawable(R.drawable.ic_clients)));
        items.add(new NavigationItem(getString(R.string.menu_employees), getResources().getDrawable(R.drawable.ic_clients_normal)));
//        items.add(new NavigationItem("Смены", getResources().getDrawable(R.drawable.ic_work_shifts)));
        items.add(new NavigationItem(getString(R.string.menu_finance), getResources().getDrawable(R.drawable.ic_finance)));
//        items.add(new NavigationItem("Акция", getResources().getDrawable(R.drawable.ic_tab_campaign_normal)));
        items.add(new NavigationItem(getString(R.string.menu_analytics), getResources().getDrawable(R.drawable.ic_assessment_white_24dp)));
        items.add(new NavigationItem(getString(R.string.menu_reviews), getResources().getDrawable(R.drawable.ic_tab_comment_normal)));
        items.add(new NavigationItem(getString(R.string.menu_settings), getResources().getDrawable(R.drawable.ic_settings_white_24dp)));
        items.add(new NavigationItem(getString(R.string.menu_about), getResources().getDrawable(R.drawable.ic_share_white_24dp)));
        return items;
    }

    /**
     * Changes the icon of the drawer to back
     */
    public void showBackButton() {
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Changes the icon of the drawer to menu
     */
    public void showDrawerButton() {
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        mActionBarDrawerToggle.syncState();
    }

    void selectItem(int position) {
        L.d("calling selectitem ");
        mCurrentSelectedPosition = position;
        if (mDrawerLayout != null) {
//            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
        L.d("calling selectitem2 ");
        ((NavigationDrawerAdapter) mDrawerList.getAdapter()).selectPosition(position);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        L.d("onNavigationDrawerItemSelected ");
        selectItem(position);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        mDrawerLayout = drawerLayout;
    }

    public static void saveSharedSetting(Context ctx, String settingName, String settingValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(settingName, settingValue);
        editor.apply();
    }

    public static String readSharedSetting(Context ctx, String settingName, String defaultValue) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(settingName, defaultValue);
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
    public void onUserReceived(UserGetSelfResponseEvent event){
        user = event.getUser();
        if(user != null) {
            if (!TextUtils.isEmpty(user.getAvatar())) {
                profileAvatar.setVisibility(View.VISIBLE);
                avatarBackground.setVisibility(View.VISIBLE);
                fadeAvatar.setVisibility(View.VISIBLE);

                HttpClient.getPicasso()
                        .load(RetrofitClient.API_URL_IMAGES + user.getAvatar())
                        .fit()
                        .centerCrop()
                        .transform(new TransformationCircle(getActivity()))
                        .placeholder(R.drawable.ic_no_image)
                        .into(profileAvatar);

                HttpClient.getPicasso()
                        .load(RetrofitClient.API_URL_IMAGES + user.getAvatar())
                        .fit()
                        .centerCrop()
                        .into(avatarBackground);
            } else {
                avatarBackground.setVisibility(View.GONE);
                fadeAvatar.setVisibility(View.GONE);
                profileAvatar.setVisibility(View.VISIBLE);
                profileAvatar.setImageResource(R.drawable.ic_no_image);
            }
            profileName.setPaintFlags(profileName.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
            profileName.setText(TextUtils.isEmpty(user.getFirstName()) ? "" : user.getFirstName());
            profilePhone.setText(Functions.maskPhoneNumber(user.getPhone(), Converters.PHONE_PATTERN));

            if(user.isOwner()){
                spinnerLayout.setVisibility(View.VISIBLE);
                profileName.setVisibility(View.GONE);
                userRole.setVisibility(View.GONE);
            }
            else{
                spinnerLayout.setVisibility(View.GONE);
                profileName.setVisibility(View.VISIBLE);
                userRole.setVisibility(View.VISIBLE);
                if(washer != null){
                    userRole.setText(userRoles.get(Functions.getUserRole(washer,user.getId())));
//                    + " - "+userStatus.get(Functions.getUserStatus(washer,user.getId()))
                }
            }

            if(adapter != null){
                navigationItems = getMenu();
                adapter = new NavigationDrawerAdapter(navigationItems);
                adapter.setNavigationDrawerCallbacks(this);
                mDrawerList.setAdapter(adapter);
                mDrawerList.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {
//            if(user != null && !user.isOwner()){
//                userRole.setText(userRoles.get(Functions.getUserRole(washer,user.getId())));
//            }
            if(!TextUtils.isEmpty(washer.getName()) && !washer.getName().equals("NA")){
//                profileName.setText(washer.getName());
                createWasherListSpinner(washer.getName(),washer.getId());
            }
            else{
//                profileName.setText("");
                createWasherListSpinner(BA.str(R.string.no_name),washer.getId());
            }
        }
    }

    @OnClick(R.id.profile_phone)
    public void onProfilePhoneClicked(){
        if(!Functions.isUserHasAccess(washer,user)) return;
        startActivity(new Intent(getActivity(),AdminProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    @OnClick(R.id.profile_avatar)
    public void onProfileAvatarClicked(){
        if(!Functions.isUserHasAccess(washer,user)) return;
        startActivity(new Intent(getActivity(),AdminProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    @OnClick(R.id.profile_name)
    public void onProfileNameClicked(){
        if(!Functions.isUserHasAccess(washer,user)) return;
        startActivity(new Intent(getActivity(),AdminProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        getActivity().overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
    }

    @OnClick(R.id.logout)
    public void onLogoutButtonClicked(){
        AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(getActivity());
        dialog.setTitle(BA.str(R.string.confirm_logout));
        dialog.setNegativeButton(BA.str(R.string.cancel_word), null);
        dialog.setPositiveButton(BA.str(R.string.ok_word), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BA.getEventBus().post(new DeleteDeviceRequestEvent());
                BA.getEventBus().post(new AuthAdminLogoutRequestEvent());
            }
        });
        dialog.show();
    }
}
