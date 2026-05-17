package com.driverspa.admin.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.codepond.wizardroid.WizardStep;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.Washer;
import com.driverspa.util.otto.WizardNextStepAllowEvent;
import com.driverspa.util.otto.WizardNextStepVerifyEvent;
import com.driverspa.util.otto.WizardPreviousStepAllowEvent;
import com.driverspa.util.otto.WizardPreviousStepVerifyEvent;
import com.driverspa.util.otto.ws.EditWasherRequestEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class WasherCreateStep3 extends WizardStep {

    Washer washer;
    @BindView(R.id.pager)
    ViewPager viewPager;
    PagerAdapter adapter;

    int selectedTab = 0;

    @BindView(R.id.services)
    View services;
    @BindView(R.id.servicesOwn)
    View servicesOwn;
    @BindView(R.id.servicesGroup)
    View servicesGroup;

    private ProgressDialog pd = null;

    TextView titleView;
    Toolbar mToolbar;


    //Wire the layout to the step
    public WasherCreateStep3() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.washer_create_step3, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        services.setSelected(true);
        adapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                setTabButtons(position);
            }
        });

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(selectedTab);

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        ((TextView)(mToolbar.findViewById(R.id.action_done))).setText("Выйти");
    }

    @Subscribe
    public void onNextStepVerifyRequested(WizardNextStepVerifyEvent event){
        //Go to next step
        if(event.getPosition()==2)
            BA.getEventBus().post(new WizardNextStepAllowEvent());
    }

    @Subscribe
    public void onPreviousStepVerifyRequested(WizardPreviousStepVerifyEvent event){
            BA.getEventBus().post(new WizardPreviousStepAllowEvent());
    }

    void setWaitScreen(boolean set) {
        if(pd == null) {
            pd = new ProgressDialog(getActivity());
            pd.setTitle("");
            pd.setIndeterminate(true);
            pd.setMessage("Загрузка...");
        }

        if(set) pd.show();
        else  pd.dismiss();
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

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
    }

    @OnClick(R.id.services)
    public void onServicesButtonClick(){
        viewPager.setCurrentItem(0);
    }

    @OnClick(R.id.servicesOwn)
    public void onOwnServicesButtonClick(){
        viewPager.setCurrentItem(1);
    }

    @OnClick(R.id.servicesGroup)
    public void onGroupServicesButtonClick(){
        viewPager.setCurrentItem(2);
    }

    private void setTabButtons(int pos){
        switch (pos){
            case 0:
                services.setSelected(true);
                servicesOwn.setSelected(false);
                servicesGroup.setSelected(false);
                break;
            case 1:
                services.setSelected(false);
                servicesOwn.setSelected(true);
                servicesGroup.setSelected(false);
                break;
            case 2:
                services.setSelected(false);
                servicesOwn.setSelected(false);
                servicesGroup.setSelected(true);
                break;
        }
    }

    boolean isVisible = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if(isVisibleToUser){
            if(titleView!=null) {
                titleView.setText("Услуги");
                mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BA.getEventBus().post(new EditWasherRequestEvent(washer.getId(), washer));
                        if(getActivity()!=null) getActivity().finish();
                    }
                });
            }
        }
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
                    return  new AdminServicePriceFragment();
                case 1:
                    //Books Fragment
                    return  new AdminOwnServicePriceFragment();
//                case 2:
//                    //Fragment available washers
//                    return new AdminGroupServicePriceFragment();
            }
            return null;
        }
        @Override
        public int getCount() {
            return 2; //No of Tabs
        }
    }
}