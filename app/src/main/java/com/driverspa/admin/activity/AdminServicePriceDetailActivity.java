package com.driverspa.admin.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServicePriceExpandable;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.L;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class AdminServicePriceDetailActivity extends AdminBaseActivity {
	@BindView(R.id.txtTitle)
	TextView textTitle;
	@BindView(R.id.list_view)
	ListView listView;
    @BindView(R.id.price_layout)
    LinearLayout priceLayout;
    DecimalFormat formatter = new DecimalFormat("###.##");
	Integer selectedService;
    Washer washer;
	PriceDefinitionDetailAdapter adapter;
    TextView titleView;
    Context context;
    List<ServicePriceExpandable> servicePriceExpandableList;
    ServicePriceExpandable servicePriceExpandable;
    public static final int PRICE = 1;
    public static final int TIME = 2;

    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_price_definition_detail);
        ButterKnife.bind(this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
        context = this;
        selectedService = getIntent().getIntExtra(Constants.SERVICE, 0);
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
        if(washer != null) {
            BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
        }
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

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null && washer.getServicePriceList() != null) {
            servicePriceExpandableList = washer.getServicePriceList();
            for(ServicePriceExpandable s : servicePriceExpandableList) {
                if(s.getServiceId() == selectedService) {
                    servicePriceExpandable = s;
                    L.d("selected s "+servicePriceExpandable.getServiceId());
                    for(ReversePrices p: servicePriceExpandable.getPricesWithCarType()){
                        L.d("iter "+p.getCarType());
                    }
                    break;
                }
            }
            listView.setVisibility(View.GONE);
            preparePriceView();
            textTitle.setVisibility(View.GONE);
//            textTitle.setText("Цены на "+servicePriceExpandable.getServiceName());
            titleView.setText(servicePriceExpandable.getServiceName());
        }
        else{
            finish();
            overridePendingTransitionWithCommonCloseTransition();
        }
    }

    private void preparePriceView(){
        priceLayout.removeAllViews();

        //Will be listed all cartypes
        final HashMap<Integer,String> allCarTypes = BA.getReference().getCarType();

        //Iterate all cartypes
        for (Map.Entry<Integer,String> entry : allCarTypes.entrySet()) {
            HashMap<String, String> serviceMap = new HashMap<String, String>();
            final Integer carType = entry.getKey();
            String carTypeName = entry.getValue();

            View view = LayoutInflater.from(context).inflate(R.layout.list_service_price_detail_item, null, false);
            TextView desc = (TextView) view.findViewById(R.id.desc);
            final View checkbox = view.findViewById(R.id.checkbox);
            final View descLayout = view.findViewById(R.id.descLayout);
            final View priceLinearLayout = view.findViewById(R.id.priceLayout);

            priceLinearLayout.setVisibility(View.GONE);
            desc.setText(carTypeName);

            final EditText priceItem = (EditText) view.findViewById(R.id.priceItem);
            final EditText timeItem = (EditText) view.findViewById(R.id.timeItem);

            priceItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        if(!TextUtils.isEmpty(priceItem.getText().toString()) && priceItem.getText().toString().equals("0")) {
                            priceItem.setText("");
                        }
                    }
                    else{
                        if(TextUtils.isEmpty(priceItem.getText().toString())){
                            priceItem.setText("0");
                        }
                    }
                }
            });

            timeItem.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        if(!TextUtils.isEmpty(timeItem.getText().toString()) && timeItem.getText().toString().equals("0")) {
                            timeItem.setText("");
                        }
                    }
                    else{
                        if(TextUtils.isEmpty(timeItem.getText().toString())){
                            timeItem.setText("0");
                        }
                    }
                }
            });

            boolean foundCarType = false;
            //Find each cartype reverseprice
            for(ReversePrices price : servicePriceExpandable.getPricesWithCarType()) {
                if(price.getCarType() == carType) {
                    foundCarType = true;
                    priceLinearLayout.setVisibility(View.VISIBLE);
                    priceItem.setText((price.getPrice()!=null?formatter.format(price.getPrice()).replaceAll(",", " "):formatter.format(0).replaceAll(",", " "))+"");
                    timeItem.setText(price.getTime()!=null?""+price.getTime():"0");

                    priceItem.setTag(price);
                    timeItem.setTag(price);

                    //we need to update adapter once we finish with editing
                    priceItem.addTextChangedListener(new MyTextWatcher(priceItem,price,PRICE));
                    timeItem.addTextChangedListener(new MyTextWatcher(timeItem,price,TIME));
                }
            }

            //Checked or not checked
            if(foundCarType){checkbox.setVisibility(View.VISIBLE);}
            else {checkbox.setVisibility(View.GONE);}


            HashMap<Integer,Object> tagMap = new HashMap<Integer,Object>();
            tagMap.put(0,carType);
            tagMap.put(1,checkbox);
            descLayout.setTag(tagMap);

            descLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<Integer,Object> tagMap = (HashMap<Integer,Object>)v.getTag();
                    Integer carType = (Integer)tagMap.get(0);
                    View checkbox = (View)tagMap.get(1);
                    if(checkbox.getVisibility() == View.VISIBLE){
                       //Remove the price from the list
                       checkbox.setVisibility(View.GONE);
                        for (Iterator<ReversePrices> iter = servicePriceExpandable.getPricesWithCarType().listIterator(); iter.hasNext();) {
                            ReversePrices price = iter.next();
                            if(price.getCarType() == carType) iter.remove();
                        }
                        priceLinearLayout.setVisibility(View.GONE);
                    }
                    else{
                        //Show price layout and add item to the list
                        checkbox.setVisibility(View.VISIBLE);
                        priceLinearLayout.setVisibility(View.VISIBLE);
                        ReversePrices price = new ReversePrices();
                        price.setCarType(carType);
                        price.setCarTypeName(allCarTypes.get(carType));
                        price.setPrice(0D);
                        price.setTime(0);
                        priceLinearLayout.setVisibility(View.VISIBLE);
                        priceItem.setText("0");
                        timeItem.setText("0");
                        priceItem.setTag(price);
                        timeItem.setTag(price);

                        //we need to update adapter once we finish with editing
                        priceItem.addTextChangedListener(new MyTextWatcher(priceItem,price,PRICE));
                        timeItem.addTextChangedListener(new MyTextWatcher(timeItem,price,TIME));

                        servicePriceExpandable.getPricesWithCarType().add(price);
                    }
                }
            });

            priceLayout.addView(view);
        }
    }

    private class MyTextWatcher implements TextWatcher{
        int whichText;
        EditText editText;
        ReversePrices p;

        public MyTextWatcher(EditText editText, ReversePrices p, int whichText){
           this.editText = editText;
           this.p = p;
           this.whichText = whichText;
        }
        @Override
        public void afterTextChanged(Editable s) {
           String enteredText = s.toString();
           if(whichText == PRICE){
               try {
                   p.setPrice(Double.parseDouble(enteredText));
               }
               catch (Exception e){
//                   ToastUtil.display(context, "Не правильный формат цены");
               }
           }
            else if(whichText == TIME){
               try {
                  p.setTime(Integer.parseInt(enteredText));
               }
               catch (Exception e){
//                   ToastUtil.display(context, "Не правильный формат времени");
               }
           }
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


}
