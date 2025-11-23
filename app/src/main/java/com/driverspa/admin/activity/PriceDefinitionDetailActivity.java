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

import com.driverspa.adapter.PriceDefinitionDetailAdapter;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class PriceDefinitionDetailActivity extends AdminBaseActivity {
	@BindView(R.id.txtTitle)
	TextView textTitle;
	@BindView(R.id.list_view)
	ListView listView;
    @BindView(R.id.price_layout)
    LinearLayout priceLayout;
    DecimalFormat formatter = new DecimalFormat("###.##");
	Integer selectedCarType;
    Washer washer;
	PriceDefinitionDetailAdapter adapter;
    TextView titleView;
    Context context;

    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

    	setContentView(R.layout.activity_price_definition_detail);
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
    	ButterKnife.bind(this);
        selectedCarType = getIntent().getIntExtra(Constants.CAR_TYPE, 0);
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
        if (washer != null) {
            listView.setVisibility(View.GONE);
            preparePriceView();
            HashMap<Integer,String> allCarTypes = BA.getReference().getCarType();
            String carTypeDesc = allCarTypes.get(selectedCarType);
            textTitle.setText("Цены на "+carTypeDesc.toLowerCase());
        }
    }

    private void preparePriceView(){
//        adapter = new PriceDefinitionDetailAdapter(this);
        List<Washer.Prices> pricesList = washer.getMenu().get(selectedCarType);
        priceLayout.removeAllViews();
        if(pricesList !=null ){
            for (Washer.Prices price : pricesList){
                View view = LayoutInflater.from(context).inflate(R.layout.list_price_definition_detail_item, null, false);

                TextView txtService = (TextView) view.findViewById(R.id.txtService);
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

                priceItem.setText((price.getPrice()!=null?formatter.format(price.getPrice()).replaceAll(",", " "):formatter.format(0).replaceAll(",", " "))+"");
                timeItem.setText(price.getTime()!=null?""+price.getTime():"0");

                HashMap<Integer,String> allServices = BA.getReference().getServices();
                txtService.setText(allServices.get(price.getType()));
                priceItem.setTag(price);
                timeItem.setTag(price);

                //we need to update adapter once we finish with editing
                priceItem.addTextChangedListener(new MyTextWatcher(priceItem,price,PRICE));
                timeItem.addTextChangedListener(new MyTextWatcher(timeItem,price,TIME));

                priceLayout.addView(view);
            }
        }
    }


    private class MyTextWatcher implements TextWatcher{
        int whichText;
        EditText editText;
        Washer.Prices p;

        public MyTextWatcher(EditText editText, Washer.Prices p, int whichText){
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

    public static final int PRICE = 1;
    public static final int TIME = 2;
}
