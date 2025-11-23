package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.CLIENT_ALL_SMS;
import static com.driverspa.util.Constants.CLIENT_SMS;
import static com.driverspa.util.Constants.CLIENT_WASHME_PUSH;
import static com.driverspa.util.Constants.INFO;
import static com.driverspa.util.Constants.PENDING;
import static com.driverspa.util.Constants.URL_PREFIX;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.SmsScheduleContent;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.AdminNotificationNewsRequest;
import com.driverspa.model.api.response.ClientsResponse;
import com.driverspa.util.Constants;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AddNotificationNewsRequestEvent;
import com.driverspa.util.otto.ws.AddNotificationNewsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.NDSpinner;

public class AdminAddNotificationsActivity extends AdminBaseActivity {

    @BindView(R.id.spinner)
    NDSpinner spinner;
    @BindView(R.id.text)
    EditText text;
    @BindView(R.id.txtClients)
    TextView txtClients;
    @BindView(R.id.clientsLayout)
    View clientsLayout;
    AdminNotificationNewsRequest request;

    HashMap<String,String> reverseTarget = new HashMap<String,String>();
    Washer washer;
    String[] valuesArray;
    int selectedItem = 0;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_admin_add_notification);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        titleView.setText("Уведомление клиентам");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);
        valuesArray = getResources().getStringArray(R.array.newsTarget);
        createActionBarSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    private void createActionBarSpinner(){
        if(spinner != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.newsTarget, R.layout.spinner_layout);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    selectedItem = pos;
                    txtClients.setText("Выберите клиентов");
                    selectedClients = null;
                    if(pos == 1){
                        clientsLayout.setVisibility(View.GONE);
                    }
                    else
                        clientsLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    
                }
            });
                    
            for (Map.Entry<String, String> entry : Constants.newsTarget.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                reverseTarget.put(value,key);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @OnClick(R.id.clients)
    public void onClientsClicked(){
        Intent intent = new Intent(this,ClientSMSSendActivity.class);
        intent.putExtra(ClientSMSSendActivity.NEWS_TARGET_EXTRA,reverseTarget.get(valuesArray[selectedItem]));
        startActivityForResult(intent,555);
        overridePendingTransitionWithCommoOpeningTransition(this);
    }

    List<ClientInfo> selectedClients;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 555){
            if(resultCode == RESULT_OK){
                ClientsResponse response = ClientsResponse.deserialize(data.getStringExtra(ClientSMSSendActivity.SELECTED_CLIENTS));
                if(response != null && response.getClients() != null) {
                    selectedClients = response.getClients();
                    txtClients.setText("Выбрано " + response.getClients().size() + " клиент(ов)");
                }
                else{
                    txtClients.setText("Выберите клиентов");
                }
            }
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
    }

    @OnClick(R.id.btnSend)
    public void onAddNewsClicked(){

        if(TextUtils.isEmpty(text.getText().toString())){
            ToastUtil.display(this,"Введите сообщение");
            text.requestFocus();
            return;
        }

        request = new AdminNotificationNewsRequest();
        request.setCarwash(URL_PREFIX + washer.getId());

        if(spinner != null && selectedItem != -1 && !TextUtils.isEmpty(reverseTarget.get(valuesArray[selectedItem]))) {
            request.setNewsTarget(reverseTarget.get(valuesArray[selectedItem]));
        }
        else{
            ToastUtil.display(this,"Выберите получателей");
            return;
        }

        String title = "Автомойка "+washer.getName().toLowerCase().replace("автомойка","")+": ";
        request.setText(title+text.getText().toString());
        request.setNewsType(INFO);
        request.setStatus(PENDING);

        if(reverseTarget.get(valuesArray[selectedItem]).equals(CLIENT_ALL_SMS)){
            if(selectedClients == null){
                ToastUtil.display(this,"Нет клиентов для отправки смс");
                return;
            }

            if(selectedClients.size() == 0){
                ToastUtil.display(this,"Нет клиентов для отправки смс");
                return;
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
				dialog.setTitle("Внимание!");
                dialog.setMessage("Смс будет отправлен с вашего телефона. Убедитесь в том, что у вас хватит баланса отправить смс всем клиентам. Для проверки отправленных смс, откройте приложение СМС. Хотите продолжить?");
                dialog.setNegativeButton("Нет",null);
				dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
                        request.setStatus(Constants.NEWS_SENT);
                        setWaitScreen(true);
                        BA.getEventBus().post(new AddNotificationNewsRequestEvent(request));
                     }
				});
				dialog.show();

        }
        else if(reverseTarget.get(valuesArray[selectedItem]).equals(CLIENT_WASHME_PUSH)){
//            request.setStatus(Constants.NEWS_SENT);
            setWaitScreen(true);
            BA.getEventBus().post(new AddNotificationNewsRequestEvent(request));
        }
        else if(reverseTarget.get(valuesArray[selectedItem]).equals(CLIENT_SMS)){

            if(selectedClients == null){
                ToastUtil.display(this,"Нет клиентов для отправки смс");
                return;
            }

            if(selectedClients.size() == 0){
                ToastUtil.display(this,"Нет клиентов для отправки смс");
                return;
            }

            AlertDialog.Builder dialog = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
            dialog.setTitle("Внимание!");
            dialog.setMessage("Смс будет отправлен с вашего телефона. Убедитесь в том, что у вас хватит баланса отправить смс всем клиентам. Для проверки отправленных смс, откройте приложение СМС. Хотите продолжить?");
            dialog.setNegativeButton("Нет",null);
            dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    request.setStatus(Constants.NEWS_SENT);
                    setWaitScreen(true);
                    BA.getEventBus().post(new AddNotificationNewsRequestEvent(request));
                }
            });
            dialog.show();
        }
        else{
            ToastUtil.display(this,"Выберите получателей");
            return;
        }
    }

    @Subscribe
    public void onNotificationNewsResponseReceived(AddNotificationNewsResponseEvent event){
        setWaitScreen(false);
        if(event.getData() != null){
            if(BaseAssist.isSuccess(event.getData())){
                if(reverseTarget.get(valuesArray[selectedItem]).equals(CLIENT_ALL_SMS) ||  reverseTarget.get(valuesArray[selectedItem]).equals(CLIENT_SMS)) {
                    SmsScheduleContent content = new SmsScheduleContent(request, selectedClients);
                    UserPreferences.putSMSForClientsData(this, content.serialize());
                }
//                else{
//                    SmsScheduleContent content = new SmsScheduleContent(request, selectedClients);
//                    UserPreferences.putPUSHForClientsData(this, content.serialize());
//                }
                closeKeyboard();
                finish();
                overridePendingTransitionWithCommonCloseTransition();
                ToastUtil.display(this,"Успешно отправлено");
            }
            else{
                ToastUtil.display(this, event.getData().getMessage());
            }
        }
    }

    @Subscribe
    public void onWasherReceived(WasherGetResponseEvent event) {
        washer = event.getWasher();
        if (washer == null) {
            finish();
            overridePendingTransitionWithCommonCloseTransition();
            ToastUtil.display(this,"Неизвестная ошибка, невозможно отправить уведомление");
        }
    }
}
