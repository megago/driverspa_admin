package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.URL_PREFIX;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.driverspa.adapter.GroupServicesGridAdapter;
import com.driverspa.adapter.OfflineBoxesGridAdapter;
import com.driverspa.adapter.ServicesGridAdapter;
import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
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
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Box;
import com.driverspa.model.CarType;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServiceItem;
import com.driverspa.model.WashCountCampaign;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.ws.AdminClientRequestEvent;
import com.driverspa.util.otto.ws.AdminClientResponseEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminStartQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminStartQueueResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableGridView;
import com.driverspa.view.NDSpinner;

public class AdminEditQueuedBookActivity extends AdminBaseActivity {

	Washer washer;
	String boxId;
	BookInfo book;

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
	TextView carNo;
	@BindView(R.id.phone)
	EditText mobile;
	@BindView(R.id.clientName)
	EditText clientName;
//	@BindView(R.id.mobileLayout)
//	View mobileLayout;
	@BindView(R.id.discount)
	EditText discount;
	@BindView(R.id.comment)
	EditText comment;
	@BindView(R.id.carMark)
	EditText carMark;

	@BindView(R.id.txtBookTime)
	TextView txtBookTime;
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

	@BindView(R.id.extra)
	TextView extraButton;
	@BindView(R.id.extraLayout)
	View extraLayout;
	@BindView(R.id.paid)
	CheckBox paid;
	@BindView(R.id.washCountCampaignDescription)
	TextView washCountCampaignDescription;

	@BindView(R.id.nocashLayout)
	View nocashLayout;
	@BindView(R.id.nocash)
	CheckBox nocash;
	@BindView(R.id.discountSpinner)
	NDSpinner spinner;
	@BindView(R.id.discountAmount)
	EditText discountAmount;
	MaskedWatcher maskedWatcher;

	int selectedItem = 0;
	TextWatcher discountAmountWatcher;

	BookingRequest request;
	HashMap<Integer,Double> menuServicesPrices = new HashMap<Integer,Double>();

	@BindView(R.id.grid_group_services)
	ExpandableGridView gridGroupServices;
	@BindView(R.id.grid_services)
	ExpandableGridView gridServices;
	@BindView(R.id.grid_boxes)
	ExpandableGridView gridBoxes;

	private ServicesGridAdapter serviceAdapter;
	private GroupServicesGridAdapter groupServiceAdapter;
	private OfflineBoxesGridAdapter boxAdapter;

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

    	setContentView(R.layout.activity_admin_edit_queue);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
    	ButterKnife.bind(this);

		book = BookInfo.deserialize(getIntent().getStringExtra(Constants.EXTRA_BOOK));

//		groupBtn.setSelected(true);
//		serviceBtn.setSelected(false);
//		groupServiceLayout.setVisibility(View.VISIBLE);
//		serviceLayout.setVisibility(View.GONE);

		titleView.setText("Номер: "+book.getOrder());
		carNo.setText(book.getClientKey().replaceAll("\\(","").replaceAll("\\)",""));
		if(book.getClientKey().contains("(")) {
			carNo.setText(book.getClientKey().substring(0, book.getClientKey().indexOf("(")));
			if (!TextUtils.isEmpty(book.getClientKey()))
				clientName.setText(book.getClientKey().substring(book.getClientKey().indexOf("("), book.getClientKey().length()).replaceAll("\\(", "").replaceAll("\\)", ""));
		}

		try {
			maskedWatcher = new MaskedWatcher("### ###-##-##");
			mobile.addTextChangedListener(maskedWatcher);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if(!TextUtils.isEmpty(book.getMobile()) && book.getMobile().length() > 2) {
				mobile.setText(book.getMobile().substring(2,book.getMobile().length()));
			}

//		mobile.setText(book.getMobile());

//		if(!book.getMobile().equals(UserPreferences.getUserPhone(BA.getContext()))) {
//			mobileLayout.setVisibility(View.VISIBLE);
//			if(!TextUtils.isEmpty(book.getMobile()))
//
//			else
//				mobileLayout.setVisibility(View.GONE);
//		}
//		else
//			mobileLayout.setVisibility(View.GONE);

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
					calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
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
		selectedCarType = CarType.Sedan;

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth()-(int) Functions.dipToPixels(this, 10);

		ViewGroup.LayoutParams layoutParamsServices = gridServices.getLayoutParams();
		layoutParamsServices.width = width;
		gridServices.setLayoutParams(layoutParamsServices);
//		gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 32)) / 4);
		gridGroupServices.setLayoutParams(layoutParamsServices);
//		gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 32)) / 4);

		//set layout params same as grid services
		gridBoxes.setLayoutParams(layoutParamsServices);
//		gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 32)) / 4);

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
//				overridePendingTransition(0,0);
                break;
        }
        return true;
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(0,0);
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

