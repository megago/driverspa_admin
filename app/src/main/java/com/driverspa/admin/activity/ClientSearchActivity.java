package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.URL_PREFIX;
import static com.driverspa.util.Constants.VIEW_FOOTER;

import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.ClientsAdapter;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.LocalBookingList;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminBookRequestEvent;
import com.driverspa.util.otto.ws.AdminBookResponseEvent;
import com.driverspa.util.otto.ws.AdminClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.ProgressViewHolder;

/**
 * Created by Yerzhan Tanatov on 26/12/16.
 */
public class ClientSearchActivity extends AdminBaseActivity {

    private int numberPage = 0;
    private ClientsAdapter listAdapter;
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
    private boolean loading = false;
    private boolean noData = false;

    List<ClientInfo> clientInfoAdapterData;
    private boolean lazyLoading = false;
    DividerItemDecoration itemDecoration;
    AdminSearchFilter filter = new AdminSearchFilter();
    TextView titleView;

    EditText searchText;
    TextWatcher searchTextWatcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        setContentView(R.layout.activity_admin_search_clients);
        ButterKnife.bind(this);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        searchText = (EditText) mToolbar.findViewById(R.id.search);

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

        searchText.addTextChangedListener(searchTextWatcher);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        itemDecoration = new DividerItemDecoration(this);
        makeList();
    }


    private void makeList() {
        clientInfoAdapterData = new ArrayList<ClientInfo>();
        listAdapter = new ClientsAdapter(this,clientInfoAdapterData,washer);
        listAdapter.setData(clientInfoAdapterData);
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
        ((ClientsAdapter) listAdapter).setOnItemClickListener(new ClientsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final ClientInfo client = (ClientInfo)((clientInfoAdapterData.get(position)));
                closeKeyboard();
                Intent resultData = new Intent();
                resultData.putExtra(Constants.CLIENT_INFO,client.serialize());
                setResult(ActivityForResult.ACTIVITY_SEARCH_HAS_RESULT,resultData);
                finish();
                overridePendingTransition(0,0);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerLinear.setLayoutManager(manager);
        mRecyclerLinear.setAdapter(listAdapter);
        mRecyclerLinear.removeItemDecoration(itemDecoration);
        mRecyclerLinear.addItemDecoration(itemDecoration);
        createScrollListener(mRecyclerLinear, manager, listAdapter);
    }

    private void createScrollListener(final RecyclerView mRecyclerView, final LinearLayoutManager mLayoutManager, final ClientsAdapter adapter) {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItem = mLayoutManager.getItemCount();
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (!noData && !loading && lastVisibleItem == totalItem - 1) {
                    if (clientInfoAdapterData.size() > 0 && (numberPage+1)*20 == clientInfoAdapterData.size() ) {
                        View view = ClientSearchActivity.this.getLayoutInflater().inflate(R.layout.progress_item, mRecyclerView, false);
                        ProgressViewHolder holder = new ProgressViewHolder(view);
                        adapter.addFooterView(VIEW_FOOTER, holder);
                        numberPage++;
                        loadData(numberPage,true);
                    }
                }
            }
        });
    }

    private void loadData(int numberPage, boolean lazyLoading) {
        if (!lazyLoading) {
            noData = false;
        }
        this.lazyLoading = lazyLoading;
        this.loading = true;
        this.numberPage = numberPage;
        filter.setCarwash(washer.getId());
        filter.setOffset(Integer.toString(numberPage * 20));
        filter.setClientType("all");
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
                        clientInfoAdapterData.clear();
                        clientInfoAdapterData.addAll(event.getResult().getResponse().getClients());
                        listAdapter.notifyDataSetChanged();

                    }
                    else{ //Lazy loading data insert
                        clientInfoAdapterData.addAll(event.getResult().getResponse().getClients());
                        listAdapter.notifyDataSetChanged();
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
            filter.setSearchText(searchText);
            loadData(0,false);
        }
    }

    private void addToQueue(ClientInfo client){
        BookingRequest request = new BookingRequest();
        if(client.getCarNo().contains("("))
            request.setCarNo(client.getCarNo().substring(0,client.getCarNo().indexOf("(")));
        else
            request.setCarNo(client.getCarNo());
        request.setMobile(client.getPhone()!=null?client.getPhone():"");
        if(client.getCarNo().contains("("))
            request.setClientKey(client.getCarNo().substring(0,client.getCarNo().indexOf("(")));
        else
            request.setClientKey(client.getCarNo());

            request.setTime(Instant.now());
        request.setCarwash(URL_PREFIX+washer.getId());
        request.setOffline(true);
        request.setQueued(true);
        request.setDirectWash(false);
        BA.getEventBus().post(new AdminBookRequestEvent(request));
        setWaitScreen(true);
    }

    @Subscribe
    public void onAdminBookResponseReceived(AdminBookResponseEvent event){
        setWaitScreen(false);
        if(event.getResult() != null){
            if(BaseAssist.isSuccess(event.getResult())) {
                ToastUtil.display(this, BA.str(R.string.client_added_queue));
            }
            else
                ToastUtil.display(this, event.getResult().getMessage());
        }
        else{
            ToastUtil.display(this, BA.str(R.string.err_add_queue));
        }
    }
}
