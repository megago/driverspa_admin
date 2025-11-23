package com.driverspa.admin.activity;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.FilterableClientsAdapter;
import com.driverspa.adapter.FilterableStaffsAdapter;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.Staff;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddRequestEvent;
import com.driverspa.util.otto.ws.AdminCarwashTransactionAddResponseEvent;
import com.driverspa.util.otto.ws.AdminClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;
import com.driverspa.util.otto.ws.AdminStaffListRequestEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DelayAutoCompleteTextView;
import com.driverspa.view.NDSpinner;

public class AdminAddCarwashTransactionActivity extends AdminBaseActivity implements DatePickerDateSetListener {

    public static final String TRANSACTION_CODE = "TRANSACTION_CODE";

    @BindView(R.id.clientLayout)
    View clientLayout;
    @BindView(R.id.staffLayout)
    View staffLayout;
    @BindView(R.id.paymentTypeLayout)
    View paymentTypeLayout;

    @BindView(R.id.description)
    EditText description;
    @BindView(R.id.date)
    TextView date;
    @BindView(R.id.time)
    TextView time;

    @BindView(R.id.staff)
    DelayAutoCompleteTextView staffComplete;
    @BindView(R.id.client)
    DelayAutoCompleteTextView clientComplete;

    @BindView(R.id.amount)
    EditText amount;
    @BindView(R.id.operationType)
    NDSpinner operationType;
    @BindView(R.id.paymentType)
    NDSpinner paymentType;
    Washer washer;
    Date defaultDate;
    int hour, minute;
    boolean isVisible = false;

