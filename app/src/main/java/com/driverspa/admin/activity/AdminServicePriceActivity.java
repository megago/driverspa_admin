package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.ServicePriceExpandableListAdapter;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServicePriceExpandable;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class AdminServicePriceActivity extends AdminBaseActivity {

    @BindView(R.id.expandable_list_view)
    ExpandableListView listView;
	Washer washer;
    ServicePriceExpandableListAdapter adapter;
    List<ServicePriceExpandable> servicePriceExpandableList;

    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
        setContentView(R.layout.activity_service_price);
        ButterKnife.bind(this);
        adapter = new ServicePriceExpandableListAdapter(this);
        servicePriceExpandableList = new ArrayList<ServicePriceExpandable>();
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText(BA.str(R.string.services_and_prices));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                parent.smoothScrollToPosition(groupPosition);
               if(servicePriceExpandableList.get(groupPosition).getPricesWithCarType() != null && servicePriceExpandableList.get(groupPosition).getPricesWithCarType().size() > 0) {
                   if (parent.isGroupExpanded(groupPosition)) {
                       parent.collapseGroup(groupPosition);
                   } else {
                       parent.expandGroup(groupPosition);
                   }
               }
               else{
                   Intent intent = new Intent(getApplicationContext(),AdminServicePriceDetailActivity.class);
                   intent.putExtra(Constants.SERVICE,servicePriceExpandableList.get(groupPosition).getServiceId());
                   startActivity(intent);
                   overridePendingTransitionWithCommoOpeningTransition(AdminServicePriceActivity.this);
               }

                return true;
            }
        });

    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event){
        washer = event.getWasher();
        if(servicePriceExpandableList.size() == 0 && washer != null) {

            servicePriceExpandableList = new ArrayList<ServicePriceExpandable>();
            //Set all services
            final HashMap<Integer,String> allServices = BA.getReference().getServices();
            final HashMap<Integer,String> allCarTypes = BA.getReference().getCarType();
            for (Map.Entry<Integer,String> entry : allServices.entrySet()) {
                HashMap<String,String> serviceMap = new HashMap<String,String>();
                Integer serviceId = entry.getKey();
                String serviceName = entry.getValue();

                //expandable model
                ServicePriceExpandable servicePriceExpandable = new ServicePriceExpandable();
                servicePriceExpandable.setServiceId(serviceId);
                servicePriceExpandable.setServiceName(serviceName);

                //Check menu object
                LinkedHashMap<Integer, ArrayList<Washer.Prices>> carTypeMenuAll = new LinkedHashMap<Integer, ArrayList<Washer.Prices>>();
                carTypeMenuAll = washer.getMenu();
                List<ReversePrices> reversePrices = new ArrayList<ReversePrices>();

                //Iterating menu to get reversed menu object(fill expandable model)
                if(carTypeMenuAll.size() > 0) { // If menu has values
                    for (Map.Entry<Integer, ArrayList<Washer.Prices>> menu : carTypeMenuAll.entrySet()) {
                        Integer carType = menu.getKey();
                        ArrayList<Washer.Prices> prices = menu.getValue();

                        //check service id, then fill price and time for cartype
                        if(prices != null && prices.size() > 0) {
                            for (Washer.Prices price : prices) {
                                if (serviceId == price.getType()) {
                                    ReversePrices reversePrice = new ReversePrices();
                                    reversePrice.setCarType(carType);
                                    reversePrice.setCarTypeName(allCarTypes.get(carType));
                                    reversePrice.setPrice(price.getPrice());
                                    reversePrice.setTime(price.getTime());
                                    reversePrices.add(reversePrice);
                                }
                            }
                        }
                    }
                }

                servicePriceExpandable.setPricesWithCarType(reversePrices);
                servicePriceExpandableList.add(servicePriceExpandable);
            }

//            try {
//                //Sort services
//                Collections.sort(servicePriceExpandableList, new Comparator<ServicePriceExpandable>() {
//                    @Override
//                    public int compare(ServicePriceExpandable lhs, ServicePriceExpandable rhs) {
//                        if (lhs.getServiceId() < rhs.getServiceId() ||
//                                (
//                                        lhs.getPricesWithCarType() != null && lhs.getPricesWithCarType().size() > 0
//                                                &&
//                                                !(rhs.getPricesWithCarType() != null && rhs.getPricesWithCarType().size() > 0)
//                                )
//                                )
//                            return -1;
//                        else
//                            return 1;
//                    }
//                });
//            }
//            catch(Exception e){}

            //create adapter
            adapter = new ServicePriceExpandableListAdapter(this,servicePriceExpandableList);
            washer.setServicePriceList(servicePriceExpandableList);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

////            for(int i=0; i < adapter.getGroupCount(); i++)
//            if(adapter!=null && adapter.getGroupCount() > 0)
//                  listView.expandGroup(0);

        }
        else{
            adapter.notifyDataSetChanged();
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
        closeKeyboard();
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
            reverseReversedServices(servicePriceExpandableList);
            BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
        }
    }

    private void reverseReversedServices(List<ServicePriceExpandable> servicePriceExpandableList){
        if(servicePriceExpandableList != null && servicePriceExpandableList.size() > 0 ) {
            LinkedHashMap<Integer, Integer> uniqueCarTypes = new LinkedHashMap<Integer, Integer>();
            final ArrayList<Integer> requestCarTypes = new ArrayList<Integer>();
            final ArrayList<Integer> requestServices = new ArrayList<Integer>();

            //Full scan -> find car types
            for (ServicePriceExpandable s : servicePriceExpandableList) {

                //Generating unique se
                if(s.getPricesWithCarType() != null && s.getPricesWithCarType().size() > 0)
                    requestServices.add(s.getServiceId());

                for (ReversePrices p : s.getPricesWithCarType()) {
                    uniqueCarTypes.put(p.getCarType(), p.getCarType());
                }
            }

            LinkedHashMap<Integer, ArrayList<Washer.Prices>> carTypeMenuAll = new LinkedHashMap<Integer, ArrayList<Washer.Prices>>();
            //Create Menu for cartypes and services
            for (Map.Entry<Integer, Integer> entry : uniqueCarTypes.entrySet()) {
                Integer carType = entry.getKey();
                requestCarTypes.add(carType);
                ArrayList<Washer.Prices> prices = new ArrayList<Washer.Prices>();
                for (ServicePriceExpandable s : servicePriceExpandableList) {

                    for (ReversePrices p : s.getPricesWithCarType()) {
                        if (p.getCarType() == carType) {

                            Washer.Prices price = new Washer.Prices();
                            price.setPrice(p.getPrice());
                            price.setTime(p.getTime());
                            price.setType(s.getServiceId());
                            prices.add(price);
                        }
                    }
                }
                carTypeMenuAll.put(carType, prices);
            }
            washer.setCarTypes(new ArrayList<Integer>(requestCarTypes));
            washer.setServices(new ArrayList<Integer>(requestServices));

            washer.setMenu(carTypeMenuAll);
        }
    }
}
