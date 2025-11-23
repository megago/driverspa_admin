package com.driverspa.admin.activity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.AdminCarwashTransactionsAdapter;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.CarwashTransaction;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminCarwashTransactionsRequestEvent;
import com.driverspa.util.otto.ws.AdminCarwashTransactionsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;


public class AdminTransactionsActivity extends AdminBaseActivity implements DatePickerDateSetListener {

    public static final String FROM_WORKSHIFT="FROM_WORKSHIFT";
    public static final String START_DATE="START_DATE";
    public static final String END_DATE="END_DATE";
    public static final String OPERATION_TYPE="OPERATION_TYPE";
    public static final String INFORMATION_TEXT="INFORMATION_TEXT";

    boolean fromWorkshift = false;
    long workShiftStartDate;
    long workShiftEndDate;

    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.startTime)
    TextView startTime;
    Date startTimeDt;
    @BindView(R.id.endTime)
    TextView endTime;
    Date endTimeDt;
    @BindView(R.id.btnAdd)
    View buttonAdd;

    @BindView(R.id.informationTextLayout)
    View informationTextLayout;
    @BindView(R.id.informationText)
    TextView informationText;

    Washer washer;
    AdminCarwashTransactionsAdapter adapter;
    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    SimpleDateFormat timeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    DecimalFormat decimalFormatter = new DecimalFormat("#,###.##");
    NDSpinner spinner;
    List<CarwashTransaction> transactions;
    private boolean isSpinnerTouched = false;
    AdminSearchFilter filter = new AdminSearchFilter();
    int selectedOperationType = 0;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_admin_transactions);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        spinner = (NDSpinner) mToolbar.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);

        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Транзакции");
        titleView.setVisibility(View.GONE);
        mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);
        listView.setVisibility(View.VISIBLE);

        fromWorkshift=getIntent().getBooleanExtra(FROM_WORKSHIFT,false);
        if(fromWorkshift){
            workShiftStartDate = getIntent().getLongExtra(START_DATE,0);
            workShiftEndDate = getIntent().getLongExtra(END_DATE,0);
        }

        if(fromWorkshift && workShiftStartDate != 0 && workShiftEndDate != 0){
            HashMap<String,String> startTimeMap = Functions.formatUTCDate(new Date(workShiftStartDate));
//            HashMap<String,String> startTimeMap = Functions.form(new Date(workShiftStartDate));
//            startTime.setText(startTimeMap.get(Functions.DATE)+" "+startTimeMap.get(Functions.TIME));
            startTime.setText(timeFormatter.format(workShiftStartDate));
//            HashMap<String,String> endTimeMap = Functions.formatUTCDate(new Date(workShiftEndDate));
//            endTime.setText(endTimeMap.get(Functions.DATE)+" "+endTimeMap.get(Functions.TIME));
            endTime.setText(timeFormatter.format(workShiftEndDate));

            startTimeDt = new Date(workShiftStartDate);
            endTimeDt = new Date(workShiftEndDate);
            buttonAdd.setVisibility(View.GONE);
        }
        else {
            startTimeDt = Functions.getStartOfDay(new Date());
            endTimeDt = Functions.getEndOfDay(new Date());
            startTime.setText(formatter.format(startTimeDt.getTime()));
            startTime.setPaintFlags(startTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            endTime.setText(formatter.format(endTimeDt.getTime()));
            endTime.setPaintFlags(endTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        filter.setOperationType(null);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });
        createActionBarSpinner();
        if(!TextUtils.isEmpty(getIntent().getStringExtra(OPERATION_TYPE))){
//            selectedOperationType = getIntent().getIntExtra(OPERATION_TYPE,0);
            filter.setOperationType(getIntent().getStringExtra(OPERATION_TYPE));
            spinner.setSelection(selectedOperationType);
        }

        if(!TextUtils.isEmpty(getIntent().getStringExtra(INFORMATION_TEXT))){
//            informationTextLayout.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(getIntent().getStringExtra(INFORMATION_TEXT));
//            informationText.setText(getIntent().getStringExtra(INFORMATION_TEXT));
            spinner.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onAdminCarwashTransactionsReceived(AdminCarwashTransactionsResponseEvent event) {
        setWaitScreen(false);
        if (event.getData() != null) {
            if (BaseAssist.isSuccess(event.getData())) {
                transactions = event.getData().getResponse().getCarwashTransactions();
                if (transactions != null) {
                    adapter = new AdminCarwashTransactionsAdapter(this);
                    adapter.set(transactions);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    if(transactions.size() == 0) addEmptyTextView(listView, "Нет проводок");
                } else {
                    addEmptyTextView(listView, "Нет проводок");
                }
            } else {
                addEmptyTextView(listView, "Нет проводок");
                ToastUtil.display(this, event.getData().getMessage());
            }
        } else {
            addEmptyTextView(listView, "Нет проводок");
            ToastUtil.display(this, "Ошибка при получении данных");
        }
    }

    @Override
    public void onDatePickerDataset(Long time, String type) {
        if (type.equals("start")) {
            startTimeDt = Functions.getStartOfDay(new Date(time));
            startTime.setText(formatter.format(startTimeDt.getTime()));
            if (washer != null && startTimeDt != null)
                loadData(washer, startTimeDt, endTimeDt);
        } else if (type.equals("end")) {
            endTimeDt = Functions.getStartOfDay(new Date(time));
            endTime.setText(formatter.format(endTimeDt.getTime()));
            if (washer != null && endTimeDt != null)
                loadData(washer, startTimeDt, endTimeDt);
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
        setWaitScreen(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
    }

    @OnClick(R.id.startTime)
    public void startTimeClicked() {
        if(!fromWorkshift) {
            DatePickerFragment picker = DatePickerFragment.newInstance("start", startTimeDt.getTime());
            picker.setDatePickerDateSetListener(this);
            picker.show(getSupportFragmentManager(), "datePicker");
        }
    }

    @OnClick(R.id.endTime)
    public void endTimeClicked() {
        if(!fromWorkshift) {
            DatePickerFragment picker = DatePickerFragment.newInstance("end", endTimeDt.getTime());
            picker.setDatePickerDateSetListener(this);
            picker.show(getSupportFragmentManager(), "datePicker");
        }
    }

    private void loadData(Washer washer, Date startTimeDt, Date endTimeDt) {
        setWaitScreen(true);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String formattedDate = formatter.format(startTimeDt.getTime());
        filter.setLimit("1000");
        filter.setCarwash(washer.getId());
        filter.setStartDate(formatter.format(startTimeDt.getTime()));
        filter.setEndDate(formatter.format(endTimeDt.getTime()));
        BA.getEventBus().post(new AdminCarwashTransactionsRequestEvent(filter));
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {
            setWaitScreen(true);
            loadData(washer, startTimeDt, endTimeDt);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {

                }
            });
        }
    }

    private void createActionBarSpinner(){
        if(spinner != null) {
            LinkedHashMap<Integer,String> operationTypes =  BA.getReference().getOperationTypes();
            String[] valuesArray = new String[operationTypes.size()+1];
            final Integer[] keysArray = new Integer[operationTypes.size()+1];

            int i = 0;
            valuesArray[i] = "Все типы операции";
            keysArray[i++] = null;
            for(LinkedHashMap.Entry<Integer,String> entry : operationTypes.entrySet()){
                String operationType = entry.getValue();
                if(!TextUtils.isEmpty(operationType) && operationType.length() > 33)
                    operationType = operationType.substring(0,33)+"...";
                valuesArray[i] =operationType;
                keysArray[i] = entry.getKey();
                i++;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valuesArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    if (!isSpinnerTouched) return;
                    if(pos == 0) filter.setOperationType(null);
                    else filter.setOperationType(Integer.toString(keysArray[pos]));
                    loadData(washer,startTimeDt,endTimeDt);
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spinner.setAdapter(adapter);
        }
    }

    @OnClick(R.id.btnAdd)
    public void onButtonAddClicked(){
        Intent intent = new Intent(this,AdminAddCarwashTransactionActivity.class);
        startActivity(intent);
    }
}
