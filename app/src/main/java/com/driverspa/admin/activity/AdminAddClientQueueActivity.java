package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.URL_PREFIX;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.adapter.GroupServicesGridAdapter;
import com.driverspa.adapter.OfflineBoxesGridAdapter;
import com.driverspa.adapter.ServicesGridAdapter;
import com.driverspa.assist.BaseAssist;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.adapter.FilterableClientsAdapter;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.Box;
import com.driverspa.model.CarType;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServiceItem;
import com.driverspa.model.WashCountCampaign;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.ws.AdminBookRequestEvent;
import com.driverspa.util.otto.ws.AdminBookResponseEvent;
import com.driverspa.util.otto.ws.AdminClientResponseEvent;
import com.driverspa.util.otto.ws.AdminClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DelayAutoCompleteTextView;
import com.driverspa.view.ExpandableGridView;
import com.driverspa.view.NDSpinner;

public class AdminAddClientQueueActivity extends AdminBaseActivity {
	    
	Washer washer;
	String boxId;

	@BindView(R.id.btnSedan)
	View buttonSedan;
	@BindView(R.id.btnCrossover)
	View buttonCrossover;
	@BindView(R.id.btnBigJeep)
	View buttonBigJeep;
	@BindView(R.id.btnMinbus)
	View buttonMinbus;
	@BindView(R.id.btnMini)
	View buttonMini;
	@BindView(R.id.btnUniversal)
	View buttonUniversal;
	@BindView(R.id.btnMiniJeep)
	View buttonMiniJeep;
	@BindView(R.id.btnMinivan)
	View buttonMiniVan;
	@BindView(R.id.btnMoto)
	View buttonMoto;

	@BindView(R.id.carNo)
	DelayAutoCompleteTextView carNo;
	@BindView(R.id.carMark)
	EditText carMark;
	@BindView(R.id.clientName)
	EditText clientName;
	@BindView(R.id.phone)
	EditText mobile;
	@BindView(R.id.discount)
	EditText discount;
	@BindView(R.id.discountAmount)
	EditText discountAmount;
	@BindView(R.id.comment)
	EditText comment;
	CarType selectedCarType;
	@BindView(R.id.txtPrice)
	TextView txtPrice;
	@BindView(R.id.txtTime)
	TextView txtTime;
	TextView titleView;

	@BindView(R.id.no_service)
	View noService;
	@BindView(R.id.no_offline_box)
	View noOfflineBox;

	@BindView(R.id.detailLayout)
	View detailLayout;
	@BindView(R.id.more)
	TextView moreButton;

	@BindView(R.id.extra)
	TextView extraButton;
	@BindView(R.id.extraLayout)
	View extraLayout;
	@BindView(R.id.paid)
	CheckBox paid;
	@BindView(R.id.nocashLayout)
	View nocashLayout;
	@BindView(R.id.nocash)
	CheckBox nocash;
	@BindView(R.id.discountSpinner)
	NDSpinner spinner;

	int selectedItem = 0;
	TextWatcher discountAmountWatcher;

	@BindView(R.id.btnBook)
	Button buttonBook;

	BookingRequest request;
	HashMap<Integer, Double> menuServicesPrices = new HashMap<Integer, Double>();

	@BindView(R.id.grid_group_services)
	ExpandableGridView gridGroupServices;
	@BindView(R.id.grid_services)
	ExpandableGridView gridServices;
	@BindView(R.id.grid_boxes)
	ExpandableGridView gridBoxes;
	@BindView(R.id.groupBtn)
	View groupBtn;
	@BindView(R.id.serviceBtn)
	View serviceBtn;
	MaskedWatcher maskedWatcher;
	@BindView(R.id.clients)
	TextView clients;
	@BindView(R.id.washCountCampaignDescription)
	TextView washCountCampaignDescription;

	private FilterableClientsAdapter clientsAdapter;
	private ServicesGridAdapter serviceAdapter;
	private GroupServicesGridAdapter groupServiceAdapter;
	private OfflineBoxesGridAdapter boxAdapter;
	ClientInfo client;

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_add_client_queue);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.to_box));

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
    	ButterKnife.bind(this);

		String clientStr = getIntent().getStringExtra(Constants.CLIENT_INFO);
		if(!TextUtils.isEmpty(clientStr)) {
			client = ClientInfo.deserialize(clientStr);
		}
        else{
			ToastUtil.display(this,BA.str(R.string.err_get_client));
		}

		selectedCarType = CarType.Sedan;
		groupBtn.setSelected(true);
		serviceBtn.setSelected(false);
