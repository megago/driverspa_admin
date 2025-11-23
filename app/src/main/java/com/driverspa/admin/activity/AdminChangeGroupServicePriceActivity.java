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
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminGroupMenuChangeRequestEvent;
import com.driverspa.util.otto.ws.AdminGroupMenuChangeResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminChangeGroupServicePriceActivity extends AdminBaseActivity {

    Washer washer;

    MultipleSelectDialog servicesDialog;
    @BindView(R.id.txtServices)
    TextView textServices;

    @BindView(R.id.name)
    EditText groupName;
    public static final int PRICE = 1;
    public static final int TIME = 2;
    DecimalFormat formatter = new DecimalFormat("###.##");
    List<ReversePrices> priceList;
    Washer.GroupMenu groupMenu;
    List<Integer> serviceList;
    @BindView(R.id.priceLayout)
    LinearLayout priceLayout;
    Context context;
    String groupMenuId;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        setContentView(R.layout.activity_change_group_service);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Изменить услугу");
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        ButterKnife.bind(this);
        groupMenuId = getIntent().getStringExtra(Constants.GROUP);
        context = this;
        priceList = new ArrayList<ReversePrices>();
        groupMenu = new Washer.GroupMenu();
        serviceList = new ArrayList<Integer>();
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
                onBackPressed();
                break;
        }
        return true;
    }

    @OnClick (R.id.services)
    public void onServicesClicked(){
        servicesDialog.show();
    }

    public static <T> boolean listEqualsNoOrder(List<T> l1, List<T> l2) {
        final Set<T> s1 = new HashSet<>(l1);
        final Set<T> s2 = new HashSet<>(l2);

        return s1.equals(s2);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransitionWithCommonCloseTransition();
    }

    private void saveData(){
        if(TextUtils.isEmpty(groupName.getText().toString())){
            ToastUtil.display(this,"Введите название комплексной услуги");
            groupName.setError("Обязательное поле");
            return;
        }
//        if(!(serviceList!=null && serviceList.size()>1)){
//            ToastUtil.display(this,"Нужно выбрать более одной услуги");
//            return;
//        }

        if(!(priceList!=null&&priceList.size()>0)){
            ToastUtil.display(this,"Определите цены хотя бы на один тип машины");
            return;
        }

        for(Washer.GroupMenu g : washer.getGroupMenu()){
            if(!g.getId().equals(groupMenuId)) {
                if (g.getName().toLowerCase().equals(groupName.getText().toString().toLowerCase())) {
                    ToastUtil.display(this, "С таким названием существует комплекс, выберите другое имя");
                    return;
                }
//
//                if (listEqualsNoOrder(g.getServices(), serviceList)) {
//                    ToastUtil.display(this, "С такими услугами существует комплекс, выберите другие услуги");
//                    return;
//                }
            }
        }

        setWaitScreen(true);
        BA.getEventBus().post(new AdminGroupMenuChangeRequestEvent(washer.getId(),groupMenuId, groupName.getText().toString(), serviceList, priceList));
    }

    @Subscribe
    public void onGroupMenuResponseReceived(AdminGroupMenuChangeResponseEvent event){
        setWaitScreen(false);
        if(event.getData() != null){
            if(BaseAssist.isSuccess(event.getData())){
                finish();
                overridePendingTransitionWithCommonCloseTransition();
                ToastUtil.display(this,"Успешно изменен комплекс услуг");
            }
            else{
                ToastUtil.display(this, event.getData().getMessage());
            }
        }
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
        if(washer != null){
//            saveData();
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if(washer != null) {
            for(Washer.GroupMenu g: washer.getGroupMenu()){
                if(g.getId().equals(groupMenuId)){
                    groupMenu = g;
                    break;
                }
            }
            groupName.setText(groupMenu.getName());
            priceList = groupMenu.getPrices();
            serviceList = groupMenu.getServices();
            preparePriceView();
            final List<HashMap<String,String>> servicesMapList = new ArrayList<HashMap<String,String>>();
            final HashMap<Integer,String> allServices = BA.getReference().getServices();
            List<HashMap<String,String>> selectedServicesMapList = new ArrayList<HashMap<String,String>>();

            for (Map.Entry<Integer,String> entry : allServices.entrySet()) {
                HashMap<String,String> serviceMap = new HashMap<String,String>();
                Integer key = entry.getKey();
                String value = entry.getValue();
                serviceMap.put(MultipleSelectDialog.ID, Integer.toString(key));
                serviceMap.put(MultipleSelectDialog.NAME, value);
                servicesMapList.add(serviceMap);
            }

            String serviceNames = "";
            int k = 0;
            if(groupMenu.getServices()!=null && groupMenu.getServices().size() > 0) {
                for (Integer service : groupMenu.getServices()) {
                    HashMap<String, String> serviceMap = new HashMap<String, String>();
                    serviceMap.put(MultipleSelectDialog.ID, Integer.toString(service));
                    serviceMap.put(MultipleSelectDialog.NAME, allServices.get(service));
                    selectedServicesMapList.add(serviceMap);
                    serviceNames = ((k == 0) ? "" : (serviceNames + " + ")) + ((BA.getReference().getServices().get(service)));
                    k++;
                }
                textServices.setText(serviceNames);
            }
            else{
                 textServices.setText("Выберите услуги");
            }

            servicesDialog = new MultipleSelectDialog(AdminChangeGroupServicePriceActivity.this, servicesMapList, "Выберите услуги", selectedServicesMapList) {
                @Override
                public void onDismiss(HashMap<Integer, String> namesSelectedItm) {
                    if(serviceList != null)
                       serviceList.clear();
                    else
                       serviceList = new ArrayList<Integer>();
                    if(namesSelectedItm.size() == 0){
                        textServices.setText("Выберите услуги");
                    }
                    else if(namesSelectedItm.size() == 1){
                        serviceList.add(Integer.parseInt(namesSelectedItm.get(0)));
                        textServices.setText(allServices.get(Integer.parseInt(namesSelectedItm.get(0))));
                    }
                    else{
                        textServices.setText("Выбрано ("+namesSelectedItm.size()+") услуг");
                        String serviceNames = "";
                        for(int i = 0; i < namesSelectedItm.size(); i++){
                            serviceNames = ((i==0)?"":(serviceNames + " + ")) + ((BA.getReference().getServices().get(Integer.parseInt(namesSelectedItm.get(i)))));
                            serviceList.add(Integer.parseInt(namesSelectedItm.get(i)));
                        }
                        textServices.setText(serviceNames);
                    }
//                    washer.setServices(new ArrayList<Integer>(requestServices));
//                    requestServices.clear();
                }
            };

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
            for(ReversePrices price : priceList) {
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
                        for (Iterator<ReversePrices> iter = priceList.listIterator(); iter.hasNext();) {
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

                        priceList.add(price);
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
