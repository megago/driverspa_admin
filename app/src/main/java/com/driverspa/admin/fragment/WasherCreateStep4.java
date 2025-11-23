package com.driverspa.admin.fragment;

import android.app.Dialog;
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

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
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
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        ((TextView)(mToolbar.findViewById(R.id.action_done))).setText("Выйти");
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
            ToastUtil.display(getActivity(),"Время начало не может быть больше или равно времени окончании");
            mondayToHour = 23;
            mondayToMinute = 59;
            return;
        }

        if(saturdayFromHour*60+saturdayFromMinute >= saturdayToHour*60+saturdayToMinute){
            ToastUtil.display(getActivity(),"Время начало не может быть больше или равно времени окончании");
            saturdayToHour = 23;
            saturdayToMinute = 59;
            return;
        }

        BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
    }

    private void showTimePicker(final TextView targetText) {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogWithNoActionBar);
        dialog.setContentView(R.layout.dialog_time_picker);
        dialog.setTitle("Выберите интервал");
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
                            ToastUtil.display(getActivity(),"Время начало не может быть больше или равно времени окончании");
                            mondayToHour = 23;
                            mondayToMinute = 59;
                        }

                        targetText.setText("Время работы в будние: "+ Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
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
                            ToastUtil.display(getActivity(),"Время начало не может быть больше или равно времени окончании");
                            saturdayToHour = 23;
                            saturdayToMinute = 59;
                        }
                        targetText.setText("Время работы в выходные:  "+Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));
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
    }

    @Override
    public void onPause() {
        super.onPause();
        BA.getEventBus().unregister(this);
        if(washer != null) {
            saveData();
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer != null) {

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

                monday.setText("Время работы в будние:  "+Functions.formatTimeInterval(mondayFromHour, mondayFromMinute, mondayToHour, mondayToMinute));
                saturday.setText("Время работы в выходные:  "+Functions.formatTimeInterval(saturdayFromHour, saturdayFromMinute, saturdayToHour, saturdayToMinute));

                if(washer.getPayOptions() != null && washer.getPayOptions().size() > 0){
                    if(washer.getPayOptions().size() == 1)  payType.setText(allPayTypes.get(washer.getPayOptions().get(0).toString())); //payType.setText(washer.getPayOptions().get(0).toString());
                    else payType.setText("Выбрано ("+washer.getPayOptions().size()+") вид оплаты");
                }

                if(washer.getAdditionalInfo() != null && washer.getAdditionalInfo().size() > 0){
                    if(washer.getAdditionalInfo().size() == 1) addInfo.setText(allAddInfo.get(washer.getAdditionalInfo().get(0)));// addInfo.setText(washer.getAdditionalInfo().get(0));
                    else addInfo.setText("Выбрано ("+washer.getAdditionalInfo().size()+") доп. информации");
                }

                contacts = washer.getContacts();
                if(contacts != null && contacts.size() > 0){
                    for(Washer.Contact contact : contacts){
                        if(contact.getType().equals("phone")){
                            phone.setText(contact.getValue());
                        }
                        else if(contact.getType().equals("website")){
                            web.setText(contact.getValue());
                        }
                        else if(contact.getType().equals("email")){
                            email.setText(contact.getValue());
                        }
                    }
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

                for (Map.Entry<String,String> entry : allPayTypes.entrySet()) {
                    HashMap<String,String> payMap = new HashMap<String,String>();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    payMap.put(MultipleSelectDialog.ID, key);
                    payMap.put(MultipleSelectDialog.NAME, value);
                    listPayTypes.add(payMap);
                }

                if(washer.getPayOptions() != null){
                    for(int i = 0; i < washer.getPayOptions().size(); i++){
                        HashMap<String,String> payMap = new HashMap<String,String>();
                        payMap.put(MultipleSelectDialog.ID, washer.getPayOptions().get(i));
                        payMap.put(MultipleSelectDialog.NAME, allPayTypes.get(washer.getPayOptions().get(i)));
                        selectedPayTypes.add(payMap);
                    }
                }

                final ArrayList<String> requestPayTypes = new ArrayList<String>();
                paymentDialog = new MultipleSelectDialog(getActivity(), listPayTypes, "Выберите тип оплаты", selectedPayTypes) {
                    @Override
                    public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

                        if(namesSelectedItm.size() == 0){
                            payType.setText("Вид оплаты");
                        }
                        else if(namesSelectedItm.size() == 1){
                            requestPayTypes.add(namesSelectedItm.get(0));
                            payType.setText(allPayTypes.get(namesSelectedItm.get(0).toString()));
                        }
                        else{
                            payType.setText("Выбрано ("+namesSelectedItm.size()+") вид оплаты");
                            for(int i = 0; i < namesSelectedItm.size(); i++){
                                requestPayTypes.add(namesSelectedItm.get(i).toString());
                            }
                        }
                        washer.setPayOptions(new ArrayList<String>(requestPayTypes));
                        requestPayTypes.clear();
                    }
                };

                //Additional Information

                List<HashMap<String,String>> listAddInfos = new ArrayList<HashMap<String,String>>();
                List<HashMap<String,String>> selectedAddInfos = new ArrayList<HashMap<String,String>>();

                for (Map.Entry<String,String> entry : allAddInfo.entrySet()) {
                    HashMap<String,String> addInfoMap = new HashMap<String,String>();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    addInfoMap.put(MultipleSelectDialog.ID, key);
                    addInfoMap.put(MultipleSelectDialog.NAME, value);
                    listAddInfos.add(addInfoMap);
                }

                if(washer.getAdditionalInfo() != null){
                    for(int i = 0; i < washer.getAdditionalInfo().size(); i++){
                        HashMap<String,String> addInfoMap = new HashMap<String,String>();
                        addInfoMap.put(MultipleSelectDialog.ID, washer.getAdditionalInfo().get(i));
                        addInfoMap.put(MultipleSelectDialog.NAME, allAddInfo.get(washer.getAdditionalInfo().get(i)));
                        selectedAddInfos.add(addInfoMap);
                    }
                }

                final ArrayList<String> requestAddInfoTypes = new ArrayList<String>();
                addInfoDialog = new MultipleSelectDialog(getActivity(), listAddInfos, "Выберите доп. Информацию", selectedAddInfos) {
                    @Override
                    public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

                        if(namesSelectedItm.size() == 0){
                            addInfo.setText("Доп. информация");
                        }
                        else if(namesSelectedItm.size() == 1){
                            requestAddInfoTypes.add(namesSelectedItm.get(0));
                            addInfo.setText(allAddInfo.get(namesSelectedItm.get(0)));
                        }
                        else{
                            addInfo.setText("Выбрано ("+namesSelectedItm.size()+") доп. информации");
                            for(int i = 0; i < namesSelectedItm.size(); i++){
                                requestAddInfoTypes.add(namesSelectedItm.get(i));
                            }
                        }
                        washer.setAdditionalInfo(new ArrayList<String>(requestAddInfoTypes));
                        requestAddInfoTypes.clear();
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
                titleView.setText("Доп. информация");
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
