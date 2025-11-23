package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.FareRequestsAdapter;
import com.driverspa.model.FareRequest;
import com.driverspa.model.FareRequestList;
import com.driverspa.model.User;
import com.driverspa.model.Washer;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.FareRequestBidAcceptedEvent;
import com.driverspa.util.otto.FareRequestBidNotAcceptedEvent;
import com.driverspa.util.otto.FareRequestUpdateEvent;
import com.driverspa.util.otto.MakeBidForRequestEvent;
import com.driverspa.util.otto.ws.UserGetSelfResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DividerItemDecoration;

public class AdminFareRequestFragment extends AdminBaseFragment {

    public interface ActivityActions {
    }

    private ActivityActions activityActions;
    private FareRequestsAdapter listAdapter;
    Washer washer;
    @BindView(R.id.recycler_linear)
    RecyclerView mRecyclerLinear;
    @BindView(R.id.noRecords)
    TextView noRecords;
    List<FareRequest> fareRequestsAdapterData;
    private boolean lazyLoading = false;
    DividerItemDecoration itemDecoration;

    Toolbar toolbar;
    TextView titleView;
    ProgressBar actionBarProgress;
    AppBarLayout appBarLayout;
    View actionBarRefreshButton;
    Spinner spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fare_requests, container, false);
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
        actionBarProgress = (ProgressBar) toolbar.findViewById(R.id.progress);
        actionBarRefreshButton = toolbar.findViewById(R.id.refresh);
        spinner = (Spinner) toolbar.findViewById(R.id.spinner);
        titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Предложения от клиентов");

        if(fragmentVisible && actionBarRefreshButton != null)
            actionBarRefreshButton.setVisibility(View.GONE);

        itemDecoration = new DividerItemDecoration(getActivity());
    }

    private void makeList() {
        fareRequestsAdapterData = new ArrayList<FareRequest>();
        listAdapter = new FareRequestsAdapter(getActivity(),fareRequestsAdapterData,washer);
        listAdapter.setData(fareRequestsAdapterData);
        if(fareRequestsAdapterData.size() == 0) noRecords.setVisibility(View.VISIBLE);
        else noRecords.setVisibility(View.GONE);
        createListLayout();
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        makeList();
        updateList();
    }

    private void createListLayout(){
        ((FareRequestsAdapter) listAdapter).setOnItemClickListener(new FareRequestsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final FareRequest fareRequest = (FareRequest)((fareRequestsAdapterData.get(position)));
                BA.getEventBus().post(new MakeBidForRequestEvent(fareRequest));
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        mRecyclerLinear.setLayoutManager(manager);
        mRecyclerLinear.setAdapter(listAdapter);
        mRecyclerLinear.removeItemDecoration(itemDecoration);
        mRecyclerLinear.addItemDecoration(itemDecoration);
//        createScrollListener(mRecyclerLinear, manager, listAdapter);
    }

//    private void createScrollListener(final RecyclerView mRecyclerView, final LinearLayoutManager mLayoutManager, final FareReqeustsAdapter adapter) {
//        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int totalItem = mLayoutManager.getItemCount();
//                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
//                if (!noData && !loading && lastVisibleItem == totalItem - 1) {
//                    if (fareRequests.size() > 0 && (numberPage+1)*20 == fareRequests.size() ) {
//                        View view = getActivity().getLayoutInflater().inflate(R.layout.progress_item, mRecyclerView, false);
//                        ProgressViewHolder holder = new ProgressViewHolder(view);
//                        adapter.addFooterView(VIEW_FOOTER, holder);
//                        numberPage++;
//                        if(fragmentVisible) {
//                            loadData(numberPage, true);
//                        }
//                    }
//                }
//            }
//        });
//    }


    @Override
    public void onResume() {
        super.onResume();
        updateList();
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        fragmentVisible = isVisibleToUser;
        if(fragmentVisible) {
            updateList();
            if (appBarLayout != null)
                appBarLayout.setExpanded(true, true);
            if(actionBarRefreshButton != null) {
                actionBarRefreshButton.setVisibility(View.GONE);
            }
            if(actionBarProgress != null)
                actionBarProgress.setVisibility(View.GONE);
            if(spinner != null)
                spinner.setVisibility(View.GONE);
            if(titleView != null) {
                titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                titleView.setCompoundDrawablePadding(0);
                titleView.setOnClickListener(null);
                titleView.setVisibility(View.VISIBLE);
                titleView.setText("Предложения от клиентов");
            }
        }
    }

    @Subscribe
    public void onFareRequestsUpdateEventReceived(FareRequestUpdateEvent event) {
        updateList();
    }

    private void updateList(){
        FareRequestList fareRequestList;
        if (!TextUtils.isEmpty(UserPreferences.getActiveFareRequest(BA.getContext()))) {
            fareRequestList = FareRequestList.deserialize(UserPreferences.getActiveFareRequest(BA.getContext()));
        } else {
            fareRequestList = new FareRequestList();
            fareRequestList.setFareRequests(new ArrayList<FareRequest>());
        }
        List<FareRequest> fareRequests = fareRequestList.getFareRequests();
        if (fareRequests == null) {
            fareRequests = new ArrayList<>();
        }
        if (fareRequestList != null && fareRequests != null && fareRequests.size() > 0) {
            if(noRecords != null && listAdapter != null) {
                Iterator<FareRequest> iter = fareRequests.iterator();
                long currentTime = System.currentTimeMillis();
                while (iter.hasNext()) {
                    FareRequest fareRequest = iter.next();
                    long timeDiff = currentTime - fareRequest.getInitialDate().getTime();
                    int seconds = (int) (timeDiff / 1000) % 60;
                    long days = timeDiff / (24 * 60 * 60 * 1000);
                    if(days > 1){
                        iter.remove();
                        break;
                    }
                }
                fareRequestList.setFareRequests(fareRequests);
                UserPreferences.putActiveFareRequestBooking(BA.getContext(),fareRequestList.serialize());
                BA.getEventBus().post(new FareRequestUpdateEvent());

                noRecords.setVisibility(View.GONE);
                fareRequestsAdapterData = fareRequests;
                listAdapter.setData(fareRequestsAdapterData);
                listAdapter.notifyDataSetChanged();
            }
        }
        else{
            if(noRecords != null && listAdapter != null) {
                noRecords.setVisibility(View.VISIBLE);
                fareRequestsAdapterData = fareRequests;
                listAdapter.setData(fareRequestsAdapterData);
                listAdapter.notifyDataSetChanged();
            }
        }
    }

    @Subscribe
    public void onFareRequestsBidAcceptedEventReceived(FareRequestBidAcceptedEvent event){

    }

    @Subscribe
    public void onFareRequestsNotBidAcceptedEventReceived(FareRequestBidNotAcceptedEvent event){

    }

    boolean fragmentVisible = false;

    @Subscribe
    public void onUserReceived(UserGetSelfResponseEvent event) {
        user = event.getUser();
    }
    User user;
    Activity context;
}
