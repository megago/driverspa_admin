package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.URL_PREFIX;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.driverspa.adapter.CarsGridAdapter;
import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.fragment.ClientCarDefinitionFragment;
import com.driverspa.model.CarItem;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.ClientCreateRequest;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.CarDefinitionItemRequestEvent;
import com.driverspa.util.otto.ws.ClientCreateRequestEvent;
import com.driverspa.util.otto.ws.ClientCreateResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableGridView;

public class AdminAddClientActivity extends AdminBaseActivity {

    @BindView(R.id.clientName)
    EditText clientName;
    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.birthDate)
    EditText birthDate;
    @BindView(R.id.discount)
    EditText discount;
    @BindView(R.id.bonusPercentage)
    EditText bonusPercentage;
    @BindView(R.id.disableBonusAccrual)
    CheckBox disableBonusAccrual;
    Washer washer;
    @BindView(R.id.grid_cars)
    ExpandableGridView gridCars;
    private CarsGridAdapter carAdapter;
    private ClientCarDefinitionFragment carDefinitionDialogFragment;
    List<CarItem> carItemList = new ArrayList<CarItem>();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_admin_add_client);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText(BA.str(R.string.add_client));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);

        //Car adapter
        ViewGroup.LayoutParams layoutParamsCar = gridCars.getLayoutParams();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth()-(int) Functions.dipToPixels(this, 20);
        layoutParamsCar.width = width; //this is in pixels
        gridCars.setLayoutParams(layoutParamsCar);
        gridCars.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 45)) / 4);

        carItemList.add(new CarItem());
        carAdapter = new CarsGridAdapter(this,carItemList);
        gridCars.setAdapter(carAdapter);
        gridCars.setExpanded(true);
        gridCars.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (position == 0) {
                    openCarDefinition();
                } else {
                    carAdapter.setSelectedPosition(position);
                    if (carAdapter.getSelectedView() != null)
                        carAdapter.getSelectedView().setSelected(false);
                    carAdapter.setSelectedView(v);
                    v.setSelected(true);
                }
            }
        });

        discount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int value = Integer.parseInt(s.toString());
                    if(value > 100){
                        discount.setText("100");
                    }
                }
                catch(Exception e){}
            }
        });

        bonusPercentage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int value = Integer.parseInt(s.toString());
                    if(value > 100){
                        bonusPercentage.setText("100");
                    }
                }
                catch(Exception e){}
            }
        });

        try {
            MaskedWatcher maskedWatcher = new MaskedWatcher("### ###-##-##");
            phone.addTextChangedListener(maskedWatcher);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void openCarDefinition() {
        carDefinitionDialogFragment = new ClientCarDefinitionFragment();
        carDefinitionDialogFragment.setCancelable(false);
        carDefinitionDialogFragment.show(getSupportFragmentManager().beginTransaction(), "DialogFragment");
    }

    @Subscribe
    public void onCarDefinitionReceived(CarDefinitionItemRequestEvent event){
        closeKeyboard();
        if(carItemList != null && carItemList.size() > 0)
            for(CarItem car : carItemList) {
                if (car.getCarNumber() != null && event.getCar().getCarNumber().toLowerCase().equals(car.getCarNumber().toLowerCase())) {
                    ToastUtil.displayAtTop(this, BA.str(R.string.car_with_number_exists));
                    return;
                }
            }

        carItemList.add(event.getCar());
        carAdapter = new CarsGridAdapter(this,carItemList);
        gridCars.setAdapter(carAdapter);
        gridCars.setExpanded(true);
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
                setResult(RESULT_CANCELED);
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        closeKeyboard();
        super.onResume();
        BA.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
    }

    @OnClick(R.id.btnSend)
    public void onAddClientsClicked(){
//        if(TextUtils.isEmpty(clientName.getText().toString())){
//            ToastUtil.display(this,"Введите ФИО клиента");
//            clientName.requestFocus();
//            return;
//        }
        String phoneStr = phone.getText().toString();
        phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

//        if(TextUtils.isEmpty(phoneStr)){
//            ToastUtil.display(this,"Введите номер телефона");
//            phone.requestFocus();
//            return;
//        }

//        if((carItemList!=null && carItemList.size() < 2) || carItemList==null){
//            ToastUtil.display(this,"Добавьте машину клиента");
//            phone.requestFocus();
//            return;
//        }
        List<CarItem> requestCars = new ArrayList<>();
        if(carItemList!= null && carItemList.size() > 0)
            for(int i = 0; i < carItemList.size(); i++){
                if(i == 0) continue;
                requestCars.add(carItemList.get(i));
            }
            ClientCreateRequest request = new ClientCreateRequest.Builder()
                                                                    .name(clientName.getText().toString())
                                                                    .phone((!TextUtils.isEmpty(phoneStr)?"+7"+phoneStr:""))
                                                                    .cars(requestCars)
                                                                    .carwash(URL_PREFIX+washer.getId())
                                                                    .discount(Integer.parseInt(TextUtils.isEmpty(discount.getText().toString())?"0":discount.getText().toString()))
                                                                    .bonusAccrualPercentage(Integer.parseInt(TextUtils.isEmpty(bonusPercentage.getText().toString())?"0":bonusPercentage.getText().toString()))
                                                                    .disableBonusAccrual(disableBonusAccrual.isChecked()).create();
        BA.getEventBus().post(new ClientCreateRequestEvent(request));
        setWaitScreen(true);
    }

    @Subscribe
    public void onClientCreateResponseReceived(ClientCreateResponseEvent event) {
        setWaitScreen(false);
        if(event.getData() != null){
            if(BaseAssist.isSuccess(event.getData())){
                ToastUtil.display(this,BA.str(R.string.client_added_ok));
                setResult(RESULT_OK);
                finish();
                overridePendingTransitionWithCommonCloseTransition();
            }
            else
                ToastUtil.display(this,event.getData().getMessage());
        }
        else{
            ToastUtil.display(this,BA.str(R.string.err_add_client));
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer == null) {
            finish();
            overridePendingTransitionWithCommonCloseTransition();
            ToastUtil.display(this,BA.str(R.string.err_add_client_unknown));
        }
    }
}
