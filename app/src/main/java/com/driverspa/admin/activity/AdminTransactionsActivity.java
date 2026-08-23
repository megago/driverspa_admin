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

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    Instant startTimeDt;
    @BindView(R.id.endTime)
    TextView endTime;
    Instant endTimeDt;
    @BindView(R.id.btnAdd)
    View buttonAdd;

    @BindView(R.id.informationTextLayout)
    View informationTextLayout;
    @BindView(R.id.informationText)
    TextView informationText;

    Washer washer;
    AdminCarwashTransactionsAdapter adapter;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());
    DateTimeFormatter requestFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(ZoneId.systemDefault());
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
        titleView.setText(BA.str(R.string.transactions));
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
            HashMap<String,String> startTimeMap = Functions.formatUTCDate(Instant.ofEpochMilli(workShiftStartDate));
//            startTime.setText(startTimeMap.get(Functions.DATE)+" "+startTimeMap.get(Functions.TIME));
                  startTime.setText(timeFormatter.format(Instant.ofEpochMilli(workShiftStartDate)));
//            endTime.setText(endTimeMap.get(Functions.DATE)+" "+endTimeMap.get(Functions.TIME));
                  endTime.setText(timeFormatter.format(Instant.ofEpochMilli(workShiftEndDate)));

            startTimeDt = Instant.ofEpochMilli(workShiftStartDate);
            endTimeDt = Instant.ofEpochMilli(workShiftEndDate);
            buttonAdd.setVisibility(View.GONE);
        }
        else {
            startTimeDt = Functions.getStartOfDay(Instant.now());
            endTimeDt = Functions.getEndOfDay(Instant.now());
			startTime.setText(formatter.format(startTimeDt));
            startTime.setPaintFlags(startTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			endTime.setText(formatter.format(endTimeDt));
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
                    if(transactions.size() == 0) addEmptyTextView(listView, BA.str(R.string.no_transactions));
                } else {
                    addEmptyTextView(listView, BA.str(R.string.no_transactions));
                }
            } else {
                addEmptyTextView(listView, BA.str(R.string.no_transactions));
                ToastUtil.display(this, event.getData().getMessage());
            }
        } else {
            addEmptyTextView(listView, BA.str(R.string.no_transactions));
            ToastUtil.display(this, BA.str(R.string.err_get_data));
        }
    }

    @Override
    public void onDatePickerDataset(Long time, String type) {
        if (type.equals("start")) {
      startTimeDt = Functions.getStartOfDay(Instant.ofEpochMilli(time));
      startTime.setText(formatter.format(startTimeDt));
            if (washer != null && startTimeDt != null)
                loadData(washer, startTimeDt, endTimeDt);
        } else if (type.equals("end")) {
      endTimeDt = Functions.getStartOfDay(Instant.ofEpochMilli(time));
      endTime.setText(formatter.format(endTimeDt));
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
          DatePickerFragment picker = DatePickerFragment.newInstance("start", startTimeDt.toEpochMilli());
            picker.setDatePickerDateSetListener(this);
            picker.show(getSupportFragmentManager(), "datePicker");
        }
    }

    @OnClick(R.id.endTime)
    public void endTimeClicked() {
        if(!fromWorkshift) {
          DatePickerFragment picker = DatePickerFragment.newInstance("end", endTimeDt.toEpochMilli());
            picker.setDatePickerDateSetListener(this);
            picker.show(getSupportFragmentManager(), "datePicker");
        }
    }

      private void loadData(Washer washer, Instant startTimeDt, Instant endTimeDt) {
        setWaitScreen(true);
        filter.setLimit("1000");
        filter.setCarwash(washer.getId());
        filter.setStartDate(requestFormatter.format(startTimeDt));
        filter.setEndDate(requestFormatter.format(endTimeDt));
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
            valuesArray[i] = BA.str(R.string.all_operation_types);
            keysArray[i++] = null;
            for(LinkedHashMap.Entry<Integer,String> entry : operationTypes.entrySet()){
                String operationType = entry.getValue();
                if(!TextUtils.isEmpty(operationType) && operationType.length() > 33)
                    operationType = operationType.substring(0,33)+"...";
                valuesArray[i] =operationType;
                keysArray[i] = entry.getKey();
                i++;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
            adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);

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
