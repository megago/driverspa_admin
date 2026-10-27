package com.driverspa.admin.activity;

import android.content.Intent;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.admin.payment.KaspiQrBlock;
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
	KaspiQrBlock kaspiQrBlock;

	@BindView(R.id.phone)
	EditText phone;
	@BindView(R.id.web)
	EditText web;
	
	@BindView(R.id.txtPayType)
	TextView payType;	
	@BindView(R.id.txtAddInfo)
	TextView addInfo;
	ArrayList<Contact> contacts;

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
		titleView.setText(BA.str(R.string.additional));
		mToolbar.findViewById(R.id.action_done).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
		});

		ButterKnife.bind(this);
		kaspiQrBlock = new KaspiQrBlock(this, findViewById(R.id.kaspiQrBlock), new KaspiQrBlock.Host() {
			@Override
			public void startActivityForResult(Intent intent, int requestCode) {
				AdditionalInformationActivity.this.startActivityForResult(intent, requestCode);
			}

			@Override
			public void onPaymentQrChanged() {
				// Keeps the edited contacts and redraws the payment options from the washer.
				saveData();
			}
		});
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		kaspiQrBlock.onActivityResult(requestCode, resultCode, data);
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
    	// Email is no longer edited here (the account email is shown on the
    	// profile). Preserve any existing washer contact email so saving the
    	// other fields doesn't wipe it.
    	String existingEmail = "";
    	if (washer != null && washer.getContacts() != null) {
    		for (Contact c : washer.getContacts()) {
    			if ("email".equals(c.getType())) { existingEmail = c.getValue(); break; }
    		}
    	}
    	Contact emailContact = new Contact();
    	emailContact.setType("email");
    	emailContact.setValue(existingEmail);
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
		kaspiQrBlock.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		BA.getEventBus().unregister(this);
		kaspiQrBlock.onPause();
		if(washer != null){
			saveData();
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
		if(washer != null) {
			kaspiQrBlock.bind(washer);


			if(washer.getPayOptions() != null && washer.getPayOptions().size() > 0){
				if(washer.getPayOptions().size() == 1) payType.setText(washer.getPayOptions().get(0).toString());
				else payType.setText(BA.str(R.string.selected_paren)+washer.getPayOptions().size()+BA.str(R.string.paren_payment_type));
			}

			if(washer.getAdditionalInfo() != null && washer.getAdditionalInfo().size() > 0){
				if(washer.getAdditionalInfo().size() == 1) addInfo.setText(washer.getAdditionalInfo().get(0));
				else addInfo.setText(BA.str(R.string.selected_paren)+washer.getAdditionalInfo().size()+BA.str(R.string.paren_additional_info));
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

			listPayTypes.addAll(allPayTypes.entrySet().stream()
					.map(entry -> toDialogItem(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList()));

			if(washer.getPayOptions() != null){
				selectedPayTypes.addAll(washer.getPayOptions().stream()
						.map(optionId -> toDialogItem(optionId, allPayTypes.get(optionId)))
						.collect(Collectors.toList()));
			}

			final ArrayList<String> requestPayTypes = new ArrayList<String>();
			paymentDialog = new MultipleSelectDialog(this, listPayTypes, BA.str(R.string.select_payment_type), selectedPayTypes) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

					if(namesSelectedItm.size() == 0){
						payType.setText(BA.str(R.string.payment_type));
					}
					else if(namesSelectedItm.size() == 1){
						requestPayTypes.add(namesSelectedItm.get(0));
						payType.setText(allPayTypes.get(namesSelectedItm.get(0).toString()));
					}
					else{
						payType.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_payment_type));
						requestPayTypes.addAll(toOrderedSelection(namesSelectedItm));
					}
					washer.setPayOptions(new ArrayList<String>(requestPayTypes));
					requestPayTypes.clear();
				}
			};

			//Additional Information
			final HashMap<String,String> allAddInfo = BA.getReference().getAdditionalInfo();
			List<HashMap<String,String>> listAddInfos = new ArrayList<HashMap<String,String>>();
			List<HashMap<String,String>> selectedAddInfos = new ArrayList<HashMap<String,String>>();

			listAddInfos.addAll(allAddInfo.entrySet().stream()
					.map(entry -> toDialogItem(entry.getKey(), entry.getValue()))
					.collect(Collectors.toList()));

			if(washer.getAdditionalInfo() != null){
				selectedAddInfos.addAll(washer.getAdditionalInfo().stream()
						.map(infoId -> toDialogItem(infoId, allAddInfo.get(infoId)))
						.collect(Collectors.toList()));
			}

			final ArrayList<String> requestAddInfoTypes = new ArrayList<String>();
			addInfoDialog = new MultipleSelectDialog(this, listAddInfos, BA.str(R.string.select_additional_info), selectedAddInfos) {
				@Override
				public void onDismiss(HashMap<Integer, String> namesSelectedItm) {

					if(namesSelectedItm.size() == 0){
						addInfo.setText(BA.str(R.string.additional_info));
					}
					else if(namesSelectedItm.size() == 1){
						requestAddInfoTypes.add(namesSelectedItm.get(0));
						addInfo.setText(allPayTypes.get(namesSelectedItm.get(0)));
					}
					else{
						addInfo.setText(BA.str(R.string.selected_paren)+namesSelectedItm.size()+BA.str(R.string.paren_additional_info));
						requestAddInfoTypes.addAll(toOrderedSelection(namesSelectedItm));
					}
					washer.setAdditionalInfo(new ArrayList<String>(requestAddInfoTypes));
					requestAddInfoTypes.clear();
				}
			};

		}
	}

}
