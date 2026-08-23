package com.driverspa.admin.fragment;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.util.otto.SearchEvent;

public class AdminClientsFragment extends AdminBaseFragment {

    public interface ActivityActions {
//        public void hideShowFilterButton(int tab);
    }

    @BindView(R.id.pager)
    ViewPager viewPager;
    PagerAdapter adapter;

    int selectedTab = 0;
    @BindView(R.id.registered)
    View registered;
    @BindView(R.id.notRegistered)
    View notRegistered;
    Toolbar toolbar;
    TextView titleView;
    Spinner spinner;
    ProgressBar actionBarProgress;
    AppBarLayout appBarLayout;
    View actionBarRefreshButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_clients, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.appbar);
        spinner = (Spinner) toolbar.findViewById(R.id.spinner);
        actionBarProgress = (ProgressBar) toolbar.findViewById(R.id.progress);
        actionBarRefreshButton = toolbar.findViewById(R.id.refresh);
        titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
        titleView.setText(BA.str(R.string.menu_clients));

        if(fragmentVisible && actionBarRefreshButton != null)
            actionBarRefreshButton.setVisibility(View.GONE);

        registered.setSelected(true);
        adapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                setTabButtons(position);
            }
        });

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(selectedTab);
    }


    @OnClick(R.id.registered)
    public void onRegisteredButtonClick(){
        viewPager.setCurrentItem(0);
    }

    @OnClick(R.id.notRegistered)
    public void onNotRegisteredButtonClick(){
        viewPager.setCurrentItem(1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(fragmentVisible && actionBarRefreshButton != null) {
            actionBarRefreshButton.setVisibility(View.GONE);
        }
        BA.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
    }

    private void setTabButtons(int pos){
        actionBarRefreshButton.setVisibility(View.GONE);
        switch (pos){
            case 0:
                registered.setSelected(true);
                notRegistered.setSelected(false);
                break;
            case 1:
                registered.setSelected(false);
                notRegistered.setSelected(true);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    //Books Fragment
                    return new AdminRegisteredClientsFragment();
//                case 1:
//                    //Notification fragment
//                    return new AdminNotRegisteredClientsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 1; //No of Tabs
        }
    }

    private ActivityActions activityActions;

    boolean fragmentVisible;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        fragmentVisible = isVisibleToUser;
        if(fragmentVisible){
            if(appBarLayout != null)
                appBarLayout.setExpanded(true, true);
            if(actionBarRefreshButton != null)
              actionBarRefreshButton.setVisibility(View.GONE);
            if(actionBarProgress != null)
              actionBarProgress.setVisibility(View.GONE);
            if(spinner != null)
              spinner.setVisibility(View.GONE);
            if(titleView != null) {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText(BA.str(R.string.menu_clients));
            }
        }
        else{
            BA.getEventBus().post(new SearchEvent(""));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityActions = (ActivityActions) activity;
//        if(fragmentVisible)
//            activityActions.hideShowFilterButton(1);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activityActions = null;
    }

}