//		groupServiceLayout.setVisibility(View.VISIBLE);
//		serviceLayout.setVisibility(View.GONE);

		discount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					int value = Integer.parseInt(s.toString());
					if(value > 100){
						discount.setText("100");
					}
				}
				catch(Exception e){}

				if(serviceAdapter != null && serviceAdapter.getData() != null)
				  calculatePriceAndTime((ArrayList) serviceAdapter.getData(),(ArrayList) groupServiceAdapter.getData());
			}
		});

		discountAmountWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (serviceAdapter != null && serviceAdapter.getData() != null)
					calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
			}
		};

		discountAmount.addTextChangedListener(discountAmountWatcher);


		titleView.setText(BA.str(R.string.send_to_box));
		request = new BookingRequest();
		buttonSedan.setOnClickListener(buttonSelector);
		buttonCrossover.setOnClickListener(buttonSelector);
		buttonBigJeep.setOnClickListener(buttonSelector);
		buttonMinbus.setOnClickListener(buttonSelector);
		buttonMoto.setOnClickListener(buttonSelector);
		buttonMini.setOnClickListener(buttonSelector);
		buttonMiniJeep.setOnClickListener(buttonSelector);
		buttonMiniVan.setOnClickListener(buttonSelector);
		buttonUniversal.setOnClickListener(buttonSelector);

		buttonSedan.setSelected(true);
		buttonCrossover.setSelected(false);
		buttonBigJeep.setSelected(false);
		buttonMinbus.setSelected(false);
		buttonMoto.setSelected(false);

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth()-(int) Functions.dipToPixels(this, 10);

		ViewGroup.LayoutParams layoutParamsServices = gridServices.getLayoutParams();
		layoutParamsServices.width = width;
		gridServices.setLayoutParams(layoutParamsServices);
		gridGroupServices.setLayoutParams(layoutParamsServices);

		//set layout params same as grid services
		gridBoxes.setLayoutParams(layoutParamsServices);

		boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
		boolean tabletBigSize = getResources().getBoolean(R.bool.isBigTablet);

		if(!tabletSize) {
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
			gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
		}
		else if(!tabletBigSize) {
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
			gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
		}
		else{
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
			gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
		}

		createActionBarSpinner();
	}

	private void createActionBarSpinner(){
		if(spinner != null) {
			String[] valuesArray = new String[]{"%     ", "₸     "};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
			adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					selectedItem = pos;
					selectDiscountType();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
			spinner.setAdapter(adapter);
			spinner.setSelection(selectedItem);
		}
	}

	private void selectDiscountType(){
		if(selectedItem == 0){
			discount.setText("0");
			discount.setVisibility(View.VISIBLE);
			discountAmount.setVisibility(View.GONE);

		}
		else{
			discountAmount.setText("0");
			discount.setVisibility(View.GONE);
			discountAmount.setVisibility(View.VISIBLE);
		}
		if (serviceAdapter != null && serviceAdapter.getData() != null)
			calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
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
				overridePendingTransition(0,0);
                break;
        }
        return true;
    }

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(0,0);
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
		if (washer != null) {
			if(washer.getPayOptions() != null && washer.getPayOptions().size() > 0){
				for(String options : washer.getPayOptions()){
					if(!options.equals("cash")){
						nocashLayout.setVisibility(View.VISIBLE);
					}
				}
			}

			if(client != null){
				setData(client);
			}
			makeCarTypeButtons(washer.getCarTypes());
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
									@Override
									public void run() {
										createService(selectedCarType.getValue());
										createBoxGrid(washer);

										carNo.setThreshold(2);
										clientsAdapter = new FilterableClientsAdapter(AdminAddClientQueueActivity.this,new ArrayList<ClientInfo>(),washer);
										carNo.setAdapter(clientsAdapter);
										carNo.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_bar));
										carNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
											@Override
											public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
												ClientInfo client = ((ClientInfo)clientsAdapter.getItem(position));
												setData(client);
											}
										});

									}}, 50);

		}
	}

	private void createService(Integer carType){
		resetPriceAndTime();
		//Services
		ArrayList<ServiceItem> serviceItems = new ArrayList<ServiceItem>();
		final LinkedHashMap<Integer,String> dictionary = BA.getReference().getServices();
		LinkedHashMap<Integer,ArrayList<Washer.Prices>> washerMenu = washer.getMenu();
		if(washerMenu != null && washerMenu.size() > 0){
			serviceItems.clear();
			menuServicesPrices.clear();
			for (Map.Entry<Integer, ArrayList<Washer.Prices>> entry : washerMenu.entrySet()) {
				Integer key = entry.getKey();
				if(key == carType){
					ArrayList<Washer.Prices> pricesMenu = entry.getValue();
					for(Washer.Prices priceMenu : pricesMenu){
						ServiceItem newItem = new ServiceItem();
						newItem.setServiceId(priceMenu.getType());
						newItem.setServiceName(dictionary.get(priceMenu.getType()));
						newItem.setPrice(priceMenu.getPrice());
						newItem.setTime(priceMenu.getTime());
						serviceItems.add(newItem);
					}
					break;
				}
			}
		}

		//Group services
		List<Washer.GroupMenu> groupServiceItems = new ArrayList<Washer.GroupMenu>();
        List<Washer.GroupMenu> washerGroup = washer.getGroupMenu();
		if(washerGroup != null && washerGroup.size() > 0){
			groupServiceItems.clear();
			for(Washer.GroupMenu g:washerGroup){
				for(ReversePrices p:g.getPrices()){
					if(p.getCarType() == carType){
						groupServiceItems.add(g);
					}
				}
			}
		}

		//Service adapter
		serviceAdapter = new ServicesGridAdapter(this,serviceItems);
		gridServices.setAdapter(serviceAdapter);
		gridServices.setExpanded(true);
		gridServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				request.setServices(new ArrayList<Integer>());
				final ArrayList<Integer> requestServices = new ArrayList<Integer>();
				if (serviceAdapter.getSelectedPosition().get(position) != null && serviceAdapter.getSelectedPosition().get(position)) {
					serviceAdapter.setSelectedPosition(position, false);
					serviceAdapter.notifyDataSetChanged();
				} else {
					if(serviceAdapter.getData().get(position).getPrice() > 0) {
					   if(request != null && request.getGroupServices() != null && request.getGroupServices().size() > 0){
						   Washer.GroupMenu selectedGroup = null;
//						   if(groupServiceAdapter != null && groupServiceAdapter.getData() != null && groupServiceAdapter.getData().size() > 0) {
//							   for (Washer.GroupMenu g : groupServiceAdapter.getData()) {
//	                             for(String gId : request.getGroupServices()){
//									 if(gId.equals(g.getId())){
//										 selectedGroup = g;
//										 break;
//									 }
//								 }
//							   }
//					      }
						   if(selectedGroup != null && selectedGroup.getServices()!=null && selectedGroup.getServices().contains(serviceAdapter.getData().get(position).getServiceId())){
							   ToastUtil.display(BA.getContext(),BA.str(R.string.service_already_in_complex));
						   }
						   else{
							   serviceAdapter.setSelectedPosition(position, true);
							   serviceAdapter.notifyDataSetChanged();
						   }

					   }
						else {
						   serviceAdapter.setSelectedPosition(position, true);
						   serviceAdapter.notifyDataSetChanged();
					   }
					}
					else{
						ToastUtil.display(BA.getContext(),BA.str(R.string.err_no_price_for_type));
					}
				}

				for (int i = 0; i < serviceAdapter.getData().size(); i++) {
					if (serviceAdapter.getSelectedPosition().get(i) != null && serviceAdapter.getSelectedPosition().get(i)) {
						requestServices.add(serviceAdapter.getData().get(i).getServiceId());
					}
				}
				request.setServices(new ArrayList<Integer>(requestServices));
				requestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(),(ArrayList) groupServiceAdapter.getData());
			}
		});

		if(serviceItems.size() == 0){
			gridServices.setVisibility(View.GONE);
			noService.setVisibility(View.VISIBLE);
		}
		else{
			gridServices.setVisibility(View.VISIBLE);
			noService.setVisibility(View.GONE);
		}

		//Group Service adapter
		groupServiceAdapter = new GroupServicesGridAdapter(this,groupServiceItems);
		gridGroupServices.setAdapter(groupServiceAdapter);
		gridGroupServices.setExpanded(true);
		gridGroupServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				request.setGroupServices(new ArrayList<String>());
				final ArrayList<String> groupRequestServices = new ArrayList<String>();

				if (groupServiceAdapter.getSelectedPosition().get(position) != null && groupServiceAdapter.getSelectedPosition().get(position)) {
					groupServiceAdapter.setSelectedPosition(position, false);
					groupServiceAdapter.notifyDataSetChanged();
				}
				else{
					Washer.GroupMenu selectedGroup = groupServiceAdapter.getData().get(position);

				  if(selectedGroup != null && selectedGroup.getServices() != null && request != null && request.getServices() != null && request.getServices().size() > 0){
					  boolean found = false;
//					  for(Integer s : request.getServices()){
//						  if(selectedGroup.getServices().contains(s)){
//							  found = true;
//						  }
//					  }
					  if(found) {
						  ToastUtil.display(BA.getContext(), BA.str(R.string.service_already_selected_complex));
					  }
					  else{
						  groupServiceAdapter.setSelectedPosition(position, true);
						  groupServiceAdapter.notifyDataSetChanged();
					  }
				 	}
					else {
					  groupServiceAdapter.setSelectedPosition(position, true);
					  groupServiceAdapter.notifyDataSetChanged();
				  }

				}

				for (int i = 0; i < groupServiceAdapter.getData().size(); i++) {
					if (groupServiceAdapter.getSelectedPosition().get(i) != null && groupServiceAdapter.getSelectedPosition().get(i)) {
						groupRequestServices.add(groupServiceAdapter.getData().get(i).getId());
					}
				}
				request.setGroupServices(new ArrayList<String>(groupRequestServices));
				groupRequestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(),(ArrayList) groupServiceAdapter.getData());
			}
		});

