package com.driverspa.admin.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.driverspa.assist.BaseAssist;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.GroupServicePriceAdapter;
import com.driverspa.admin.activity.AdminAddGroupServicePriceActivity;
import com.driverspa.admin.activity.AdminChangeGroupServicePriceActivity;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.ws.AdminGroupMenuListResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminGroupServicePriceFragment extends AdminBaseFragment {

    public interface ActivityActions {
    }

    private ActivityActions activityActions;

    @BindView(R.id.expandable_list_view)
    ExpandableListView listView;
    @BindView(R.id.noRecords)
    View noRecords;

    Washer washer;
    GroupServicePriceAdapter adapter;
    List<Washer.GroupMenu> groupMenuList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_group_service_price, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        adapter = new GroupServicePriceAdapter(getActivity());
        groupMenuList = new ArrayList<Washer.GroupMenu>();
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                parent.smoothScrollToPosition(groupPosition);
                if (groupMenuList.get(groupPosition).getPrices() != null && groupMenuList.get(groupPosition).getPrices().size() > 0) {
                    if (parent.isGroupExpanded(groupPosition)) {
                        parent.collapseGroup(groupPosition);
                    } else {
                        parent.expandGroup(groupPosition);
                    }
                } else {
                    Intent intent = new Intent(getActivity(), AdminChangeGroupServicePriceActivity.class);
                    intent.putExtra(Constants.GROUP, groupMenuList.get(groupPosition).getId());
                    startActivity(intent);
                    overridePendingTransitionWithCommoOpeningTransition(getActivity());
                }

                return true;
            }
        });
    }

    @Subscribe
    public void onAdminGroupMenuListReceived(AdminGroupMenuListResponseEvent event) {
        setWaitScreen(false);
        if (event.getData() != null) {
            if (BaseAssist.isSuccess(event.getData())) {
                if(event.getData().getResponse() != null && event.getData().getResponse().getWasher() != null
                        && event.getData().getResponse().getWasher().getGroupMenu() != null
                        &&event.getData().getResponse().getWasher().getGroupMenu().size() > 0) {
                    noRecords.setVisibility(View.GONE);
//                    groupMenuList = event.getData().getResponse().getWasher().getGroupMenu();
                    groupMenuList = new ArrayList<Washer.GroupMenu>();
                    for(Washer.GroupMenu menu : event.getData().getResponse().getWasher().getGroupMenu()){
                        if((menu.getServices() != null && menu.getServices().size() > 0)){ //Group menu with sub services are group services
                            groupMenuList.add(menu);
                        }
                    }
                    // Configured (priced) groups first, original order preserved.
                    groupMenuList.sort((a, b) -> {
                        boolean aConfigured = a.getPrices() != null && !a.getPrices().isEmpty();
                        boolean bConfigured = b.getPrices() != null && !b.getPrices().isEmpty();
                        if (aConfigured == bConfigured) return 0;
                        return aConfigured ? -1 : 1;
                    });
//                    washer.setGroupMenu(groupMenuList);
                    adapter.setData(groupMenuList);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                else{
                    noRecords.setVisibility(View.VISIBLE);
                }
            } else {
                ToastUtil.display(getActivity(), event.getData().getMessage());
            }
        } else {
            addEmptyTextView(listView, BA.str(R.string.no_complex_services2));
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {
//            BA.getEventBus().post(new AdminGroupMenuListRequestEvent(washer.getId()));
//            setWaitScreen(true);
        }
    }

    @OnClick(R.id.btnAdd)
    public void onNewGroupAddButtonClicked() {
        Intent intent = new Intent(getActivity(), AdminAddGroupServicePriceActivity.class);
//        intent.putExtra(Constants.SERVICE,groupMenuList.get(groupPosition).getServiceId());
        startActivity(intent);
        overridePendingTransitionWithCommoOpeningTransition(getActivity());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveData();
                break;
        }
        return true;
    }

    private void saveData() {
        overridePendingTransitionWithCommonCloseTransition();
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
        if (washer != null) {
//            BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
        }
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
}
