package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.model.Washer.Prices;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class PriceDefinitionActivity extends AdminBaseActivity {
	    	
	@BindView(R.id.list_view)
	ListView listView;
	ArrayList<Integer> adapterData;	
	HashMap<Integer, ArrayList<Prices>> carTypeMenu;
	Washer washer;
	PriceDefinitionAdapter adapter;
		
    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

    	setContentView(R.layout.activity_price_definition);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Цены");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    	ButterKnife.bind(this);
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event){
        washer = event.getWasher();
        if(washer != null) {
            carTypeMenu = washer.getMenu();
            adapter = new PriceDefinitionAdapter(this);
            adapterData = new ArrayList<Integer>();
            for(Integer carType : washer.getCarTypes()){
                adapterData.add(carType);
            }
            adapter.set(adapterData);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Intent intent = new Intent(PriceDefinitionActivity.this, PriceDefinitionDetailActivity.class);
                    Integer selectedCarType = adapter.getList().get(arg2);
                    intent.putExtra(Constants.CAR_TYPE, selectedCarType);
                    startActivityForResult(intent, ActivityForResult.ACTIVITY_PRICE_DETAIL);
                    overridePendingTransitionWithCommoOpeningTransition(PriceDefinitionActivity.this);
                }
            });
        }
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
            	saveData();
                break;          
        }
        return true;
    }	

    @Override
    public void onBackPressed() {
    	saveData();
    }
    
    private void saveData(){
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
        if(washer != null) {
            BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
        }
    }
}
