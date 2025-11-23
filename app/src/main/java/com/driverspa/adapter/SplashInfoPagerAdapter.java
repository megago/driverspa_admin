package com.driverspa.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.driverspa.fragment.SplashInfoFragment;
import com.driverspa.fragment.TempMainFragment;

/**
 * Created by Yerzhan Tanatov on 19/10/15.
 */

public class SplashInfoPagerAdapter extends FragmentStatePagerAdapter {

    public SplashInfoPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return SplashInfoFragment.init(0);
            case 1:
                return SplashInfoFragment.init(1);
            case 2:
                return SplashInfoFragment.init(2);
            case 3:
                return new TempMainFragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 4; //No of Tabs
    }
}
