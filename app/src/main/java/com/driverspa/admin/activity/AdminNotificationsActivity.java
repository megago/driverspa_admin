package com.driverspa.admin.activity;
import com.driverspa.BA;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.splunk.mint.Mint;

import com.driverspa.R;
import com.driverspa.activity.BaseActivity;
import com.driverspa.admin.fragment.AdminNotificationsFragment;
import com.driverspa.util.UserPreferences;

public class AdminNotificationsActivity extends BaseActivity implements AdminNotificationsFragment.ActivityActions{
    public static FragmentManager fragmentManager;
    AdminNotificationsFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

        setContentView(R.layout.activity_common);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((TextView)mToolbar.findViewById(R.id.action_bar_title)).setText(BA.str(R.string.notifications_clients));

        fragmentManager = getSupportFragmentManager();
        if( savedInstanceState == null ) {
            fragment = new AdminNotificationsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK, new Intent());
                finish();
                overridePendingTransitionWithCommonCloseTransition();
                break;
//            case R.id.menu_options:
//                if(fragment != null)
//                    fragment.showOptions();
//                break;
        }
        return true;
    }

    @Override
    public void addNews() {
        Intent intent = new Intent(this,AdminAddNotificationsActivity.class);
        startActivity(intent);
        overridePendingTransitionWithCommoOpeningTransition(this);
    }
}
