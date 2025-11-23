package com.driverspa.admin.activity;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.dialog.MultipleSelectDialog;
import com.driverspa.model.Washer;
import com.driverspa.model.Washer.Contact;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherLocalUpdateRequestEvent;

public class AdditionalInformationActivity extends AdminBaseActivity {
	    
	Washer washer;
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
	ArrayList<Contact> contacts;
	
    @Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_additional_information);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		TextView titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Дополнительно");
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
		});

		ButterKnife.bind(this);
    } 


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
				finish();
				overridePendingTransitionWithCommonCloseTransition();
				break;
        }
        return true;
    }

	@Override
	public void onBackPressed() {
		saveData();
		finish();
		overridePendingTransitionWithCommonCloseTransition();
	}

	private void saveData(){
    	contacts = new ArrayList<Contact>();
    	Contact phoneContact = new Contact();
    	phoneContact.setType("phone");
    	phoneContact.setValue(phone.getText().toString());
        contacts.add(phoneContact);
    	Contact webContact = new Contact();
    	webContact.setType("website");
    	webContact.setValue(web.getText().toString());
        contacts.add(webContact);
    	Contact emailContact = new Contact();
    	emailContact.setType("email");
    	emailContact.setValue(email.getText().toString());
        contacts.add(emailContact);        	        
        washer.setContacts(contacts);
		BA.getEventBus().post(new WasherLocalUpdateRequestEvent(washer));
    }
    
//    @OnClick(R.id.time)
//    public void onTimeDefinitionClicked(){
//		Intent intent = new Intent(this,TimeDefinitionActivity.class);
//		startActivityForResult(intent, ActivityForResult.ACTIVITY_TIME_DEFINITION);
//    }
    
    @OnClick(R.id.addinfo)
    public void onAddInfoClicked(){
    	addInfoDialog.show();
    }

    @OnClick(R.id.payType)
    public void onPayTypeClicked(){
    	paymentDialog.show();			
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
		if(washer != null){
			saveData();
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if(washer != null) {


			if(washer.getPayOptions() != null && washer.getPayOptions().size() > 0){
				if(washer.getPayOptions().size() == 1) payType.setText(washer.getPayOptions().get(0).toString());
				else payType.setText("Выбрано ("+washer.getPayOptions().size()+") вид оплаты");
			}

			if(washer.getAdditionalInfo() != null && washer.getAdditionalInfo().size() > 0){
				if(washer.getAdditionalInfo().size() == 1) addInfo.setText(washer.getAdditionalInfo().get(0));
				else addInfo.setText("Выбрано ("+washer.getAdditionalInfo().size()+") доп. информации");
			}

			contacts = washer.getContacts();
			if(contacts != null && contacts.size() > 0){
				for(Contact contact : contacts){
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
				contacts = new ArrayList<Contact>();
				Contact phoneContact = new Contact();
				phoneContact.setType("phone");
				phoneContact.setValue("");
				contacts.add(phoneContact);
				Contact webContact = new Contact();
				webContact.setType("website");
				webContact.setValue("");
				contacts.add(webContact);
				Contact emailContact = new Contact();
				emailContact.setType("email");
				emailContact.setValue("");
				contacts.add(emailContact);
			}

			//Pay Options
			final HashMap<String,String> allPayTypes = BA.getReference().getPayoptions();
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
			paymentDialog = new MultipleSelectDialog(this, listPayTypes, "Выберите тип оплаты", selectedPayTypes) {
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
			final HashMap<String,String> allAddInfo = BA.getReference().getAdditionalInfo();
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
			addInfoDialog = new MultipleSelectDialog(this, listAddInfos, "Выберите доп. Информацию", selectedAddInfos) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

					if(namesSelectedItm.size() == 0){
						addInfo.setText("Доп. информация");
					}
					else if(namesSelectedItm.size() == 1){
						requestAddInfoTypes.add(namesSelectedItm.get(0));
						addInfo.setText(allPayTypes.get(namesSelectedItm.get(0)));
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
