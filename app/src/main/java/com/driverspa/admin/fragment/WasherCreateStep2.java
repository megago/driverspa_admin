package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.EXTRA_BOX_ID;
import static com.driverspa.util.Constants.EXTRA_BOX_NAME;
import static com.driverspa.util.Constants.EXTRA_BOX_WASHER_PERSON;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.driverspa.adapter.AdminBoxesAdapter;
import com.squareup.otto.Subscribe;

import org.codepond.wizardroid.WizardStep;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.activity.AdminAddBoxActivity;
import com.driverspa.admin.activity.AdminChangeBoxTypeActivity;
import com.driverspa.model.Box;
import com.driverspa.model.Washer;
import com.driverspa.util.otto.WizardNextStepAllowEvent;
import com.driverspa.util.otto.WizardNextStepVerifyEvent;
import com.driverspa.util.otto.WizardPreviousStepVerifyEvent;
import com.driverspa.util.otto.ws.AdminBoxListRequestEvent;
import com.driverspa.util.otto.ws.AdminBoxListResponseEvent;
import com.driverspa.util.otto.ws.EditWasherRequestEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class WasherCreateStep2 extends WizardStep{


    @BindView(R.id.list_view)
    ListView listView;
    Washer washer;
    AdminBoxesAdapter adapter;
    TextView titleView;
    Toolbar mToolbar;

    private ProgressDialog pd = null;

    //Wire the layout to the step
    public WasherCreateStep2() {
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.washer_create_step2, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        ((TextView)(mToolbar.findViewById(R.id.action_done))).setText(BA.str(R.string.logout));
    }



    @Subscribe
    public void onNextStepVerifyRequested(WizardNextStepVerifyEvent event){
        //Go to next step
        if(event.getPosition()==1)
            BA.getEventBus().post(new WizardNextStepAllowEvent());
    }

    @Subscribe
    public void onPreviousStepVerifyRequested(WizardPreviousStepVerifyEvent event){
    }

    void setWaitScreen(boolean set) {
        if(pd == null) {
            pd = new ProgressDialog(getActivity());
            pd.setTitle("");
            pd.setIndeterminate(true);
            pd.setMessage(BA.str(R.string.loading_dots));
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

    boolean isVisible = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if(isVisibleToUser){
            if(titleView!=null) {
                titleView.setText(BA.str(R.string.boxes_settings));
                mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getActivity()!=null) getActivity().finish();
                        BA.getEventBus().post(new EditWasherRequestEvent(washer.getId(), washer));
                    }
                });
            }
        }
    }


    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {

            ArrayList<Box> boxes = new ArrayList<>();
            boxes = washer.getBoxSettings();
            adapter = new AdminBoxesAdapter(getActivity());
            adapter.set(boxes);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            BA.getEventBus().post(new AdminBoxListRequestEvent(washer.getId()));
//            setWaitScreen(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                    String boxId = adapter.getList().get(arg2).getUid();
                    String boxName = adapter.getList().get(arg2).getBoxName();
                    String washerPerson = adapter.getList().get(arg2).getWasherPerson();
                    Intent intent = new Intent(getActivity(),AdminChangeBoxTypeActivity.class);
                    intent.putExtra(EXTRA_BOX_ID, boxId);
                    intent.putExtra(EXTRA_BOX_NAME, boxName);
                    intent.putExtra(EXTRA_BOX_WASHER_PERSON, washerPerson);
                    startActivity(intent);
                }
            });
        }
    }

    @Subscribe
    public void onAdminBoxListReceived(AdminBoxListResponseEvent event){
        setWaitScreen(false);
        if(event.getData() != null){
            ArrayList<Box> boxes = event.getData().getResponse().getBoxes();
            washer.setBoxSettings(boxes);
            adapter = new AdminBoxesAdapter(getActivity());
            adapter.set(boxes);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    @OnClick(R.id.btnAddBox)
    public void onAddBoxClicked(){
        Intent intent = new Intent(getActivity(),AdminAddBoxActivity.class);
        startActivity(intent);
    }
}