package com.driverspa.admin.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.squareup.otto.Subscribe;

import org.codepond.wizardroid.WizardStep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.payment.KaspiQrBlock;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.model.Washer;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.WizardNextStepVerifyEvent;
import com.driverspa.util.otto.WizardPreviousStepAllowEvent;
import com.driverspa.util.otto.WizardPreviousStepVerifyEvent;
import com.driverspa.util.otto.ws.EditWasherRequestEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

/**
 * Created by Yerzhan Tanatov on 11/04/17.
 */
public class WasherCreateStep4 extends WizardStep {

    Washer washer;
    @BindView(R.id.comment)
    EditText comment;

    @BindView(R.id.time)
    TextView monday;

    @BindView(R.id.weekendTime)
    TextView saturday;

    ArrayList<Washer.Day> mon;
    ArrayList<Washer.Day> tue;
    ArrayList<Washer.Day> wed;
    ArrayList<Washer.Day> thu;
    ArrayList<Washer.Day> fri;
    ArrayList<Washer.Day> sat;
    ArrayList<Washer.Day> sun;

    int mondayFromHour;
    int mondayFromMinute;
    int mondayToHour;
    int mondayToMinute;

    int tuesdayFromHour;
    int tuesdayFromMinute;
    int tuesdayToHour;
    int tuesdayToMinute;

    int wednesdayFromHour;
    int wednesdayFromMinute;
    int wednesdayToHour;
    int wednesdayToMinute;

    int thursdayFromHour;
    int thursdayFromMinute;
    int thursdayToHour;
    int thursdayToMinute;

    int fridayFromHour;
    int fridayFromMinute;
    int fridayToHour;
    int fridayToMinute;

    int saturdayFromHour;
    int saturdayFromMinute;
    int saturdayToHour;
    int saturdayToMinute;

    int sundayFromHour;
    int sundayFromMinute;
    int sundayToHour;
    int sundayToMinute;

