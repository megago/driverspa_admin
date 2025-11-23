package com.driverspa.admin.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.GroupServicePriceAdapter;
import com.driverspa.model.Washer;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminGroupMenuListRequestEvent;
import com.driverspa.util.otto.ws.AdminGroupMenuListResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

public class AdminGroupServicePriceActivity extends AdminBaseActivity {

    @BindView(R.id.expandable_list_view)
    ExpandableListView listView;
    Washer washer;
    GroupServicePriceAdapter adapter;
    List<Washer.GroupMenu> groupMenuList;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_group_service_price);
        ButterKnife.bind(this);
        adapter = new GroupServicePriceAdapter(this);
        groupMenuList = new ArrayList<Washer.GroupMenu>();
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Комплексные услуги");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                parent.smoothScrollToPosition(groupPosition);
                if(groupMenuList.get(groupPosition).getPrices() != null && groupMenuList.get(groupPosition).getPrices().size() > 0) {
                    if (parent.isGroupExpanded(groupPosition)) {
                        parent.collapseGroup(groupPosition);
                    } else {
                        parent.expandGroup(groupPosition);
                    }
                }
                else{
                    Intent intent = new Intent(getApplicationContext(),AdminChangeGroupServicePriceActivity.class);
                    intent.putExtra(Constants.GROUP,groupMenuList.get(groupPosition).getId());
                    startActivity(intent);
                    overridePendingTransitionWithCommoOpeningTransition(AdminGroupServicePriceActivity.this);
                }

                return true;
            }
        });

    }

    @Subscribe
    public void onAdminGroupMenuListReceived(AdminGroupMenuListResponseEvent event){
        setWaitScreen(false);
        if(event.getData() != null){
           if(BaseAssist.isSuccess(event.getData())) {
               groupMenuList = event.getData().getResponse().getWasher().getGroupMenu();
               washer.setGroupMenu(groupMenuList);
               adapter.setData(groupMenuList);
               listView.setAdapter(adapter);
               adapter.notifyDataSetChanged();
           }
            else{
               ToastUtil.display(this,event.getData().getMessage());
           }
        }
        else {
            addEmptyTextView(listView, "Нет комплексных услуг");
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event){
        washer = event.getWasher();
        if(washer != null){
            BA.getEventBus().post(new AdminGroupMenuListRequestEvent(washer.getId()));
            setWaitScreen(true);
        }
    }

    @OnClick(R.id.btnAdd)
    public void onNewGroupAddButtonClicked(){
        Intent intent = new Intent(getApplicationContext(),AdminAddGroupServicePriceActivity.class);
//        intent.putExtra(Constants.SERVICE,groupMenuList.get(groupPosition).getServiceId());
        startActivity(intent);
        overridePendingTransitionWithCommoOpeningTransition(AdminGroupServicePriceActivity.this);
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
                saveData();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        saveData();
    }

    private void saveData(){
        finish();
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
        if(washer != null) {
//            BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
        }
    }
}
