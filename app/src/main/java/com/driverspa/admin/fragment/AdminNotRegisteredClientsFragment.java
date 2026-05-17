package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.URL_PREFIX;
import static com.driverspa.util.Constants.VIEW_FOOTER;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
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
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.ws.AdminBookRequestEvent;
import com.driverspa.util.otto.ws.AdminBookResponseEvent;
import com.driverspa.util.otto.ws.AdminNotRegisteredClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminNotRegisteredClientsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.ProgressViewHolder;

public class AdminNotRegisteredClientsFragment extends AdminBaseFragment {

    public interface ActivityActions {
        public void addToTheBox(ClientInfo client);
    }

    private ActivityActions activityActions;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_not_registered_clients, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        itemDecoration = new DividerItemDecoration(getActivity());
        makeList();
    }

    private void makeList() {
        clientInfoAdapterData = new ArrayList<ClientInfo>();
        listAdapter = new ClientsAdapter(getActivity(),clientInfoAdapterData,washer);
        listAdapter.setData(clientInfoAdapterData);
        createListLayout();
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        loadData(0,false);
    }

    private void createListLayout(){
        ((ClientsAdapter) listAdapter).setOnItemClickListener(new ClientsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final ClientInfo client = (ClientInfo)((clientInfoAdapterData.get(position)));
                showOptions(client);
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
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
                        View view = getActivity().getLayoutInflater().inflate(R.layout.progress_item, mRecyclerView, false);
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
//        filter.setLimit("10");
        filter.setOffset(Integer.toString(numberPage * 20));
        filter.setCarwash(washer.getId());
        filter.setClientType("not_registered");

        BA.getEventBus().post(new AdminNotRegisteredClientsRequestEvent(filter));
    }

    @Subscribe
    public void onAdminClientsReceived(AdminNotRegisteredClientsResponseEvent event) {
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
                    else {
                        noRecords.setVisibility(View.VISIBLE);
                    }
                    ToastUtil.display(getActivity(), event.getResult().getMessage());
                }
                else{
                    noInternetLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    if(clientInfoAdapterData != null && clientInfoAdapterData.size() > 0){
                        ToastUtil.display(getActivity(), event.getResult().getMessage());
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
                else {
                    noRecords.setVisibility(View.VISIBLE);
                }

            }
            else{
                noInternetLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
            ToastUtil.display(getActivity(), "Ошибка при получении данных");
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


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityActions = (ActivityActions) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityActions = null;
    }

    @Subscribe
    public void onSearchSubmitted(SearchEvent event){
        if(event != null && event.getQuery() != null && fragmentVisible){
            filter.setSearchText(event.getQuery());
            loadData(0,false);
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        fragmentVisible = isVisibleToUser;
        if(fragmentVisible == false && !TextUtils.isEmpty(filter.getSearchText())){
            filter.setSearchText("");
            loadData(0,false);
        }
    }
    boolean fragmentVisible = false;


    public void showOptions(final ClientInfo client) {
        if (client!=null) {
            //Bookings

            AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
            b.setItems(R.array.client_not_registered_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            activityActions.addToTheBox(client);
                            break;
                        case 1:
                            addToQueue(client);
                            break;
                    }
                }
            });
            String carNo = client.getCarNo();
            if(!TextUtils.isEmpty(client.getName()) && !TextUtils.isEmpty(client.getCarNo()) && carNo.contains(client.getName())){
                carNo = carNo.replaceAll(client.getName(),"").replaceAll("\\(","").replaceAll("\\)","");
            }
            String clientName = "";
            if(!TextUtils.isEmpty(client.getCarNo()))
                clientName= clientName + " "+carNo.replaceAll("\\(","").replaceAll("\\)","");
            b.setTitle("Операция по клиенту " + clientName);
            b.show();
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
            request.setClientKey(client.getCarNo());
        else
            request.setClientKey(client.getCarNo()+" ("+client.getName()+")");

        request.setTime(new Date());
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
                ToastUtil.display(getActivity(), "Клиент добавлен в очередь");
            }
            else
                ToastUtil.display(getActivity(), event.getResult().getMessage());
        }
        else{
            ToastUtil.display(getActivity(), "Ошибка при добавлении очереди");
        }
    }

}
