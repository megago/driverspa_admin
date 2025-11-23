package com.driverspa.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.Reference;
import com.driverspa.adapter.CityChooseAdapter;
import com.driverspa.util.Functions;
import com.driverspa.util.GPSTracker;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.CityChangeEvent;
import com.driverspa.util.otto.LocationRequestEndEvent;
import com.driverspa.util.otto.TurnOnGPSRequestEvent;
import android.widget.AdapterView.OnItemClickListener;

import androidx.appcompat.widget.Toolbar;

import com.splunk.mint.Mint;

public class CityChooseActivity extends BaseActivity {

    @BindView(R.id.list_view)
    ListView listView;
    CityChooseAdapter adapter;
    TextView titleView;
    Context context;
    static ArrayList<Reference.City> allCities = BA.getReference().getCities();

    View headerView;
    View myLocationCheckbox;
    TextView myLocationText;
    View myLocationLayout;

    boolean isCityFoundByGPS = UserPreferences.isCityFoundByGPS(BA.getContext());
    String localCity = UserPreferences.getCity(BA.getContext());
    String selectedCityCode = localCity;
    private ProgressDialog pd = null;
    GPSTracker gps;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "b054ddc0");
        setContentView(R.layout.activity_city_choose);
        context = this;
        ButterKnife.bind(this);
        gps = new GPSTracker(this);
        headerView = getLayoutInflater().inflate(R.layout.header_city_choose,null, false);
        myLocationCheckbox = headerView.findViewById(R.id.myLocationCheckbox);
        myLocationText = (TextView) headerView.findViewById(R.id.myLocation);
        myLocationLayout = headerView.findViewById(R.id.currentLocation);

        myLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWaitScreen(true);
                myLocationCheckbox.setVisibility(View.GONE);
                if(gps.canGetLocation()){
                   setWaitScreen(false);
                   onLocationResponseReceived(gps.getLocation());
                }
                else{
                    onGPSTurnONRequested(new TurnOnGPSRequestEvent());
                }

            }
        });

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Выберите город");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = new CityChooseAdapter(this);
        adapter.set(allCities);
        listView.addHeaderView(headerView);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        myLocationCheckbox.setVisibility(View.GONE);

        if(!TextUtils.isEmpty(localCity)){
          if(isCityFoundByGPS){
              myLocationCheckbox.setVisibility(View.VISIBLE);
              myLocationText.setText(Functions.getCityDescription(localCity));
          }
            else{
              myLocationCheckbox.setVisibility(View.GONE);
              adapter.setSelectedCityCode(localCity);
              adapter.notifyDataSetChanged();
          }
        }
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               if(position == 0){
               }
                else {
                   UserPreferences.putCityFoundByGPS(BA.getContext(),false);
                   myLocationCheckbox.setVisibility(View.GONE);
                   selectedCityCode = allCities.get(position - 1).getCode();
                   adapter.setSelectedCityCode(selectedCityCode);
                   UserPreferences.putCity(CityChooseActivity.this,selectedCityCode);
                   saveData();
               }
                adapter.notifyDataSetChanged();
            }
        });

        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(selectedCityCode)){
                    ToastUtil.displayAtTop(context, "Выберите город");
                    return;
                }
                saveData();
            }
        });
    }

    public void onGPSTurnONRequested(TurnOnGPSRequestEvent event){
        setWaitScreen(false);
        showSettingsAlert();
    }

    public void onLocationResponseReceived(Location location){
      setWaitScreen(false);
      if(location != null){
         String nearestCity = findNearestCity(location);
         if(!TextUtils.isEmpty(nearestCity)){
             myLocationCheckbox.setVisibility(View.VISIBLE);
             myLocationText.setText(Functions.getCityDescription(nearestCity));
             selectedCityCode = nearestCity;
             adapter.setSelectedCityCode(null);
             adapter.notifyDataSetChanged();
             UserPreferences.putCityFoundByGPS(BA.getContext(),true);
         }
          else{
             ToastUtil.display(this,"Не могу определить местоположение");
             myLocationText.setText("Мое местоположение");
             myLocationCheckbox.setVisibility(View.GONE);
             UserPreferences.putCityFoundByGPS(BA.getContext(),false);
         }
      }
        else{
          ToastUtil.display(this,"Не могу определить местоположение");
          myLocationText.setText("Мое местоположение");
          myLocationCheckbox.setVisibility(View.GONE);
          UserPreferences.putCityFoundByGPS(BA.getContext(),false);
        }
        BA.getEventBus().post(new LocationRequestEndEvent());
    }

    public static String findNearestCity(final Location currentLoc){
        String nearestCity = "";
        final String cityCode = "CODE";
        final String distance = "DISTANCE";

        List<Reference.City> sortedCities = new ArrayList<Reference.City>(allCities);
        Collections.sort(sortedCities, new Comparator<Reference.City>() {
            @Override
            public int compare(Reference.City lhs, Reference.City rhs) {
                if(lhs.getDistance(currentLoc) > rhs.getDistance(currentLoc)){
                    return 1;
                }
                else
                    return -1;
            }
        });

        if(sortedCities.get(0).getDistance(currentLoc)/1000 < 100){
            nearestCity = sortedCities.get(0).getCode();
        }

//        for(Reference.City city: sortedCities){
//            L.d("city "+city.getCode()+" "+city.getDistance(currentLoc)/1000);
//        }


        return nearestCity;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(TextUtils.isEmpty(selectedCityCode)){
                    ToastUtil.display(this, "Выберите город");
                    return false;
                }
                saveData();
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        BA.getEventBus().register(this);
        gps = new GPSTracker(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().post(new LocationRequestEndEvent());
        gps.stopUsingGPS();
        BA.getEventBus().unregister(this);
    }

    private void saveData(){
        BA.getEventBus().post(new CityChangeEvent());
        UserPreferences.putCity(CityChooseActivity.this,selectedCityCode);
        finish();
        overridePendingTransitionWithCommonCloseTransition();
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context,AlertDialog.THEME_HOLO_LIGHT);
        // Setting Dialog Title
        alertDialog.setTitle("Настройки GPS");
        // Setting Dialog Message
        alertDialog.setMessage("GPS отключен. Хотите включить?");
        // On pressing the Settings button.
        alertDialog.setPositiveButton("Настройки", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,int which) {
//            	mContext.startActivity(new Intent(mContext, ClientSettingsActivity.class).putExtra(ClientBaseActivity.OPENING_ANIMATION, false));
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                myLocationText.setText("Мое местоположение");
                adapter.setSelectedCityCode(selectedCityCode);
                adapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void setWaitScreen(boolean set) {
        if(pd == null) {
            pd = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            pd.setTitle("");
            pd.setIndeterminate(true);
            pd.setCancelable(true);
            pd.setMessage("Определение местоположение");
        }
        if(set) pd.show();
        else  pd.dismiss();
    }

    @Override
    public void onBackPressed() {
        if(TextUtils.isEmpty(selectedCityCode)){
            ToastUtil.display(context, "Выберите город");
            return;
        }
        super.onBackPressed();
    }
}
