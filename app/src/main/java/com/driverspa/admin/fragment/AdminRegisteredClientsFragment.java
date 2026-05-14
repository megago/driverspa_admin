package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.URL_PREFIX;
import static com.driverspa.util.Constants.VIEW_FOOTER;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.driverspa.assist.BaseAssist;
import com.google.android.material.appbar.AppBarLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.ws.AdminBookRequestEvent;
import com.driverspa.util.otto.ws.AdminBookResponseEvent;
import com.driverspa.util.otto.ws.AdminClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;
import com.driverspa.view.NDSpinner;
import com.driverspa.view.ProgressViewHolder;

public class AdminRegisteredClientsFragment extends AdminBaseFragment implements View.OnClickListener {

    public interface ActivityActions {
        public void openClientInfo(ClientInfo client);
        public void addToTheBox(ClientInfo client);
        public void addNewClient();
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
    @BindView(R.id.recordsCount)
    TextView recordsCount;
    @BindView(R.id.clientTypeSpinner)
    NDSpinner clientTypeSpinner;
    int selectedItem = 0;
    String clientType;

    private boolean loading = false;
    private boolean noData = false;

    List<ClientInfo> clientInfoAdapterData;
    private boolean lazyLoading = false;
    DividerItemDecoration itemDecoration;
    AdminSearchFilter filter = new AdminSearchFilter();

    Toolbar toolbar;
    TextView titleView;
    NDSpinner spinner;
    ProgressBar actionBarProgress;
    AppBarLayout appBarLayout;
    View actionBarRefreshButton;
    View addClientButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_registered_clients, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.appbar);
        spinner = (NDSpinner) toolbar.findViewById(R.id.spinner);
        actionBarProgress = (ProgressBar) toolbar.findViewById(R.id.progress);
        actionBarRefreshButton = toolbar.findViewById(R.id.refresh);
        titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Клиенты");
//        addClientButton = getActivity().findViewById(R.id.button_add_client);
        if(fragmentVisible && actionBarRefreshButton != null)
            actionBarRefreshButton.setVisibility(View.GONE);

//        addClientButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    if(!Functions.isUserHasAccess(washer,user,context)) return;
//                    activityActions.addNewClient();
//            }
//        });
        itemDecoration = new DividerItemDecoration(getActivity());
        makeList();
        clientType = "all";
        createActionBarSpinner();
    }

    private void makeList() {
        clientInfoAdapterData = new ArrayList<ClientInfo>();
        listAdapter = new ClientsAdapter(getActivity(),clientInfoAdapterData,washer);
        listAdapter.setData(clientInfoAdapterData);
        createListLayout();
    }

    private void createActionBarSpinner(){
        if(clientTypeSpinner != null) {
            String[] valuesArray = new String[]{"Все", "Зарегистрированные", "Незарегистрированные"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.navigation_spinner_item, valuesArray);
            adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
            clientTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    selectedItem = pos;
                    switch (pos) {
                        case 0:
                            clientType = "all";
                            break;
                        case 1:
                            clientType = "registered";
                            break;
                        case 2:
                            clientType = "not_registered";
                            break;
                    }
                    selectClientType();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            clientTypeSpinner.setAdapter(adapter);

            if (clientType.equals("all")) {
                clientTypeSpinner.setSelection(0);
            } else if (clientType.equals("registered")) {
                clientTypeSpinner.setSelection(1);
            } else if (clientType.equals("not_registered")) {
                clientTypeSpinner.setSelection(2);
            }
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if(washer != null && fragmentVisible)
            loadData(0,false);
    }

    public void selectClientType(){
        if(washer != null) {
            if(fragmentVisible) {
                setWaitScreen(true);
                loadData(0, false);
            }
        }
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
                        if(fragmentVisible) {
                            loadData(numberPage, true);
                        }
                    }
                }
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
//        filter.setLimit("40");
        filter.setOffset(Integer.toString(numberPage * 20));
        filter.setClientType(clientType);
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
                            recordsCount.setText(event.getResult().getResponse().getMeta().getTotalCount()+" клиент(ов)");
                        }
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
                else
                    noRecords.setVisibility(View.VISIBLE);

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
        context = getActivity();
        activityActions = (ActivityActions) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
        activityActions = null;
    }

    @Subscribe
    public void onSearchSubmitted(SearchEvent event){
        if(event != null && event.getQuery() != null && fragmentVisible && filter != null && washer != null){
            filter.setSearchText(event.getQuery());
            loadData(0, false);
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        fragmentVisible = isVisibleToUser;

        if(fragmentVisible){
            if(appBarLayout != null)
                appBarLayout.setExpanded(true, true);

            if(actionBarRefreshButton != null) {
//                actionBarRefreshButton.setOnClickListener(this);
                actionBarRefreshButton.setVisibility(View.GONE);
            }
            if(actionBarProgress != null)
                actionBarProgress.setVisibility(View.GONE);
            if(spinner != null)
                spinner.setVisibility(View.GONE);
            if(titleView != null) {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText("Клиенты");
            }

            if(filter != null && washer != null && !(clientInfoAdapterData != null && clientInfoAdapterData.size() > 0)){
                loadData(0,false);
            }
//            if(addClientButton!=null)
//              addClientButton.setVisibility(View.VISIBLE);

            if(washer != null)
                loadData(0,false);

        }
        else{
//            if(addClientButton!=null)
//               addClientButton.setVisibility(View.GONE);
            BA.getEventBus().post(new SearchEvent(""));
        }

        if(fragmentVisible == false && filter != null && washer != null){
            filter.setSearchText("");
            if(!(clientInfoAdapterData != null && clientInfoAdapterData.size() > 0))
                loadData(0,false);
        }
    }

    boolean fragmentVisible = false;
    public void showOptions(final ClientInfo client) {
        if (client!=null) {
            //Bookings

            AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
            b.setItems(R.array.client_options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            activityActions.addToTheBox(client);
                            break;
                        case 1:
                            addToQueue(client);
                            break;
                        case 2:
                            activityActions.openClientInfo(client);
                            break;
                    }
                }
            });
            String carNo = client.getCarNo();
            if(!TextUtils.isEmpty(client.getName()) && !TextUtils.isEmpty(client.getCarNo()) && carNo.contains(client.getName())){
                carNo = carNo.replaceAll(client.getName(),"").replaceAll("\\(","").replaceAll("\\)","");
            }
            String clientName = client.getName();
            if(!TextUtils.isEmpty(client.getCarNo()))
                clientName= clientName + " "+carNo.replaceAll("\\(","").replaceAll("\\)","");
            b.setTitle("Операция по клиенту " + clientName);
//            b.show();

            activityActions.openClientInfo(client);
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

    @Subscribe
    public void onUserReceived(UserGetSelfResponseEvent event) {
        user = event.getUser();
    }
    User user;
    Activity context;

    @Override
    public void onClick(View v) {
      if(washer != null)
           loadData(0,false);
    }
}