//		if(serviceItems.size() == 0){
//			gridServices.setVisibility(View.GONE);
//			noService.setVisibility(View.VISIBLE);
//		}
//		else{
//			gridServices.setVisibility(View.VISIBLE);
//			noService.setVisibility(View.GONE);
//		}
//		if(groupServiceItems.size() == 0){
//			gridGroupServices.setVisibility(View.GONE);
//			noServiceGroup.setVisibility(View.VISIBLE);
//		}
//		else{
//			gridGroupServices.setVisibility(View.VISIBLE);
//			noServiceGroup.setVisibility(View.GONE);
//		}

		if (serviceItems.size() == 0) {
			gridServices.setVisibility(View.GONE);
		} else {
			gridServices.setVisibility(View.VISIBLE);
		}
		if (groupServiceItems.size() == 0) {
			gridGroupServices.setVisibility(View.GONE);
		} else {
			gridGroupServices.setVisibility(View.VISIBLE);
		}

		if (serviceItems.size() == 0 && groupServiceItems.size() == 0) {
			noService.setVisibility(View.VISIBLE);
		}
		else{
			noService.setVisibility(View.GONE);
		}

	}

	private void createBoxGrid(Washer washer){
		//Boxes
		ArrayList<Box> boxItems = new ArrayList<Box>();
		if(washer != null && washer.getBoxSettings() != null && washer.getBoxSettings().size() > 0)
	     for(Box item : washer.getBoxSettings()){
//			 if(item.getBookingType().getValue().equals(Constants.OFFLINE)) {
				 boxItems.add(item);
//			 }
		 }

		//Box adapter
		boxAdapter = new OfflineBoxesGridAdapter(this,boxItems);
		gridBoxes.setAdapter(boxAdapter);
		gridBoxes.setExpanded(true);
		gridBoxes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				for (int i = 0; i < boxAdapter.getData().size(); i++) {
					if (boxAdapter.getSelectedPosition().get(i) != null && boxAdapter.getSelectedPosition().get(i)) {
						boxAdapter.setSelectedPosition(i, false);
					}
				}
				boxAdapter.notifyDataSetChanged();

				boxAdapter.setSelectedPosition(position, true);
				boxAdapter.notifyDataSetChanged();
				boxId = ((Box)boxAdapter.getItem(position)).getUid();
			}
		});

		if(boxItems.size() == 0){
			gridBoxes.setVisibility(View.GONE);
			noOfflineBox.setVisibility(View.VISIBLE);
		}
		else{
			gridBoxes.setVisibility(View.VISIBLE);
			noOfflineBox.setVisibility(View.GONE);
		}
	}

	private void resetPriceAndTime(){
		txtPrice.setText("0 ₸");
		txtTime.setText(BA.str(R.string.zero_min));
		request.setServices(new ArrayList<Integer>());
		request.setGroupServices(new ArrayList<String>());
	}

	public void calculatePriceAndTime(ArrayList<ServiceItem> data, ArrayList<Washer.GroupMenu> groupData) {
		txtPrice.setText("0 ₸");
		txtTime.setText(BA.str(R.string.zero_min));
		double totalPrice = 0d;
		int totalTime = 0;
		for (int i = 0; i < data.size(); i++) {
			for (Integer service : request.getServices()) {
				if (data.get(i).getServiceId() == service) {
					totalPrice += (data.get(i).getPrice() != null ? data.get(i).getPrice() : 0d);
					totalTime += (data.get(i).getTime() != null ? data.get(i).getTime() : 0);
				}
			}
		}

		if (groupData != null && groupData.size() > 0) {
			for (int i = 0; i < groupData.size(); i++) {
				if (request.getGroupServices() != null && request.getGroupServices().size() > 0) {
					for (String groupId : request.getGroupServices()) {
						if (groupData.get(i).getId().equals(groupId)) {
							for (ReversePrices p : groupData.get(i).getPrices()) {
								if (p.getCarType() == selectedCarType.getValue()) {
									totalPrice += (p.getPrice() != null ? p.getPrice() : 0d);
									totalTime += (p.getTime() != null ? p.getTime() : 0);
								}
							}
						}
					}
				}
			}
		}

		if (totalPrice > 0) {
			if(selectedItem == 0) { //discount type is %
				int discnt = 0;
				if (!TextUtils.isEmpty(discount.getText().toString()))
					discnt = Integer.parseInt(discount.getText().toString());
				int totalPriceWithDiscount = (int) (Math.round(totalPrice) - Math.round(totalPrice) * discnt / 100);
				txtPrice.setText(totalPriceWithDiscount + " ₸");
				if (request != null)
					request.setServicePrice(totalPrice + "");
			}
			else{
				int discnt = 0;
				if (!TextUtils.isEmpty(discountAmount.getText().toString())) {
					try {
						discnt = Integer.parseInt(discountAmount.getText().toString());
					}
					catch (Exception e){}
				}
				int totalPriceWithDiscount = (int) (Math.round(totalPrice) - discnt);
				if(totalPriceWithDiscount < 0) {
					discountAmount.removeTextChangedListener(discountAmountWatcher);
					discountAmount.setText((int)totalPrice+"");
					discountAmount.setSelection(discountAmount.getText().toString().length());
					discountAmount.addTextChangedListener(discountAmountWatcher);
					totalPriceWithDiscount = 0;
				}
				txtPrice.setText(totalPriceWithDiscount + " ₸");
				if (request != null)
					request.setServicePrice(totalPrice + "");

			}
		}

		if (totalTime > 0) {
			txtTime.setText(Math.round(totalTime) + BA.str(R.string.space_min));
			if (request != null)
				request.setServiceTotalTime(totalTime);

		}
	}


	@OnFocusChange(R.id.carNo)
	public void onFocusChangeCarNo(boolean focus){
		if(focus){
			carNo.setError(null);
		}
	}

	@OnFocusChange(R.id.discount)
	public void onFocusChangeDiscount(boolean focus) {
		if (!focus) {
			if (TextUtils.isEmpty(discount.getText().toString()))
				discount.setText("0");
			else {
				try{
					int value = Integer.parseInt(discount.getText().toString());
					if(value > 100){
						discount.setText("100");
					}
				}
				catch(Exception e){
				}
			}
		} else {
			if (!TextUtils.isEmpty(discount.getText().toString()) && discount.getText().toString().equals("0")) {
				discount.setText("");
			}
		}
	}

	@OnFocusChange(R.id.discountAmount)
	public void onFocusChangeDiscountAmount(boolean focus) {
		if (!focus) {
			if (TextUtils.isEmpty(discountAmount.getText().toString()))
				discountAmount.setText("0");
		} else {
			if (!TextUtils.isEmpty(discountAmount.getText().toString()) && discountAmount.getText().toString().equals("0")) {
				discountAmount.setText("");
			}
		}
	}

	@OnClick(R.id.btnBook)
	public void createBookingRequest(){
		carNo.setError(null);
		closeKeyboard();
		String сarNoStr = carNo.getText().toString();
		if (TextUtils.isEmpty(сarNoStr)){
			ToastUtil.displayAtTop(this, BA.str(R.string.enter_car_plate));
			carNo.setError("");
			carNo.requestFocus();
			return;
		}

		String phoneStr = mobile.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

		if(selectedCarType == null) {
			ToastUtil.displayAtTop(this, BA.str(R.string.select_car));
			return;
		}

		if(!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)){
			ToastUtil.displayAtTop(this, BA.str(R.string.select_services));
			return;
		}

		if(TextUtils.isEmpty(boxId)){
			ToastUtil.displayAtTop(this, BA.str(R.string.select_box));
			return;
		}

		request.setCarNo(сarNoStr);
		request.setMobile(TextUtils.isEmpty(phoneStr)?"":"+7"+phoneStr);
		request.setClientKey(сarNoStr.trim());
		if(!TextUtils.isEmpty(clientName.getText().toString().trim()))
			request.setClientKey(сarNoStr.trim()+" ("+clientName.getText().toString()+")");
		request.setCarType(selectedCarType.getValue());
        request.setBoxId(boxId);
		request.setTime(Instant.now());
		request.setCarwash(URL_PREFIX+washer.getId());
		request.setOffline(true);
		request.setQueued(true);
		request.setDirectWash(true);
		request.setCarMark(carMark.getText().toString());
		request.setPaid(paid.isChecked());
		request.setComment(comment.getText().toString());
		request.setNocash(nocash.isChecked());

		if(selectedItem == 1) {
			try {
				double value = Double.parseDouble(discountAmount.getText().toString());
				request.setDiscountAmount(value);
			} catch (NumberFormatException e) {
				request.setDiscountAmount(0d);
			}
		}
		else{
			request.setDiscountAmount(0d);
		}

		if(selectedItem == 0) {
			try {
				request.setDiscount(Double.parseDouble(discount.getText().toString()));
			} catch (NumberFormatException e) {
				request.setDiscount(0d);
			}
		}
		else{
			request.setDiscount(0d);
		}

		BA.getEventBus().post(new AdminBookRequestEvent(request));
		setWaitScreen(true);
	}

	@Subscribe
	public void onAdminBookResponseReceived(AdminBookResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				ToastUtil.display(this, BA.str(R.string.sent_to_box_ok));
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(this, event.getResult().getMessage());
		}
		else{
			ToastUtil.display(this, BA.str(R.string.err_send_to_box));
		}
	}

	@OnClick(R.id.more)
	public void moreFieldsClicked(){
	  if(detailLayout.getVisibility() == View.GONE) {
		  detailLayout.setVisibility(View.VISIBLE);
		  moreButton.setText(BA.str(R.string.additional));
		  moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
		  buttonBook.setEnabled(true);
	  }
		else{
		  detailLayout.setVisibility(View.GONE);
		  moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
		  moreButton.setText(BA.str(R.string.additional));
		  buttonBook.setEnabled(false);
	  }
	}

	@OnClick(R.id.extra)
	public void extraFieldsClicked(){
	  if(extraLayout.getVisibility() == View.GONE) {
		  extraLayout.setVisibility(View.VISIBLE);
		  extraButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
	  }
		else{
		  extraLayout.setVisibility(View.GONE);
		  extraButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
	  }
	}

	private void makeCarTypeButtons(List<Integer> cartypes){
		buttonMini.setVisibility(View.GONE);
		buttonSedan.setVisibility(View.GONE);
		buttonUniversal.setVisibility(View.GONE);
		buttonCrossover.setVisibility(View.GONE);
		buttonMiniJeep.setVisibility(View.GONE);
		buttonBigJeep.setVisibility(View.GONE);
		buttonMiniVan.setVisibility(View.GONE);
		buttonMinbus.setVisibility(View.GONE);
		buttonMoto.setVisibility(View.GONE);
		
		if(cartypes.contains(CarType.Mini.getValue())){
			buttonMini.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Sedan.getValue())){
			buttonSedan.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Universal.getValue())){
			buttonUniversal.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Crossover.getValue())){
			buttonCrossover.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.MiniJeep.getValue())){
			buttonMiniJeep.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Jeep.getValue())){
			buttonBigJeep.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Minivan.getValue())){
			buttonMiniVan.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Minibus.getValue())){
			buttonMinbus.setVisibility(View.VISIBLE);
		}
		if(cartypes.contains(CarType.Moto.getValue())){
			buttonMoto.setVisibility(View.VISIBLE);
		}
	}

	private View.OnClickListener buttonSelector = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if ( view.getId() == buttonSedan.getId() && !buttonSedan.isSelected()){
				buttonSedan.setSelected(true);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Sedan;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMini.getId()  && !buttonMini.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(true);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Mini;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonUniversal.getId()  && !buttonUniversal.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(true);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Universal;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMiniJeep.getId()  && !buttonMiniJeep.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(true);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.MiniJeep;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMiniVan.getId()  && !buttonMiniVan.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(true);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Minivan;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonCrossover.getId()  && !buttonCrossover.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(true);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Crossover;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonBigJeep.getId()  && !buttonBigJeep.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(true);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Jeep;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMoto.getId()  && !buttonMoto.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(false);
				buttonMoto.setSelected(true);
				selectedCarType = CarType.Moto;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
			else if ( view.getId() == buttonMinbus.getId()  && !buttonMinbus.isSelected()){
				buttonSedan.setSelected(false);
				buttonMini.setSelected(false);
				buttonCrossover.setSelected(false);
				buttonMiniVan.setSelected(false);
				buttonMiniJeep.setSelected(false);
				buttonUniversal.setSelected(false);
				buttonBigJeep.setSelected(false);
				buttonMinbus.setSelected(true);
				buttonMoto.setSelected(false);
				selectedCarType = CarType.Minibus;
				createService(selectedCarType.getValue());
				closeKeyboard();
			}
		}
	};

	private void setData(ClientInfo client){
		analyzeWasherCountCampaign(client);
		if(!TextUtils.isEmpty(client.getName())) {
			clientName.setText(client.getName().trim());
			clientName.setSelection(clientName.getText().length());
		}
		else
			clientName.setText("");

		if(!TextUtils.isEmpty(client.getCarNo())){
			if(client.getCarNo().contains("(")) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {carNo.setText(client.getCarNo().substring(0, client.getCarNo().indexOf("(")).trim(),false);}
				else{carNo.setText(client.getCarNo().substring(0, client.getCarNo().indexOf("(")).trim());}
				carNo.setSelection(carNo.getText().length());

				if(TextUtils.isEmpty(client.getName())) {
					clientName.setText(client.getCarNo().substring(client.getCarNo().indexOf("("), client.getCarNo().length()).replaceAll("\\(", "").replaceAll("\\)", "").trim());
					clientName.setSelection(clientName.getText().length());
				}
			}
			else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {carNo.setText(client.getCarNo().trim(),false);}
				else {carNo.setText(client.getCarNo().trim());}
				carNo.setSelection(carNo.getText().length());
			}
		}
		else{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {carNo.setText("",false);}
			else carNo.setText("");
		}

		if(!TextUtils.isEmpty(client.getPhone()) && client.getPhone().length() > 2){
			if(maskedWatcher!= null) mobile.removeTextChangedListener(maskedWatcher);
			mobile.setText(client.getPhone().substring(2,client.getPhone().length()));
//			mobile.setEnabled(false);
		}
		else{
			if(maskedWatcher!= null) mobile.addTextChangedListener(maskedWatcher);
			mobile.setText("");
		}

		if(client.getCarType() != null){
			selectedCarType = CarType.toEnum(client.getCarType());
			if(selectedCarType!=null && selectedCarType!= CarType.Unknown){
				switch(selectedCarType.getValue()){
					case 10:
						buttonMini.performClick();
						break;
					case 1:
						buttonSedan.performClick();
						break;
					case 11:
						buttonUniversal.performClick();
						break;
					case 2:
						buttonCrossover.performClick();
						break;
					case 30:
						buttonMiniJeep.performClick();
						break;
					case 3:
						buttonBigJeep.performClick();
						break;
					case 4:
						buttonMiniVan.performClick();
						break;
					case 41:
						buttonMinbus.performClick();
						break;
					case 5:
						buttonMoto.performClick();
						break;
				}
			}
		}
		closeKeyboard();
	}

	@Subscribe
	public void onAdminClientsReceived(final AdminClientsResponseEvent event) {
		setWaitScreen(false);
		if(carNo != null) carNo.stopLoading();
		if (washer != null) {
			if (event.getResult() != null) {
				if (clientsAdapter != null && BaseAssist.isSuccess(event.getResult()) && event.getResult() != null && event.getResult().getResponse() != null && event.getResult().getResponse().getClients() != null) {
					final Filter newFilter = new Filter() {
						@Override
						protected FilterResults performFiltering(CharSequence constraint) {
							FilterResults filterResults = new FilterResults();
							if (constraint != null) {
								filterResults.values = event.getResult().getResponse().getClients();
								filterResults.count = event.getResult().getResponse().getClients().size();
							}
							return filterResults;
						}

						@Override
						protected void publishResults(CharSequence constraint, FilterResults results) {
							if (results != null && results.count > 0) {
								clientsAdapter.setFilter(this);
							}
						}
					};
					clientsAdapter.setData(event.getResult().getResponse().getClients());
					clientsAdapter.notifyDataSetChanged();
				} else {

				}
			} else {

			}
		}
	}

	private void analyzeWasherCountCampaign(ClientInfo client){
		washCountCampaignDescription.setVisibility(View.GONE);
		if(washer != null && washer.getWashCountCampaigns()!=null && washer.getWashCountCampaigns().size() > 0) {
			WashCountCampaign selectedCampaign = null;
			boolean hasSameFactor = false;
			WashCountCampaign selectedCampaignOld = null;
			for(WashCountCampaign campaign : washer.getWashCountCampaigns()) {
				if((client.getFinishedBooksCount()+1)%campaign.getWashCount() == 0){
					if(selectedCampaign != null) {hasSameFactor = true; selectedCampaignOld = selectedCampaign;}
					selectedCampaign = campaign;
				}
			}

			if(hasSameFactor){

				if(selectedCampaign.getWashCount() < selectedCampaignOld.getWashCount()) {
					if (((client.getFinishedBooksCount()+1)/selectedCampaign.getWashCount())%2 == 0){
						selectedCampaign = selectedCampaignOld;
					}
				}
				else{
					if (((client.getFinishedBooksCount()+1)/selectedCampaignOld.getWashCount())%2 != 0){
						selectedCampaign = selectedCampaignOld;
					}
				}
			}

			if(selectedCampaign != null) {
				washCountCampaignDescription.setVisibility(View.VISIBLE);
				washCountCampaignDescription.setText(selectedCampaign.getDescription());
			}
		}
	}

	@Subscribe
	public void onSearchSubmitted(SearchEvent event){
		if(event != null && event.getQuery() != null && filter != null && washer != null){
			filter.setSearchText(event.getQuery());
			filter.setCarwash(washer.getId());
			filter.setOffset(Integer.toString(0));
			filter.setClientType("all");
			BA.getEventBus().post(new AdminClientsRequestEvent(filter));
		}
	}
	AdminSearchFilter filter = new AdminSearchFilter();


	@OnClick(R.id.paid_text)
	public void onPaidTextClicked(){
		paid.setChecked(!paid.isChecked());
	}

	@OnClick(R.id.nocashText)
	public void onNocashTextClicked() {
		nocash.setChecked(!nocash.isChecked());
	}

	@OnClick(R.id.plateDetection)
	public void plateDetectionClicked(){
//		Intent intent = new Intent(this, OcrCaptureActivity.class);
//		intent.putExtra(OcrCaptureActivity.AutoFocus, true);
//		intent.putExtra(OcrCaptureActivity.UseFlash, false);
//		startActivityForResult(intent, RC_OCR_CAPTURE);
	}


	int RC_OCR_CAPTURE = 9999;
	@Subscribe
	public void onAdminClientResponseReceived(AdminClientResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				ClientInfo client = event.getResult().getClientInfo();
				if(!TextUtils.isEmpty(textPrediction) && !TextUtils.isEmpty(client.getCarNo())) {
					ToastUtil.display(this, BA.str(R.string.client_found));
					setData(client);
				}
			}
			else{
				ToastUtil.display(this,BA.str(R.string.client_not_found));
			}
		}
		else{
		}
	}
	String textPrediction;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ActivityForResult.ACTIVITY_SEARCH) {
			if (resultCode == ActivityForResult.ACTIVITY_SEARCH_HAS_RESULT) {
				try {
					ClientInfo client = ClientInfo.deserialize(data.getStringExtra(Constants.CLIENT_INFO));
					setData(client);
				}
				catch(Exception e){}
			}
		}

		if(requestCode == RC_OCR_CAPTURE) {
			if (resultCode == CommonStatusCodes.SUCCESS) {
				if (data != null) {
//					textPrediction = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
					if(!TextUtils.isEmpty(textPrediction)) textPrediction = textPrediction.replaceAll(" ","").trim().replaceAll("\n", "");
					setWaitScreen(true);
					carNo.setText(textPrediction);
					mobile.setText("");
					clientName.setText("");
					washCountCampaignDescription.setVisibility(View.GONE);
					carNo.setSelection(carNo.getText().length());
					carNo.requestFocus();
				} else {
					ToastUtil.display(this,BA.str(R.string.car_number_not_selected));
				}
			} else {
				ToastUtil.display(this,BA.str(R.string.err_process_car_number));
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}


