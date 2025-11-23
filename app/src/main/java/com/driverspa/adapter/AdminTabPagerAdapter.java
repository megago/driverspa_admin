package com.driverspa.adapter;

import androidx.fragment.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.driverspa.admin.fragment.AdminBooksFragment;
import com.driverspa.admin.fragment.AdminFareRequestFragment;
import com.driverspa.admin.fragment.AdminTimeTableFragment;

/**
 * Created by Yerzhan Tanatov on 19/10/15.
 */

public class AdminTabPagerAdapter extends FragmentStatePagerAdapter {

    public AdminTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
//            case 0:
//                //Express book
//                return  new AdminExpressWashFragment();
            case 0:
                //Books Fragment
                return  new AdminBooksFragment();
            case 1:
                //Fragment available washers
                return new AdminTimeTableFragment();
            case 2:
                //Fragment promo washers
                return new AdminFareRequestFragment();
//            case 3:
//                //Fragment favourite washers
//                return new AdminStatisticsFragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 3; //No of Tabs
    }
}