    MultipleSelectDialog paymentDialog;
    MultipleSelectDialog addInfoDialog;
    KaspiQrBlock kaspiQrBlock;

    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.web)
    EditText web;

    @BindView(R.id.txtPayType)
    TextView payType;
    @BindView(R.id.txtAddInfo)
    TextView addInfo;
    ArrayList<Washer.Contact> contacts;

    TextView titleView;
    Toolbar mToolbar;


    //Wire the layout to the step
    public WasherCreateStep4() {
    }

    private HashMap<String, String> toDialogItem(String id, String name) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put(MultipleSelectDialog.ID, id);
        item.put(MultipleSelectDialog.NAME, name);
        return item;
    }

    private ArrayList<String> toOrderedSelection(HashMap<Integer, String> namesSelectedItm) {
        return IntStream.range(0, namesSelectedItm.size())
                .mapToObj(namesSelectedItm::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.washer_create_step4, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        kaspiQrBlock = new KaspiQrBlock(getActivity(), view.findViewById(R.id.kaspiQrBlock), new KaspiQrBlock.Host() {
            @Override
            public void startActivityForResult(Intent intent, int requestCode) {
                WasherCreateStep4.this.startActivityForResult(intent, requestCode);
            }

            @Override
            public void onPaymentQrChanged() {
                // Keeps the entered hours and contacts and redraws the payment options.
                saveData();
            }
        });
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        ((TextView)(mToolbar.findViewById(R.id.action_done))).setText(BA.str(R.string.logout));
    }

//    @Override
//    public void onExit(int exitCode) {
//        switch (exitCode) {
//            case WizardStep.EXIT_NEXT:
//                saveData();
//                break;
//            case WizardStep.EXIT_PREVIOUS:
//                //Do nothing...
//                break;
//        }
//    }
//

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (kaspiQrBlock != null) kaspiQrBlock.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.addinfo)
    public void onAddInfoClicked(){
        addInfoDialog.show();
    }

    @OnClick(R.id.payType)
    public void onPayTypeClicked(){
        paymentDialog.show();
    }


    private void saveData(){
        contacts = new ArrayList<Washer.Contact>();
        Washer.Contact phoneContact = new Washer.Contact();
        phoneContact.setType("phone");
        phoneContact.setValue(phone.getText().toString());
        contacts.add(phoneContact);
        Washer.Contact webContact = new Washer.Contact();
        webContact.setType("website");
        webContact.setValue(web.getText().toString());
        contacts.add(webContact);
        Washer.Contact emailContact = new Washer.Contact();
        emailContact.setType("email");
        emailContact.setValue(email.getText().toString());
        contacts.add(emailContact);
        washer.setContacts(contacts);

        mon = new ArrayList<Washer.Day>();
        mon.add(new Washer.Day());
        tue = new ArrayList<Washer.Day>();
        tue.add(new Washer.Day());
        wed = new ArrayList<Washer.Day>();
        wed.add(new Washer.Day());
        thu = new ArrayList<Washer.Day>();
        thu.add(new Washer.Day());
        fri = new ArrayList<Washer.Day>();
        fri.add(new Washer.Day());
        sat = new ArrayList<Washer.Day>();
        sat.add(new Washer.Day());
        sun = new ArrayList<Washer.Day>();
        sun.add(new Washer.Day());

        mon.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
        tue.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
        wed.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
        thu.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
        fri.get(0).setFrom(mondayFromHour*60+mondayFromMinute);
        sat.get(0).setFrom(saturdayFromHour*60+saturdayFromMinute);
        sun.get(0).setFrom(saturdayFromHour*60+saturdayFromMinute);

        mon.get(0).setTo(mondayToHour*60+mondayToMinute);
        tue.get(0).setTo(mondayToHour*60+mondayToMinute);
        wed.get(0).setTo(mondayToHour*60+mondayToMinute);
        thu.get(0).setTo(mondayToHour*60+mondayToMinute);
        fri.get(0).setTo(mondayToHour*60+mondayToMinute);
        sat.get(0).setTo(saturdayToHour*60+saturdayToMinute);
        sun.get(0).setTo(saturdayToHour*60+saturdayToMinute);

        washer.getSchedule().setMonday(mon);
        washer.getSchedule().setTuesday(tue);
        washer.getSchedule().setWednesday(wed);
        washer.getSchedule().setThursday(thu);
        washer.getSchedule().setFriday(fri);
        washer.getSchedule().setSaturday(sat);
        washer.getSchedule().setSunday(sun);
        washer.getSchedule().setComment(comment.getText().toString());

        if(mondayFromHour*60+mondayFromMinute >= mondayToHour*60+mondayToMinute){
            ToastUtil.display(getActivity(),BA.str(R.string.err_start_after_end));
            mondayToHour = 23;
            mondayToMinute = 59;
            return;
        }

        if(saturdayFromHour*60+saturdayFromMinute >= saturdayToHour*60+saturdayToMinute){
            ToastUtil.display(getActivity(),BA.str(R.string.err_start_after_end));
            saturdayToHour = 23;
            saturdayToMinute = 59;
            return;
        }

        BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
    }

    private void showTimePicker(final TextView targetText) {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogWithNoActionBar);
        dialog.setContentView(R.layout.dialog_time_picker);
        dialog.setTitle(BA.str(R.string.select_interval));
        final TimePicker timeFrom = (TimePicker) dialog.findViewById(R.id.time_from);
        timeFrom.setIs24HourView(true);

        timeFrom.setCurrentHour(mondayFromHour);
        timeFrom.setCurrentMinute(mondayFromMinute);
        final TimePicker timeTo = (TimePicker) dialog.findViewById(R.id.time_to);
        timeTo.setIs24HourView(true);
        timeTo.setCurrentHour(mondayToHour);
        timeTo.setCurrentMinute(mondayToMinute);

        switch (targetText.getId()) {
            case R.id.time:
                timeFrom.setCurrentHour(mondayFromHour);
                timeFrom.setCurrentMinute(mondayFromMinute);
                timeTo.setCurrentHour(mondayToHour);
                timeTo.setCurrentMinute(mondayToMinute);
                break;
            case R.id.weekendTime:
                timeFrom.setCurrentHour(saturdayFromHour);
                timeFrom.setCurrentMinute(saturdayFromMinute);
                timeTo.setCurrentHour(saturdayToHour);
                timeTo.setCurrentMinute(saturdayToMinute);
                break;
        }

        final Button button = (Button) dialog.findViewById(R.id.ok);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (targetText.getId()) {
                    case R.id.time:
                        mondayFromHour = timeFrom.getCurrentHour();
                        mondayFromMinute = timeFrom.getCurrentMinute();
                        mondayToHour = timeTo.getCurrentHour();
                        mondayToMinute = timeTo.getCurrentMinute();

                        if(mondayToHour == 0 && mondayToMinute == 0){
                            mondayToHour = 23;
                            mondayToMinute = 59;

                        }
                        if(mondayFromHour*60+mondayFromMinute >= mondayToHour*60+mondayToMinute){
                            ToastUtil.display(getActivity(),BA.str(R.string.err_start_after_end));
                            mondayToHour = 23;
                            mondayToMinute = 59;
                        }

                        targetText.setText(BA.str(R.string.work_hours_weekday_pre1)+ Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
                        break;

                    case R.id.weekendTime:
                        saturdayFromHour = timeFrom.getCurrentHour();
                        saturdayFromMinute = timeFrom.getCurrentMinute();
                        saturdayToHour = timeTo.getCurrentHour();
                        saturdayToMinute = timeTo.getCurrentMinute();

                        if(saturdayToHour == 0 && saturdayToMinute == 0){
                            saturdayToHour = 23;
                            saturdayToMinute = 59;
                        }

                        if(saturdayFromHour*60+saturdayFromMinute >= saturdayToHour*60+saturdayToMinute){
                            ToastUtil.display(getActivity(),BA.str(R.string.err_start_after_end));
                            saturdayToHour = 23;
                            saturdayToMinute = 59;
                        }
                        targetText.setText(BA.str(R.string.work_hours_weekend_pre)+Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));
                        break;
                }

                dialog.dismiss();
                isVisible = false;
            }
        });
        if(!isVisible){
            dialog.show();
            isVisible = true;
        }
    }

    @OnClick(R.id.time)
    public void onMondayClicked(){
        showTimePicker(monday);
    }

    @OnClick(R.id.weekendTime)
    public void onWeekEndClicked(){
        showTimePicker(saturday);
    }

    @OnFocusChange(R.id.time)
    public void onMondayFocusChange(View view, boolean hasFocus){
        if(hasFocus){
            showTimePicker(monday);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BA.getEventBus().register(this);
        if (kaspiQrBlock != null) kaspiQrBlock.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
        if (kaspiQrBlock != null) kaspiQrBlock.onPause();
        if(washer != null) {
            saveData();
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {
            if (kaspiQrBlock != null) kaspiQrBlock.bind(washer);

            if(washer.getSchedule() != null){
                Washer.Schedule schedule = washer.getSchedule();
                comment.setText(schedule.getComment()!=null?schedule.getComment():"");

                mon = schedule.getMonday();
                if(mon != null){
                    mondayFromHour = mon.get(0).getFrom()/60;
                    mondayFromMinute = mon.get(0).getFrom()%60;
                    mondayToHour = mon.get(0).getTo()/60;
                    mondayToMinute = mon.get(0).getTo()%60;
                }
                else{
                    mon = new ArrayList<Washer.Day>();
                    mon.add(new Washer.Day());
                }


                tue = schedule.getTuesday();
                if(tue != null){
                    tuesdayFromHour = tue.get(0).getFrom()/60;
                    tuesdayFromMinute = tue.get(0).getFrom()%60;
                    tuesdayToHour = tue.get(0).getTo()/60;
                    tuesdayToMinute = tue.get(0).getTo()%60;
                }
                else{
                    tue = new ArrayList<Washer.Day>();
                    tue.add(new Washer.Day());
                }

                wed = schedule.getWednesday();
                if(wed != null){
                    wednesdayFromHour = wed.get(0).getFrom()/60;
                    wednesdayFromMinute = wed.get(0).getFrom()%60;
                    wednesdayToHour = wed.get(0).getTo()/60;
                    wednesdayToMinute = wed.get(0).getTo()%60;
                }
                else{
                    wed = new ArrayList<Washer.Day>();
                    wed.add(new Washer.Day());
                }

                thu = schedule.getThursday();
                if(thu != null){
                    thursdayFromHour = thu.get(0).getFrom()/60;
                    thursdayFromMinute = thu.get(0).getFrom()%60;
                    thursdayToHour = thu.get(0).getTo()/60;
                    thursdayToMinute = thu.get(0).getTo()%60;
                }
                else{
                    thu = new ArrayList<Washer.Day>();
                    thu.add(new Washer.Day());
                }

                fri = schedule.getFriday();
                if(fri != null){
                    fridayFromHour = fri.get(0).getFrom()/60;
                    fridayFromMinute = fri.get(0).getFrom()%60;
                    fridayToHour = fri.get(0).getTo()/60;
                    fridayToMinute = fri.get(0).getTo()%60;
                }
                else{
                    fri = new ArrayList<Washer.Day>();
                    fri.add(new Washer.Day());
                }

                sat = schedule.getSaturday();
                if(sat != null){
                    saturdayFromHour = sat.get(0).getFrom()/60;
                    saturdayFromMinute = sat.get(0).getFrom()%60;
                    saturdayToHour = sat.get(0).getTo()/60;
                    saturdayToMinute = sat.get(0).getTo()%60;
                }
                else{
                    sat = new ArrayList<Washer.Day>();
                    sat.add(new Washer.Day());
                }
                sun = schedule.getSunday();
                if(sun != null){
                    sundayFromHour = sun.get(0).getFrom()/60;
                    sundayFromMinute = sun.get(0).getFrom()%60;
                    sundayToHour = sun.get(0).getTo()/60;
                    sundayToMinute = sun.get(0).getTo()%60;
                }
                else{
                    sun = new ArrayList<Washer.Day>();
                    sun.add(new Washer.Day());
                }

                final HashMap<String,String> allPayTypes = BA.getReference().getPayoptions();
                final HashMap<String,String> allAddInfo = BA.getReference().getAdditionalInfo();

                monday.setText(BA.str(R.string.work_hours_weekday_pre2)+Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
                saturday.setText(BA.str(R.string.work_hours_weekend_pre)+Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));

                if(washer.getPayOptions() != null && washer.getPayOptions().size() > 0){
                    if(washer.getPayOptions().size() == 1)  payType.setText(allPayTypes.get(washer.getPayOptions().get(0).toString())); //payType.setText(washer.getPayOptions().get(0).toString());
                    else payType.setText(BA.str(R.string.selected_paren)+washer.getPayOptions().size()+BA.str(R.string.paren_payment_type));
                }

                if(washer.getAdditionalInfo() != null && washer.getAdditionalInfo().size() > 0){
                    if(washer.getAdditionalInfo().size() == 1) addInfo.setText(allAddInfo.get(washer.getAdditionalInfo().get(0)));// addInfo.setText(washer.getAdditionalInfo().get(0));
                    else addInfo.setText(BA.str(R.string.selected_paren)+washer.getAdditionalInfo().size()+BA.str(R.string.paren_additional_info));
                }

                contacts = washer.getContacts();
                if(contacts != null && contacts.size() > 0){
          contacts.stream().filter(contact -> "phone".equals(contact.getType())).findFirst().ifPresent(contact -> phone.setText(contact.getValue()));
          contacts.stream().filter(contact -> "website".equals(contact.getType())).findFirst().ifPresent(contact -> web.setText(contact.getValue()));
          contacts.stream().filter(contact -> "email".equals(contact.getType())).findFirst().ifPresent(contact -> email.setText(contact.getValue()));
                }
                else{
                    contacts = new ArrayList<Washer.Contact>();
                    Washer.Contact phoneContact = new Washer.Contact();
                    phoneContact.setType("phone");
                    phoneContact.setValue("");
                    contacts.add(phoneContact);
                    Washer.Contact webContact = new Washer.Contact();
                    webContact.setType("website");
                    webContact.setValue("");
                    contacts.add(webContact);
                    Washer.Contact emailContact = new Washer.Contact();
                    emailContact.setType("email");
                    emailContact.setValue("");
                    contacts.add(emailContact);
                }

                //Pay Options

                List<HashMap<String,String>> listPayTypes = new ArrayList<HashMap<String,String>>();
                List<HashMap<String,String>> selectedPayTypes = new ArrayList<HashMap<String,String>>();

        listPayTypes = allPayTypes.entrySet().stream()
            .map(entry -> toDialogItem(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

                if(washer.getPayOptions() != null){
          selectedPayTypes = washer.getPayOptions().stream()
              .map(option -> toDialogItem(option, allPayTypes.get(option)))
              .collect(Collectors.toList());
                }

                final ArrayList<String> requestPayTypes = new ArrayList<String>();
                paymentDialog = new MultipleSelectDialog(getActivity(), listPayTypes, BA.str(R.string.select_payment_type), selectedPayTypes) {
                    @Override
                    public void onDismiss(HashMap<Integer, String> namesSelectedItm) {
						requestPayTypes.clear();
						requestPayTypes.addAll(toOrderedSelection(namesSelectedItm));

                        if(namesSelectedItm.size() == 0){
                            payType.setText(BA.str(R.string.payment_type));
                        }
                        else if(namesSelectedItm.size() == 1){
                            payType.setText(allPayTypes.get(namesSelectedItm.get(0).toString()));
                        }
                        else{
                            payType.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_payment_type));
                        }
                        washer.setPayOptions(new ArrayList<String>(requestPayTypes));
                    }
                };

                //Additional Information

                List<HashMap<String,String>> listAddInfos = new ArrayList<HashMap<String,String>>();
                List<HashMap<String,String>> selectedAddInfos = new ArrayList<HashMap<String,String>>();

        listAddInfos = allAddInfo.entrySet().stream()
            .map(entry -> toDialogItem(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

                if(washer.getAdditionalInfo() != null){
          selectedAddInfos = washer.getAdditionalInfo().stream()
              .map(info -> toDialogItem(info, allAddInfo.get(info)))
              .collect(Collectors.toList());
                }

                final ArrayList<String> requestAddInfoTypes = new ArrayList<String>();
                addInfoDialog = new MultipleSelectDialog(getActivity(), listAddInfos, BA.str(R.string.select_additional_info), selectedAddInfos) {
                    @Override
                    public void onDismiss(HashMap<Integer, String> namesSelectedItm) {
						requestAddInfoTypes.clear();
						requestAddInfoTypes.addAll(toOrderedSelection(namesSelectedItm));

                        if(namesSelectedItm.size() == 0){
                            addInfo.setText(BA.str(R.string.additional_info));
                        }
                        else if(namesSelectedItm.size() == 1){
                            addInfo.setText(allAddInfo.get(namesSelectedItm.get(0)));
                        }
                        else{
                            addInfo.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_additional_info));
                        }
                        washer.setAdditionalInfo(new ArrayList<String>(requestAddInfoTypes));
                    }
                };

            }
        }
    }

    boolean isVisible = false;
    boolean isFragmentVisible = false;
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isFragmentVisible = isVisibleToUser;
        if(isVisibleToUser){
            if(titleView!=null) {
                titleView.setText(BA.str(R.string.additional_info));
                mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveData();
                        BA.getEventBus().post(new EditWasherRequestEvent(washer.getId(), washer));
                        BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
                        if(getActivity()!=null) getActivity().finish();
                    }
                });
            }
        }
    }

    @Subscribe
    public void onNextStepVerifyRequested(WizardNextStepVerifyEvent event){
        //Go to next step
        if(event.getPosition() == 3) {
            saveData();
            BA.getEventBus().post(new EditWasherRequestEvent(washer.getId(), washer));
            getActivity().finish();
        }
    }

    @Subscribe
    public void onPreviousStepVerifyRequested(WizardPreviousStepVerifyEvent event){
        BA.getEventBus().post(new WizardPreviousStepAllowEvent());
    }
}