    Integer selectedOperationType;
    private boolean isSpinnerTouched = false;
    protected FilterableClientsAdapter clientsAdapter;
    protected FilterableStaffsAdapter staffsAdapter;
    ClientInfo selectedClient;
    Staff selectedStaff;
    AdminSearchFilter filter = new AdminSearchFilter();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    DecimalFormat formatter = new DecimalFormat("#,###.##");
    LinkedHashMap<Integer,String> operationTypes =  BA.getReference().getOperationTypes();
    LinkedHashMap<String,String> paymentTypes =  BA.getReference().getPaymentTypes();
    AdminCarwashTransactionAddRequestEvent request = new AdminCarwashTransactionAddRequestEvent();

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_admin_add_carwash_transaction);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Финансовая операция");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);

        if(defaultDate == null) defaultDate = new Date();

        hour = defaultDate.getHours();
        minute = defaultDate.getMinutes();
        time.setText(hour+":"+minute);
        date.setText(dateFormatter.format(defaultDate.getTime()));
        operationType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });

        paymentType.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });

        selectedOperationType = 1;
        if(getIntent().getIntExtra(TRANSACTION_CODE,-1) != -1){
            selectedOperationType = getIntent().getIntExtra(TRANSACTION_CODE,-1);
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
                closeKeyboard();
                finish();
                overridePendingTransitionWithCommonCloseTransition();
                break;
        }
        return true;
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

    @OnClick(R.id.btnSend)
    public void onAddBoxButtonClicked(){
        closeKeyboard();
        if(TextUtils.isEmpty(description.getText().toString())){
            ToastUtil.display(this,"Введите назначение платежа");
            description.requestFocus();
            description.setError("Обязательное поле");
            return;
        }

        if(clientLayout.getVisibility() == View.VISIBLE && selectedClient == null){
            ToastUtil.display(this,"Выберите клиента");
            clientComplete.requestFocus();
            clientComplete.setError("Обязательное поле");
            return;
        }
        if(staffLayout.getVisibility() == View.VISIBLE && selectedStaff == null){
            ToastUtil.display(this,"Выберите сотрудника");
            clientComplete.requestFocus();
            clientComplete.setError("Обязательное поле");
            return;
        }

        if(TextUtils.isEmpty(amount.getText().toString())){
            ToastUtil.display(this,"Введите сумму");
            amount.requestFocus();
            amount.setError("Обязательное поле");
            return;
        }

        request.setWasherId(washer.getId());

        request.setAmount(Double.parseDouble(amount.getText().toString()));
        request.setDescription(description.getText().toString());
        if(selectedClient != null)
          request.setClientId(selectedClient.getId());
        if(selectedStaff != null)
          request.setStaffId(selectedStaff.getUid());
        request.setOperationType(selectedOperationType+1);
        request.setTranDate(defaultDate);
        request.setCarwash(Constants.URL_PREFIX+washer.getId());
        setWaitScreen(true);
        BA.getEventBus().post(request);
    }


    @Subscribe
    public void onAdminAddOperationReceived(AdminCarwashTransactionAddResponseEvent event){
        setWaitScreen(false);
        if(event.getData() != null){
            if(BaseAssist.isSuccess(event.getData())){
                ToastUtil.display(this, "Успешно добавили операцию");
                finish();
            }
            else{
                ToastUtil.display(this, event.getData().getMessage());
            }
        }
        else{
            ToastUtil.display(this, "Ошибка при добавлении операции");
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {
            BA.getEventBus().post(new AdminStaffListRequestEvent(washer.getId()));
            createOperationTypeSpinner();
            createPaymentTypeSpinner();
            clientComplete.setThreshold(2);
            clientsAdapter = new FilterableClientsAdapter(AdminAddCarwashTransactionActivity.this, new ArrayList<ClientInfo>(), washer);
            clientComplete.setAdapter(clientsAdapter);
            clientsAdapter.setCarNo(clientComplete);
            clientComplete.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_bar));
            clientComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    ClientInfo client = ((ClientInfo) clientsAdapter.getItem(position));
                    setData(client);
                }
            });

            staffComplete.setThreshold(0);
            staffsAdapter = new FilterableStaffsAdapter(AdminAddCarwashTransactionActivity.this, new ArrayList<Staff>(), washer, staffComplete);
            staffComplete.setAdapter(staffsAdapter);
            staffComplete.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_bar2));
            staffComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Staff staff = ((Staff) staffsAdapter.getItem(position));
                    selectedStaff = staff;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        staffComplete.setText(staff.getFirstName()+" ("+formatter.format(staff.getAmount())+" ₸)", false);
                        staffComplete.setSelection(staffComplete.getText().length());

                    }
                    else{
                        staffComplete.setText(staff.getFirstName()+" ("+formatter.format(staff.getAmount())+" ₸)");
                        staffComplete.setSelection(staffComplete.getText().length());
                    }
                }
            });
            operationTypeSelector(selectedOperationType+1);
        }
    }

    @OnFocusChange(R.id.staff)
    public void onFocusChangeStaff(boolean focus) {
        if (focus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                staffComplete.setText("", false);
                selectedStaff = null;
            }
            else{
                staffComplete.setText("");
                selectedStaff = null;
            }
        }
    }

    @OnFocusChange(R.id.client)
    public void onFocusChangeClient(boolean focus) {
        if (focus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                clientComplete.setText("", false);
                selectedClient = null;
            }
            else{
                clientComplete.setText("");
                selectedClient = null;
            }
        }
    }


    @Subscribe
    public void onSearchSubmitted(SearchEvent event) {
        if (event != null && event.getQuery() != null && filter != null && washer != null) {
            if (clientsAdapter != null && clientComplete != null) {
                filter.setSearchText(event.getQuery());
            }
            filter.setCarwash(washer.getId());
            filter.setOffset(Integer.toString(0));
            filter.setClientType("all");
            BA.getEventBus().post(new AdminClientsRequestEvent(filter));
        }
    }

    protected void setData(ClientInfo client) {
        this.selectedClient = client;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            clientComplete.setText(client.getName()+" ("+formatter.format(client.getDepositAmount())+" ₸)", false);
            clientComplete.setSelection(clientComplete.getText().length());

        }
        else{
            clientComplete.setText(client.getName()+" ("+formatter.format(client.getDepositAmount())+" ₸)");
            clientComplete.setSelection(clientComplete.getText().length());
        }
    }

    @Subscribe
    public void onAdminClientsReceived(final AdminClientsResponseEvent event) {
        if (clientComplete != null) clientComplete.stopLoading();
        if (washer != null) {
            if (event.getResult() != null) {
                if (clientsAdapter != null && BaseAssist.isSuccess(event.getResult()) && event.getResult() != null && event.getResult().getResponse() != null && event.getResult().getResponse().getClients() != null) {
                    final Filter newFilter = new Filter() {
                        @Override
                        protected FilterResults performFiltering(CharSequence constraint) {
                            FilterResults filterResults = new FilterResults();
                            if (constraint != null) {
                                filterResults.values = event.getResult().getResponse().getClients();
                                filterResults.count = event.getResult().getResponse().getClients().size();
                            }
                            return filterResults;
                        }

                        @Override
                        protected void publishResults(CharSequence constraint, FilterResults results) {
                            if (results != null && results.count > 0) {
                                clientsAdapter.setFilter(this);
                            }
                        }
                    };
                    if (!TextUtils.isEmpty(filter.getSearchText()))
                        clientsAdapter.setSearchedPossibleCarNo(filter.getSearchText() != null ? filter.getSearchText() : "");
                    else
                        clientsAdapter.setSearchedPossibleCarNo(filter.getCarNo() != null ? filter.getCarNo() : "");
                    clientsAdapter.setData(event.getResult().getResponse().getClients());
                    clientsAdapter.notifyDataSetChanged();
                } else {

                }
            } else {

            }
        }
    }

    private void createOperationTypeSpinner(){
        if(operationType != null) {

            String[] valuesArray = new String[operationTypes.size()-1];
            final Integer[] keysArray = new Integer[operationTypes.size()-1];

            int i = 0;
            for(LinkedHashMap.Entry<Integer,String> entry : operationTypes.entrySet()){
                if(i==keysArray.length) break;
                String operationType = entry.getValue();
                if(!TextUtils.isEmpty(operationType) && operationType.length() > 33)
                    operationType = operationType.substring(0,33)+"...";
                valuesArray[i] =operationType;
                keysArray[i] = entry.getKey();
                i++;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valuesArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            operationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    if (!isSpinnerTouched) return;
                    selectedOperationType = keysArray[pos];
                    operationTypeSelector(selectedOperationType);
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            operationType.setAdapter(adapter);
        }
        operationType.setSelection(selectedOperationType);
    }

    private void createPaymentTypeSpinner(){
        if(paymentType != null) {

            String[] valuesArray = new String[paymentTypes.size()-1];
            final String[] keysArray = new String[paymentTypes.size()-1];
            int i = 0;

            for(LinkedHashMap.Entry<String,String> entry : paymentTypes.entrySet()){
                if(i==keysArray.length) break;
                String paymentType = entry.getValue();
                valuesArray[i] = paymentType;
                keysArray[i] = entry.getKey();
                i++;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valuesArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            paymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    if (!isSpinnerTouched) return;
                    request.setPaymentType(keysArray[pos]);
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            paymentType.setAdapter(adapter);
        }
    }

    private void operationTypeSelector(Integer selectedOperationType){
        request.setOperationType(selectedOperationType);
        request.setPaymentType("C");
        switch (selectedOperationType){
            case 1:
                description.setText("Вручное начисление зп");
                paymentTypeLayout.setVisibility(View.GONE);
                clientLayout.setVisibility(View.GONE);
                staffLayout.setVisibility(View.VISIBLE);
                break;
            case 2:
                paymentTypeLayout.setVisibility(View.VISIBLE);
                description.setText("Вручное поступление");
                clientLayout.setVisibility(View.GONE);
                staffLayout.setVisibility(View.GONE);
                break;
            case 3:
                description.setText("Приход");
                paymentTypeLayout.setVisibility(View.VISIBLE);
                clientLayout.setVisibility(View.GONE);
                staffLayout.setVisibility(View.GONE);
                break;
            case 4:
                description.setText("Выдача зп");
                paymentTypeLayout.setVisibility(View.VISIBLE);
                clientLayout.setVisibility(View.GONE);
                staffLayout.setVisibility(View.VISIBLE);
                break;
            case 5:
                description.setText("");
                paymentTypeLayout.setVisibility(View.GONE);
                clientLayout.setVisibility(View.VISIBLE);
                staffLayout.setVisibility(View.GONE);
                break;
            case 6:
                description.setText("Предоплата наличными");
                clientLayout.setVisibility(View.VISIBLE);
                staffLayout.setVisibility(View.GONE);
                break;
            case 7:
                description.setText("Предоплата картой");
                clientLayout.setVisibility(View.VISIBLE);
                staffLayout.setVisibility(View.GONE);
                break;
            case 8:
                paymentTypeLayout.setVisibility(View.VISIBLE);
                description.setText("Предоплата перечислением");
                clientLayout.setVisibility(View.VISIBLE);
                staffLayout.setVisibility(View.GONE);
                break;
            case 9:
                paymentTypeLayout.setVisibility(View.VISIBLE);
                description.setText("Расход");
                clientLayout.setVisibility(View.GONE);
                staffLayout.setVisibility(View.GONE);
                break;
            case 10:
                description.setText(operationTypes.get(selectedOperationType));
                clientLayout.setVisibility(View.GONE);
                staffLayout.setVisibility(View.GONE);
//                description.setText("Списание депозит клиента");
                break;
            case 11:
                description.setText(operationTypes.get(selectedOperationType));
                clientLayout.setVisibility(View.GONE);
                staffLayout.setVisibility(View.GONE);
//                description.setText("Списание депозит клиента");
                break;
        }
    }

    @Override
    public void onDatePickerDataset(Long time, String type) {
        defaultDate.setTime(time);
        defaultDate.setHours(hour);
        defaultDate.setMinutes(minute);
        date.setText(dateFormatter.format(defaultDate.getTime()));
    }

    @OnClick(R.id.date)
    public void dateClicked() {
        DatePickerFragment picker = DatePickerFragment.newInstance("start", defaultDate.getTime());
        picker.setDatePickerDateSetListener(this);
        picker.show(getSupportFragmentManager(), "datePicker");
    }

    @OnClick(R.id.time)
    public void timeClicked() {
        showTimePicker(time);
    }

    private void showTimePicker(final TextView targetText) {
        final Dialog dialog = new Dialog(this, R.style.DialogWithNoActionBar);
        dialog.setContentView(R.layout.dialog_time_picker_single);
        dialog.setTitle("время");
        final TimePicker timeFrom = (TimePicker) dialog.findViewById(R.id.time);
        timeFrom.setIs24HourView(true);

        timeFrom.setCurrentHour(hour);
        timeFrom.setCurrentMinute(minute);

        switch (targetText.getId()) {
            case R.id.time:
                timeFrom.setCurrentHour(hour);
                timeFrom.setCurrentMinute(minute);
                break;
        }

        final Button button = (Button) dialog.findViewById(R.id.ok);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (targetText.getId()) {
                    case R.id.time:
                        hour = timeFrom.getCurrentHour();
                        minute = timeFrom.getCurrentMinute();
                        targetText.setText(hour+":"+minute);
                        defaultDate.setHours(hour);
                        defaultDate.setMinutes(minute);
                        break;
                }

                dialog.dismiss();
                isVisible = false;
            }
        });
        if(!isVisible){
            dialog.show();
            isVisible = true;
        }
    }
}