			if(book!=null) {
				AdminSearchFilter filter = new AdminSearchFilter();
				filter.setCarwash(washer.getId());
				filter.setBookId(book.getId());
				BA.getEventBus().post(new AdminClientRequestEvent(filter));
			}
			makeCarTypeButtons(washer.getCarTypes());
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					createService(selectedCarType.getValue());
					createBoxGrid(washer);
				}}, 100);
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
//							if(groupServiceAdapter != null && groupServiceAdapter.getData() != null && groupServiceAdapter.getData().size() > 0) {
//								for (Washer.GroupMenu g : groupServiceAdapter.getData()) {
//									for(String gId : request.getGroupServices()){
//										if(gId.equals(g.getId())){
//											selectedGroup = g;
//											break;
//										}
//									}
//								}
//							}
							if(selectedGroup != null && selectedGroup.getServices()!=null && selectedGroup.getServices().contains(serviceAdapter.getData().get(position).getServiceId())){
								ToastUtil.display(BA.getContext(),"Данная услуга уже содержится в выбранном комплексе услуг");
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
						ToastUtil.display(BA.getContext(),"Для данного типа авто и услуги - не указана цена");
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
//						for(Integer s : request.getServices()){
//							if(selectedGroup.getServices().contains(s)){
//								found = true;
//								break;
//							}
//						}
						if(found) {
							ToastUtil.display(BA.getContext(), "Вы уже выбрали услугу которая содержится в данном комплексе");
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
		txtTime.setText("0 мин");
		request.setServices(new ArrayList<Integer>());
		request.setGroupServices(new ArrayList<String>());
	}

	public void calculatePriceAndTime(ArrayList<ServiceItem> data, ArrayList<Washer.GroupMenu> groupData) {
		txtPrice.setText("0 ₸");
		txtTime.setText("0 мин");
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
			txtTime.setText(Math.round(totalTime) + " мин");
			if (request != null)
				request.setServiceTotalTime(totalTime);
		}
	}

	@OnClick(R.id.btnRejectQueue)
	public void rejectQueueRequest() {
		AlertDialog.Builder dialog = new MaterialAlertDialogBuilder(this);
		dialog.setTitle("Удалить очередь?");
		dialog.setNegativeButton("Нет",null);
		dialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String phoneStr = mobile.getText().toString();
				phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");
				request.setId(book.getId());
				request.setMobile(phoneStr);
				request.setTime(Instant.now());
				request.setCarwash(URL_PREFIX+washer.getId());
				request.setOffline(true);
				request.setQueued(true);
				request.setDirectWash(false);
				BA.getEventBus().post(new AdminRejectQueueRequestEvent(request));
				setWaitScreen(true);
			}
		});
		dialog.show();
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

		String phoneStr = mobile.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

		if(selectedCarType == null) {
			ToastUtil.displayAtTop(this, "Выберите машину");
			return;
		}

		if(!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)){
			ToastUtil.displayAtTop(this, "Выберите услуги");
			return;
		}

		if(TextUtils.isEmpty(boxId)){
			ToastUtil.displayAtTop(this, "Выберите бокс");
			return;
		}

		request.setId(book.getId());
		request.setMobile(TextUtils.isEmpty(phoneStr)?"":"+7"+phoneStr);
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


		BA.getEventBus().post(new AdminStartQueueRequestEvent(request));
		setWaitScreen(true);
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

	@OnClick(R.id.paid_text)
	public void onPaidTextClicked(){
		paid.setChecked(!paid.isChecked());
	}

	@OnClick(R.id.nocashText)
	public void onNocashTextClicked() {
		nocash.setChecked(!nocash.isChecked());
	}


	@Subscribe
	public void onAdminQueueResponseReceived(AdminStartQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
//				ToastUtil.display(this, "Успешно забронировали");
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(this, event.getResult().getMessage());
		}
		else{
			ToastUtil.display(this, "Ошибка при добавлении очереди");
		}
	}

//	@OnClick(R.id.serviceBtn)
//	public void onServiceButtonClicked(){
////		serviceLayout.setVisibility(View.VISIBLE);
//		groupServiceLayout.setVisibility(View.GONE);
//		serviceBtn.setSelected(true);
//		groupBtn.setSelected(false);
//	}
//	@OnClick(R.id.groupBtn)
//	public void onGroupServiceButtonClicked(){
//		serviceLayout.setVisibility(View.GONE);
//		groupServiceLayout.setVisibility(View.VISIBLE);
//		groupBtn.setSelected(true);
//		serviceBtn.setSelected(false);
//	}

	@Subscribe
	public void onAdminRejectQueueResponseReceived(AdminRejectQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
//				ToastUtil.display(this, "Успешно забронировали");
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(this, event.getResult().getMessage());
		}
		else{
			ToastUtil.display(this, "Ошибка при добавлении очереди");
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
			selectedCarType = CarType.Sedan;
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
	public void onAdminClientResponseReceived(AdminClientResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null) {
			if (BaseAssist.isSuccess(event.getResult())) {
				ClientInfo client = event.getResult().getClientInfo();
				analyzeWasherCountCampaign(client);
			}
		}
	}
}
