package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.CLIENT_ALL_SMS;
import static com.driverspa.util.Constants.VIEW_FOOTER;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.ClientsFilterAdapter;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.LocalBookingList;
import com.driverspa.model.Washer;
import com.driverspa.model.api.response.ClientsResponse;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.NDSpinner;

/**
 * Created by Yerzhan Tanatov on 26/12/16.
 */
public class ClientSMSSendActivity extends AdminBaseActivity {

    public static final String NEWS_TARGET_EXTRA = "NEWS_TARGET_EXTRA";
    public static final String SELECTED_CLIENTS = "SELECTED_CLIENTS";
    private int numberPage = 0;
    private ClientsFilterAdapter listAdapter;
    Washer washer;

    @BindView(R.id.recycler_linear)
    RecyclerView mRecyclerLinear;

    @BindView(R.id.noInternetLayout)
    LinearLayout noInternetLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.repeatConnect)
    Button repeatConnect;
    @BindView(R.id.noInternetMessage)
    TextView noIternetMsg;
    @BindView(R.id.noRecords)
    TextView noRecords;
    @BindView(R.id.recordsCount)
    TextView recordsCount;
    @BindView(R.id.search)
    EditText searchText;

    @BindView(R.id.washCountFilterSpinner)
    NDSpinner washCountFilterSpinner;
    @BindView(R.id.washDateFilterSpinner)
    NDSpinner washDateFilterSpinner;

    private boolean loading = false;
    private boolean noData = false;

    List<ClientInfo> clientInfoAdapterData;
    List<ClientInfo> clientFilteredAdapterData;
    private boolean lazyLoading = false;
    DividerItemDecoration itemDecoration;
    AdminSearchFilter filter = new AdminSearchFilter();
    TextView titleView;
    int washCountFilterItem = 0;
    int washDateFilterItem = 0;

    TextWatcher searchTextWatcher;

    String newsTarget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        setContentView(R.layout.activity_admin_send_sms);
        ButterKnife.bind(this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent resultIntent = new Intent();
                ClientsResponse response = new ClientsResponse();
                response.setClients(clientFilteredAdapterData);
                resultIntent.putExtra(SELECTED_CLIENTS,response.serialize());
                setResult(RESULT_OK,resultIntent);
                finish();
            }
        });

        searchTextWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                //search
                search(searchText.getText().toString());
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {}

            @Override
            public void afterTextChanged(Editable arg0) {}
        };

        searchText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    search(searchText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        newsTarget = getIntent().getStringExtra(NEWS_TARGET_EXTRA);

        searchText.addTextChangedListener(searchTextWatcher);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        itemDecoration = new DividerItemDecoration(this);
        makeList();
        createWashCountFilterSpinner();
        createWashDateFilterSpinner();
    }


    private void createWashCountFilterSpinner(){
        if(washCountFilterSpinner != null) {
            String[] valuesArray = new String[]{BA.str(R.string.all_word), BA.str(R.string.washed_lt_3), BA.str(R.string.washed_lt_6), BA.str(R.string.washed_lt_10), BA.str(R.string.washed_gt_15)};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
            adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);

            washCountFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {//
                    washCountFilterItem = pos;
                    filter();
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            washCountFilterSpinner.setAdapter(adapter);

        }
    }
    private void createWashDateFilterSpinner(){
        if(washDateFilterSpinner != null) {
            String[] valuesArray = new String[]{BA.str(R.string.all_word), BA.str(R.string.washed_gt_10days), BA.str(R.string.washed_gt_month), BA.str(R.string.washed_gt_3month)};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
            adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);

            washDateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {//
                    washDateFilterItem = pos;
                    filter();
                }
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            washDateFilterSpinner.setAdapter(adapter);

        }
    }

    private void filter(){
        if(clientFilteredAdapterData != null) {
            List<ClientInfo> filteredInfo = new ArrayList<ClientInfo>();
            List<ClientInfo> filteredInfo2 = new ArrayList<ClientInfo>();
            List<ClientInfo> filteredInfo3 = new ArrayList<ClientInfo>();
                  Instant today = Instant.now();
            for (ClientInfo client : clientInfoAdapterData) {
                switch (washCountFilterItem) {
                    case 0:
                        if (client.getFinishedBooksCount() >= 0)
                            filteredInfo.add(client);
                        break;
                    case 1:
                        if (client.getFinishedBooksCount() < 3)
                            filteredInfo.add(client);
                        break;
                    case 2:
                        if (client.getFinishedBooksCount() <= 6)
                            filteredInfo.add(client);
                        break;
                    case 3:
                        if (client.getFinishedBooksCount() <= 10)
                            filteredInfo.add(client);
                    case 4:
                        if (client.getFinishedBooksCount() >= 15)
                            filteredInfo.add(client);
                        break;
                }
            }
            for (ClientInfo client : filteredInfo) {
                if(client.getLastFinishedBookDate() == null) continue;
                                    long diff = today.toEpochMilli() - client.getLastFinishedBookDate().toEpochMilli();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                switch (washDateFilterItem) {
                    case 0:
                        if (days >= 0)
                            filteredInfo2.add(client);
                        break;
                    case 1:
                        if (days >= 10)
                            filteredInfo2.add(client);

                        break;
                    case 2:
                        if (days >= 30)
                            filteredInfo2.add(client);

                        break;
                    case 3:
                        if (days >= 90)
                            filteredInfo2.add(client);
                        break;
                }
            }

            String search = searchText.getText().toString();
            if (!TextUtils.isEmpty(search)) {
                for (ClientInfo client : filteredInfo2) {
                    if (client.getName() != null && client.getName().toLowerCase().contains(search.toLowerCase())) {
                        filteredInfo3.add(client);
                    } else if (client.getPhone() != null && client.getPhone().toLowerCase().contains(search.toLowerCase())) {
                        filteredInfo3.add(client);
                    } else if (client.getCarNo() != null && client.getCarNo().toLowerCase().contains(search.toLowerCase())) {
                        filteredInfo3.add(client);
                    }
                }
                clientFilteredAdapterData.clear();
                clientFilteredAdapterData.addAll(filteredInfo3);
                recordsCount.setText(clientFilteredAdapterData.size()+BA.str(R.string.clients_count));
                listAdapter.notifyDataSetChanged();
            } else {
                clientFilteredAdapterData.clear();
                clientFilteredAdapterData.addAll(filteredInfo2);
                recordsCount.setText(clientFilteredAdapterData.size()+BA.str(R.string.clients_count));
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    private void makeList() {
        clientInfoAdapterData = new ArrayList<ClientInfo>();
        clientFilteredAdapterData= new ArrayList<ClientInfo>();
        listAdapter = new ClientsFilterAdapter(this,clientFilteredAdapterData,washer);
        listAdapter.setData(clientFilteredAdapterData);
        createListLayout();
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        loadData(0,false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeKeyboard();
                setResult(ActivityForResult.ACTIVITY_SEARCH_HAS_NO_RESULT);
                finish();
                overridePendingTransition(0,0);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeKeyboard();
        setResult(ActivityForResult.ACTIVITY_SEARCH_HAS_NO_RESULT);
        finish();
        overridePendingTransition(0,0);
    }

    private void createListLayout(){
//        ((ClientsFilterAdapter) listAdapter).setOnItemClickListener(new ClientsFilterAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                final ClientInfo client = (ClientInfo)((clientFilteredAdapterData.get(position)));
//                closeKeyboard();
//                Intent resultData = new Intent();
//                resultData.putExtra(Constants.CLIENT_INFO,client.serialize());
//                setResult(ActivityForResult.ACTIVITY_SEARCH_HAS_RESULT,resultData);
//                finish();
//                overridePendingTransition(0,0);
//            }
//        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerLinear.setLayoutManager(manager);
        mRecyclerLinear.setAdapter(listAdapter);
        mRecyclerLinear.removeItemDecoration(itemDecoration);
        mRecyclerLinear.addItemDecoration(itemDecoration);
//        createScrollListener(mRecyclerLinear, manager, listAdapter);
    }

    private void createScrollListener(final RecyclerView mRecyclerView, final LinearLayoutManager mLayoutManager, final ClientsFilterAdapter adapter) {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItem = mLayoutManager.getItemCount();
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
//                if (!noData && !loading && lastVisibleItem == totalItem - 1) {
//                    if (clientFilteredAdapterData.size() > 0 && (numberPage+1)*1000 == clientFilteredAdapterData.size() ) {
//                        View view = ClientSMSSendActivity.this.getLayoutInflater().inflate(R.layout.progress_item, mRecyclerView, false);
//                        ProgressViewHolder holder = new ProgressViewHolder(view);
//                        adapter.addFooterView(VIEW_FOOTER, holder);
//                        numberPage++;
//                        loadData(numberPage,true);
//                    }
//                }
            }
        });
    }

    private void loadData(int numberPage, boolean lazyLoading) {
        if (!lazyLoading) {
            noData = false;
            recordsCount.setVisibility(View.GONE);
        }
        this.lazyLoading = lazyLoading;
        this.loading = true;
        this.numberPage = numberPage;
        filter.setCarwash(washer.getId());
        filter.setLimit("1000");
        filter.setOffset("0");
        if(newsTarget.equals(CLIENT_ALL_SMS))
           filter.setClientType("all_have_mobile");
        else if(newsTarget.equals(Constants.CLIENT_DRIVERSPA_PUSH)){
            filter.setClientType("registered");
        }
        else{
            filter.setClientType("not_registered");
        }
        BA.getEventBus().post(new AdminClientsRequestEvent(filter));
    }

    @Subscribe
    public void onAdminClientsReceived(AdminClientsResponseEvent event) {
        setWaitScreen(false);
        loading = false;

        if(washer!=null && listAdapter!=null) listAdapter.setWasher(washer);
        noRecords.setVisibility(View.GONE);
        listAdapter.removeFooterView(VIEW_FOOTER);

        if(event.getResult() != null){
            if(BaseAssist.isSuccess(event.getResult())){
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

                LocalBookingList localList = new LocalBookingList();
                List<BookInfo> localBooksList = new ArrayList<BookInfo>();

                if(!(event.getResult().getResponse().getClients() != null && event.getResult().getResponse().getClients().size() > 0)){
                    noData = true;
                    if(!lazyLoading) {
                        clientInfoAdapterData.clear();
                        listAdapter.notifyDataSetChanged();
                        noRecords.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    if(!lazyLoading){
                        if(event.getResult().getResponse().getMeta() != null) {
                            recordsCount.setVisibility(View.VISIBLE);
                            recordsCount.setText(event.getResult().getResponse().getMeta().getTotalCount()+BA.str(R.string.clients_count));
                        }
                        clientInfoAdapterData.clear();
                        clientInfoAdapterData.addAll(event.getResult().getResponse().getClients());
                        clientFilteredAdapterData.clear();
                        clientFilteredAdapterData.addAll(event.getResult().getResponse().getClients());
                        listAdapter.notifyDataSetChanged();

                    }
                    else{ //Lazy loading data insert
                        clientInfoAdapterData.addAll(event.getResult().getResponse().getClients());
                        clientFilteredAdapterData.addAll(event.getResult().getResponse().getClients());
                        listAdapter.notifyDataSetChanged();
                    }

                }
            }
            else{
                if(!lazyLoading) {
                    noInternetLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    if(clientInfoAdapterData != null && clientInfoAdapterData.size() > 0) {
                        noRecords.setVisibility(View.GONE);
                    }
                    else
                        noRecords.setVisibility(View.VISIBLE);
                    ToastUtil.display(this, event.getResult().getMessage());
                }
                else{
                    noInternetLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    if(clientInfoAdapterData != null && clientInfoAdapterData.size() > 0){
                        ToastUtil.display(this, event.getResult().getMessage());
                    }
                }
            }
        }
        else{
            if(!lazyLoading) {
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                noRecords.setVisibility(View.GONE);
                if(clientInfoAdapterData != null && clientInfoAdapterData.size() > 0) {
                    noRecords.setVisibility(View.GONE);
                }
                else
                    noRecords.setVisibility(View.VISIBLE);

            }
            else{
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
            ToastUtil.display(this, BA.str(R.string.err_get_data));
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
    }

    public void search(String searchText){
        if(filter != null && washer != null){
            filter();
        }
    }
}
