package com.driverspa.admin.activity;
import com.driverspa.BA;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.splunk.mint.Mint;

import butterknife.ButterKnife;
import com.driverspa.R;
import com.driverspa.admin.fragment.AdminOwnServicePriceFragment;
import com.driverspa.admin.fragment.AdminServicePriceFragment;
import com.driverspa.util.UserPreferences;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class AdminNewWasherActivity extends AdminBaseActivity implements AdminServicePriceFragment.ActivityActions, AdminOwnServicePriceFragment.ActivityActions {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_new_carwash_wizard);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText(BA.str(R.string.create_wash));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.findViewById(R.id.action_done).setVisibility(View.GONE);
        mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransitionWithCommonCloseTransition();
            }
        });

        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
    }
}
