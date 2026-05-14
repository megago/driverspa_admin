package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.driverspa.adapter.GroupServicesGridAdapter;
import com.driverspa.adapter.ServicesGridAdapter;
import com.driverspa.assist.BaseAssist;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
import com.driverspa.adapter.StaffGridAdapter;
import com.driverspa.model.AdminSearchFilter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.CarType;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServiceItem;
import com.driverspa.model.Staff;
import com.driverspa.model.WashCountCampaign;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.ActivityForResult;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.GroupServiceUnselectClicked;
import com.driverspa.util.otto.SearchEvent;
import com.driverspa.util.otto.ServiceUnselectClicked;
import com.driverspa.util.otto.ws.AdminBookRequestEvent;
import com.driverspa.util.otto.ws.AdminBookResponseEvent;
import com.driverspa.util.otto.ws.AdminClientResponseEvent;
import com.driverspa.util.otto.ws.AdminClientsRequestEvent;
import com.driverspa.util.otto.ws.AdminClientsResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.DelayAutoCompleteTextView;
import com.driverspa.view.ExpandableGridView;
import com.driverspa.view.NDSpinner;

public class AdminAddBookActivity extends AdminBaseActivity {
	    
	Washer washer;
	String boxName;
	String boxId;
	Long gridTime;
	String gridTitle;
	String bookDay;

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

	@BindView(R.id.paid)
	CheckBox paid;

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
	@BindView(R.id.grid_group_services)
	ExpandableGridView gridGroupServices;
	@BindView(R.id.clients)
	TextView clients;
	@BindView(R.id.washCountCampaignDescription)
	TextView washCountCampaignDescription;

	@BindView(R.id.carTypesLayout)
	View carTypesLayout;

	@BindView(R.id.searchLayout)
	View searchLayout;
	@BindView(R.id.carNo)
	DelayAutoCompleteTextView carNo;
	@BindView(R.id.carMark)
	EditText carMark;
	@BindView(R.id.clientName)
	EditText clientName;
	@BindView(R.id.phone)
	EditText mobile;
	@BindView(R.id.comment)
	EditText comment;

	int selectedItem = 0;
	@BindView(R.id.discount)
	EditText discount;
	@BindView(R.id.discountAmount)
	EditText discountAmount;
	@BindView(R.id.discountSpinner)
	NDSpinner spinner;
	@BindView(R.id.searchField)
	DelayAutoCompleteTextView searchField;
	@BindView(R.id.no_staffs)
	View noStaffs;
	@BindView(R.id.selectedClientInformationLayout)
	View selectedClientInformationLayout;
	@BindView(R.id.selectedClientInformationText)
	TextView selectedClientInformationText;

	@BindView(R.id.clientInformationsLayout)
	View clientInformationsLayout;
	@BindView(R.id.grid_staffs)
	ExpandableGridView gridStaffs;
	@BindView(R.id.cashAmount)
	EditText cashAmount;
	@BindView(R.id.cardAmount)
	EditText cardAmount;
	@BindView(R.id.bonusAmount)
	EditText bonusAmount;
	@BindView(R.id.depositAmount)
	EditText depositAmount;
	@BindView(R.id.clientBonusAmount)
	TextView clientBonusAmount;
	@BindView(R.id.clientDepositAmount)
	TextView clientDepositAmount;
	@BindView(R.id.txtPriceDifference)
	TextView txtPriceDifference;
	private StaffGridAdapter staffGridAdapter;
	ClientInfo selectedClient;
	Double finalPrice;
	Double finalPriceDifference;
	List<BookInfo.PriceDetail> priceDetails = new ArrayList<>();
	boolean isSpinnerTouched = false;
	TextWatcher discountAmountWatcher;
	AdminSearchFilter filter = new AdminSearchFilter();
	int RC_OCR_CAPTURE = 9999;

	BookingRequest request;
	HashMap<Integer,Double> menuServicesPrices = new HashMap<Integer,Double>();
	@BindView(R.id.grid_services)
	ExpandableGridView gridServices;
	private ServicesGridAdapter serviceAdapter;
	private GroupServicesGridAdapter groupServiceAdapter;
	MaskedWatcher maskedWatcher;
	private FilterableClientsAdapter clientsAdapter;
	private String queryText = "";

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_add_book);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText("Добавление брони");

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
    	ButterKnife.bind(this);

		try {
			maskedWatcher = new MaskedWatcher("### ###-##-##");
			mobile.addTextChangedListener(maskedWatcher);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		boxName = getIntent().getStringExtra(EXTRA_BOX_NAME);
		boxId = getIntent().getStringExtra(EXTRA_BOX_ID);
		gridTime = getIntent().getLongExtra(GRID_TIME,0);
		gridTitle = getIntent().getStringExtra(GRID_TITLE);
		bookDay = getIntent().getStringExtra(EXTRA_BOOK_DAY);

		titleView.setText(boxName +" - "+ (bookDay.equals(TODAY)?"сегодня в "+gridTitle:"завтра в "+gridTitle));
		txtBookTime.setText(bookDay.equals(TODAY)?"Сегодня в "+gridTitle:"Завтра в "+gridTitle);

		//Deposit and Bonus amounts
		depositAmount.setEnabled(false);
		bonusAmount.setEnabled(false);
		clientDepositAmount.setText("из 0");
		clientBonusAmount.setText("из 0");
		depositAmount.setText("0");
		bonusAmount.setText("0");
		bonusAmount.setSelection(bonusAmount.getText().length());
		depositAmount.setSelection(depositAmount.getText().length());

		cashAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				calculatePaymentTypesTransactionAmount(cashAmount,this);
			}
		});
		cardAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				calculatePaymentTypesTransactionAmount(cardAmount,this);
			}
		});
		bonusAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				calculatePaymentTypesTransactionAmount(bonusAmount,this);
			}
		});
		depositAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				calculatePaymentTypesTransactionAmount(depositAmount,this);
			}
		});


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
					if (value > 100) {
						discount.setText("100");
					}
				} catch (Exception e) {
				}

				if (serviceAdapter != null && serviceAdapter.getData() != null)
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
		selectedCarType = CarType.Sedan;

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth()-(int) Functions.dipToPixels(this, 10);

		ViewGroup.LayoutParams layoutParamsServices = gridServices.getLayoutParams();
		layoutParamsServices.width = width;
		gridServices.setLayoutParams(layoutParamsServices);
		gridGroupServices.setLayoutParams(layoutParamsServices);
		gridStaffs.setLayoutParams(layoutParamsServices);

		boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
		boolean tabletBigSize = getResources().getBoolean(R.bool.isBigTablet);

		if(!tabletSize) {
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
			gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
		}
		else if(!tabletBigSize) {
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
			gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
		}
		else{
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
			gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
		}

		clients.setPaintFlags(clients.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
	}

	protected void calculatePaymentTypesTransactionAmount() {
		double cash = 0, card = 0, bonus = 0, deposit = 0, price = 0;

		try {
			cash = Double.parseDouble(cashAmount.getText().toString());
		} catch (NumberFormatException e) {
			cash = 0;
		}
		try {
			card = Double.parseDouble(cardAmount.getText().toString());
		} catch (NumberFormatException e) {
			card = 0;
		}
		try {
			bonus = Double.parseDouble(bonusAmount.getText().toString());
		} catch (NumberFormatException e) {
			bonus = 0;
		}
		try {
			deposit = Double.parseDouble(depositAmount.getText().toString());
		} catch (NumberFormatException e) {
			deposit = 0;
		}

		finalPriceDifference = finalPrice - cash - card - bonus - deposit;
		txtPriceDifference.setText(finalPriceDifference + " ₸");

		priceDetails.clear();
		if (cash > 0) {
			BookInfo.PriceDetail pdCash = new BookInfo.PriceDetail();
			pdCash.setAmount(cash);
			pdCash.setPaymentType("C");
			priceDetails.add(pdCash);
		}
		if (card > 0) {
			BookInfo.PriceDetail pdCard = new BookInfo.PriceDetail();
			pdCard.setAmount(card);
			pdCard.setPaymentType("CD");
			priceDetails.add(pdCard);
		}
		if (bonus > 0) {
			BookInfo.PriceDetail pdBonus = new BookInfo.PriceDetail();
			pdBonus.setAmount(bonus);
			pdBonus.setPaymentType("B");
			priceDetails.add(pdBonus);
		}
		if (deposit > 0) {
			BookInfo.PriceDetail pdDeposit = new BookInfo.PriceDetail();
			pdDeposit.setAmount(deposit);
			pdDeposit.setPaymentType("DA");
			priceDetails.add(pdDeposit);
		}
		request.setPriceDetails(priceDetails);
	}


	protected void calculatePaymentTypesTransactionAmount(EditText enteringEditText, TextWatcher watcher) {
		double cash = 0, card = 0, bonus = 0, deposit = 0, price = 0;

		try {
			cash = Double.parseDouble(cashAmount.getText().toString());
		} catch (NumberFormatException e) {
			cash = 0;
		}
		try {
			card = Double.parseDouble(cardAmount.getText().toString());
		} catch (NumberFormatException e) {
			card = 0;
		}
		try {
			bonus = Double.parseDouble(bonusAmount.getText().toString());
		} catch (NumberFormatException e) {
			bonus = 0;
		}
		try {
			deposit = Double.parseDouble(depositAmount.getText().toString());
		} catch (NumberFormatException e) {
			deposit = 0;
		}

		finalPriceDifference = finalPrice - cash - card - bonus - deposit;

		if(finalPriceDifference < 0){
			if(enteringEditText != cashAmount) cashAmount.setText("0");
			if(enteringEditText != cardAmount) cardAmount.setText("0");
			if(enteringEditText != bonusAmount) bonusAmount.setText("0");
			if(enteringEditText != depositAmount) depositAmount.setText("0");
		}

		txtPriceDifference.setText(finalPriceDifference + " ₸");

		priceDetails.clear();
		if (cash > 0) {
			BookInfo.PriceDetail pdCash = new BookInfo.PriceDetail();
			pdCash.setAmount(cash);
			pdCash.setPaymentType("C");
			priceDetails.add(pdCash);
		}
		if (card > 0) {
			BookInfo.PriceDetail pdCard = new BookInfo.PriceDetail();
			pdCard.setAmount(card);
			pdCard.setPaymentType("CD");
			priceDetails.add(pdCard);
		}
		if (bonus > 0) {
			BookInfo.PriceDetail pdBonus = new BookInfo.PriceDetail();
			pdBonus.setAmount(bonus);
			pdBonus.setPaymentType("B");
			priceDetails.add(pdBonus);
		}
		if (deposit > 0) {
			BookInfo.PriceDetail pdDeposit = new BookInfo.PriceDetail();
			pdDeposit.setAmount(deposit);
			pdDeposit.setPaymentType("DA");
			priceDetails.add(pdDeposit);
		}
		request.setPriceDetails(priceDetails);
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
			makeCarTypeButtons(washer.getCarTypes());
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					createService(selectedCarType.getValue());
					createStaffGrid(washer);

					carNo.setThreshold(2);
					clientsAdapter = new FilterableClientsAdapter(AdminAddBookActivity.this, new ArrayList<ClientInfo>(), washer);
					carNo.setAdapter(clientsAdapter);
					clientsAdapter.setCarNo(carNo);
					carNo.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_bar));
					carNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
							ClientInfo client = ((ClientInfo) clientsAdapter.getItem(position));
							setData(client);
						}
					});

					searchField.setThreshold(2);
					searchField.setAdapter(clientsAdapter);
					clientsAdapter.setSearchField(searchField);
					searchField.setLoadingIndicator((ProgressBar) findViewById(R.id.search_progress_bar));
					searchField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
							ClientInfo client = ((ClientInfo) clientsAdapter.getItem(position));
							setData(client);
						}
					});

				}}, 50);
		}
	}

	protected void createService(Integer carType) {
		resetPriceAndTime();
		//Services
		ArrayList<ServiceItem> serviceItems = new ArrayList<ServiceItem>();
		final LinkedHashMap<Integer, String> dictionary = BA.getReference().getServices();
		LinkedHashMap<Integer, ArrayList<Washer.Prices>> washerMenu = washer.getMenu();
		if (washerMenu != null && washerMenu.size() > 0) {
			serviceItems.clear();
			menuServicesPrices.clear();
			for (Map.Entry<Integer, ArrayList<Washer.Prices>> entry : washerMenu.entrySet()) {
				Integer key = entry.getKey();
				if (key == carType) {
					ArrayList<Washer.Prices> pricesMenu = entry.getValue();
					for (Washer.Prices priceMenu : pricesMenu) {
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
		if (washerGroup != null && washerGroup.size() > 0) {
			groupServiceItems.clear();
			for (Washer.GroupMenu g : washerGroup) {
				for (ReversePrices p : g.getPrices()) {
					if (p.getCarType() == carType) {
						groupServiceItems.add(g);
					}
				}
			}
		}

		//Service adapter
		serviceAdapter = new ServicesGridAdapter(this, serviceItems);
		gridServices.setAdapter(serviceAdapter);
		gridServices.setExpanded(true);
		gridServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				request.setServices(new ArrayList<Integer>());
				final ArrayList<Integer> requestServices = new ArrayList<Integer>();
				if (serviceAdapter.getSelectedPosition().get(position) != null && serviceAdapter.getSelectedPosition().get(position)) {
					serviceAdapter.setSelectedPosition(position, true);
					serviceAdapter.notifyDataSetChanged();
				} else {
					if (serviceAdapter.getData().get(position).getPrice() > 0) {
						if (request != null && request.getGroupServices() != null && request.getGroupServices().size() > 0) {
							Washer.GroupMenu selectedGroup = null;
							serviceAdapter.setSelectedPosition(position, true);
							serviceAdapter.notifyDataSetChanged();
						} else {
							serviceAdapter.setSelectedPosition(position, true);
							serviceAdapter.notifyDataSetChanged();
						}
					} else {
						ToastUtil.display(BA.getContext(), "Для данного типа авто и услуги - не указана цена");
					}
				}


				for (int i = 0; i < serviceAdapter.getData().size(); i++) {
					if (serviceAdapter.getSelectedPosition().get(i) != null && serviceAdapter.getSelectedPosition().get(i)) {
						for (int j = 0; j < serviceAdapter.getSelectedPositionCount().get(i); j++)
							requestServices.add(serviceAdapter.getData().get(i).getServiceId());
					}
				}
				request.setServices(new ArrayList<Integer>(requestServices));
				requestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
			}
		});

		//Group Service adapter
		groupServiceAdapter = new GroupServicesGridAdapter(this, groupServiceItems);
		gridGroupServices.setAdapter(groupServiceAdapter);
		gridGroupServices.setExpanded(true);
		gridGroupServices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				request.setGroupServices(new ArrayList<String>());
				final ArrayList<String> groupRequestServices = new ArrayList<String>();
				if (groupServiceAdapter.getSelectedPosition().get(position) != null && groupServiceAdapter.getSelectedPosition().get(position)) {
					groupServiceAdapter.setSelectedPosition(position, true);
					groupServiceAdapter.notifyDataSetChanged();
				} else {
					Washer.GroupMenu selectedGroup = groupServiceAdapter.getData().get(position);
					if (selectedGroup != null && selectedGroup.getServices() != null && request != null && request.getServices() != null && request.getServices().size() > 0) {
						boolean found = false;
						groupServiceAdapter.setSelectedPosition(position, true);
						groupServiceAdapter.notifyDataSetChanged();
					} else {
						groupServiceAdapter.setSelectedPosition(position, true);
						groupServiceAdapter.notifyDataSetChanged();
					}

				}

				for (int i = 0; i < groupServiceAdapter.getData().size(); i++) {
					if (groupServiceAdapter.getSelectedPosition().get(i) != null && groupServiceAdapter.getSelectedPosition().get(i)) {
						for (int j = 0; j < groupServiceAdapter.getSelectedPositionCount().get(i); j++)
							groupRequestServices.add(groupServiceAdapter.getData().get(i).getId());
					}
				}
				request.setGroupServices(new ArrayList<String>(groupRequestServices));
				groupRequestServices.clear();
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
			}
		});

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
		} else {
			noService.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			View v = getCurrentFocus();
			if ( v instanceof EditText) {
				Rect outRect = new Rect();
				v.getGlobalVisibleRect(outRect);
				if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
					v.clearFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
			}
		}
		return super.dispatchTouchEvent( event );
	}

	@Subscribe
	public void onServiceUnselectClicked(ServiceUnselectClicked event) {
		int position = event.getPosition();
		closeKeyboard();
		request.setServices(new ArrayList<Integer>());
		final ArrayList<Integer> requestServices = new ArrayList<Integer>();
		if (serviceAdapter.getSelectedPosition().get(position) != null && serviceAdapter.getSelectedPosition().get(position)) {
			serviceAdapter.unSelectSelectedPosition(position);
			serviceAdapter.notifyDataSetChanged();
		}

		for (int i = 0; i < serviceAdapter.getData().size(); i++) {
			if (serviceAdapter.getSelectedPosition().get(i) != null && serviceAdapter.getSelectedPosition().get(i)) {
				for (int j = 0; j < serviceAdapter.getSelectedPositionCount().get(i); j++)
					requestServices.add(serviceAdapter.getData().get(i).getServiceId());
			}
		}
		request.setServices(new ArrayList<Integer>(requestServices));
		requestServices.clear();
		calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
	}

	@Subscribe
	public void onGroupServiceUnselectClicked(GroupServiceUnselectClicked event) {
		int position = event.getPosition();
		closeKeyboard();
		request.setGroupServices(new ArrayList<String>());
		final ArrayList<String> groupRequestServices = new ArrayList<String>();
		if (groupServiceAdapter.getSelectedPosition().get(position) != null && groupServiceAdapter.getSelectedPosition().get(position)) {
			groupServiceAdapter.unSelectSelectedPosition(position);
			groupServiceAdapter.notifyDataSetChanged();
		}

		for (int i = 0; i < groupServiceAdapter.getData().size(); i++) {
			if (groupServiceAdapter.getSelectedPosition().get(i) != null && groupServiceAdapter.getSelectedPosition().get(i)) {
				for (int j = 0; j < groupServiceAdapter.getSelectedPositionCount().get(i); j++)
					groupRequestServices.add(groupServiceAdapter.getData().get(i).getId());
			}
		}
		request.setGroupServices(new ArrayList<String>(groupRequestServices));
		groupRequestServices.clear();
		calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
	}

	protected void createStaffGrid(Washer washer) {
		//Staffs
		ArrayList<Staff> items = new ArrayList<Staff>();
		if (washer != null && washer.getStaffs() != null && washer.getStaffs().size() > 0)
			for (Staff item : washer.getStaffs()) {
				if (Constants.WORKS.equals(item.getStatus())) {
					items.add(item);
				}
			}

		//Box adapter
		staffGridAdapter = new StaffGridAdapter(this, items);
		gridStaffs.setAdapter(staffGridAdapter);
		gridStaffs.setExpanded(true);
		gridStaffs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				closeKeyboard();
				final ArrayList<String> requestStaffs = new ArrayList<String>();

				if (staffGridAdapter.getSelectedPosition().get(position) != null && staffGridAdapter.getSelectedPosition().get(position)) {
					staffGridAdapter.setSelectedPosition(position, false);
					staffGridAdapter.notifyDataSetChanged();
				} else {
					staffGridAdapter.setSelectedPosition(position, true);
					staffGridAdapter.notifyDataSetChanged();
					staffGridAdapter.notifyDataSetChanged();
				}

				for (int i = 0; i < staffGridAdapter.getData().size(); i++) {
					if (staffGridAdapter.getSelectedPosition().get(i) != null && staffGridAdapter.getSelectedPosition().get(i)) {
						requestStaffs.add(staffGridAdapter.getData().get(i).getUid());
					}
				}
				request.setStaffs(new ArrayList<String>(requestStaffs));
				requestStaffs.clear();
			}
		});

		if (items.size() == 0) {
			gridStaffs.setVisibility(View.GONE);
			noStaffs.setVisibility(View.VISIBLE);
		} else {
			gridStaffs.setVisibility(View.VISIBLE);
			noStaffs.setVisibility(View.GONE);
		}
	}

	protected void resetPriceAndTime() {
		txtPrice.setText("0 ₸");
		finalPrice = 0d;
		cashAmount.setText(finalPrice.intValue() + "");
		cashAmount.setSelection(cashAmount.getText().toString().length());
		txtTime.setText("0 мин");
		request.setServices(new ArrayList<Integer>());
		request.setGroupServices(new ArrayList<String>());
		cashAmount.setText("0");
		cardAmount.setText("0");
		bonusAmount.setText("0");
		depositAmount.setText("0");
		calculatePaymentTypesTransactionAmount();
	}

	public void calculatePriceAndTime(ArrayList<ServiceItem> data, ArrayList<Washer.GroupMenu> groupData) {
		cashAmount.setText("0");
		cashAmount.setSelection(cashAmount.getText().toString().length());
		cardAmount.setText("0");
		bonusAmount.setText("0");
		depositAmount.setText("0");
		txtPrice.setText("0 ₸");
		finalPrice = 0d;
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
			if (selectedItem == 0) { //discount type is %
				int discnt = 0;
				if (!TextUtils.isEmpty(discount.getText().toString()))
					discnt = Integer.parseInt(discount.getText().toString());
				double totalPriceWithDiscount = (int) (Math.round(totalPrice) - Math.round(totalPrice) * discnt / 100);
				txtPrice.setText(totalPriceWithDiscount + " ₸");
				finalPrice = totalPriceWithDiscount;
				cashAmount.setText(finalPrice.intValue() + "");
				cashAmount.setSelection(cashAmount.getText().toString().length());
				if (request != null)
					request.setServicePrice(totalPrice + "");
			} else {
				int discnt = 0;
				if (!TextUtils.isEmpty(discountAmount.getText().toString())) {
					try {
						discnt = Integer.parseInt(discountAmount.getText().toString());
					} catch (Exception e) {
					}
				}
				double totalPriceWithDiscount = (int) (Math.round(totalPrice) - discnt);
				if (totalPriceWithDiscount < 0) {
					discountAmount.removeTextChangedListener(discountAmountWatcher);
					discountAmount.setText((int) totalPrice + "");
					discountAmount.setSelection(discountAmount.getText().toString().length());
					discountAmount.addTextChangedListener(discountAmountWatcher);
					totalPriceWithDiscount = 0;
				}
				finalPrice = totalPriceWithDiscount;
				cashAmount.setText(finalPrice.intValue() + "");
				cashAmount.setSelection(cashAmount.getText().toString().length());
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
		calculatePaymentTypesTransactionAmount();
	}


	protected void selectDiscountType() {
		if (selectedItem == 0) {
			discount.setText("0");
			discountAmount.setText("0");
			discount.setVisibility(View.VISIBLE);
			discountAmount.setVisibility(View.GONE);

		} else {
			discount.setText("0");
			discountAmount.setText("0");
			discount.setVisibility(View.GONE);
			discountAmount.setVisibility(View.VISIBLE);
		}
		if (serviceAdapter != null && serviceAdapter.getData() != null)
			calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
	}


	protected void createActionBarSpinner() {
		if (spinner != null) {
			String[] valuesArray = new String[]{"%     ", "₸     "};
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.navigation_spinner_item, valuesArray);
			adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (!isSpinnerTouched) return;
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

	@OnClick(R.id.btnBook)
	public void createBookingRequest(){
		calculatePaymentTypesTransactionAmount();

		if(selectedCarType == null) {
			ToastUtil.displayAtTop(this, "Выберите машину");
			return;
		}
		//Requesting for company book
		String phoneStr = mobile.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

		String clientNameStr = clientName.getText().toString();
		String сarNoStr = carNo.getText().toString();
		String сarMarkStr = carMark.getText().toString();

		if (selectedClient == null) {
			if (TextUtils.isEmpty(сarNoStr)) {
				ToastUtil.displayAtTop(this, "Введите гос.номер машины");
				carNo.setError("");
				carNo.requestFocus();
				return;
			}
			request.setCarMark(carMark.getText().toString());
		} else {
			сarNoStr = selectedClient.getCarNo();
			if (!TextUtils.isEmpty(selectedClient.getPhone()) && selectedClient.getPhone().length() > 3) {
				phoneStr = selectedClient.getPhone().substring(2);
			}
			request.setCarMark(selectedClient.getCarMark());
		}

		if(!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)){
			ToastUtil.displayAtTop(this, "Выберите услуги");
			return;
		}

		if (finalPriceDifference != 0) {
			ToastUtil.displayAtTop(this, "Остаток не равен нулю! Распределите суммы правильно!");
			return;
		}

		request.setMobile(TextUtils.isEmpty(phoneStr) ? "" : "+7" + phoneStr);
		request.setCarMark(сarMarkStr);
		request.setCarNo(сarNoStr);
		request.setClientName(clientNameStr);
		if (!TextUtils.isEmpty(clientNameStr)) {
					request.setClientKey(сarNoStr + " (" + clientNameStr + ")");
		}
		else
			request.setClientKey(сarNoStr);
		request.setCarType(selectedCarType.getValue());
        request.setBoxId(boxId);
		request.setPaid(paid.isChecked());
		request.setTime(new Date(gridTime));
		request.setCarwash(URL_PREFIX+washer.getId());
		request.setOffline(true);

		if (selectedItem == 1) {
			try {
				double value = Double.parseDouble(discountAmount.getText().toString());
				request.setDiscountAmount(value);
			} catch (NumberFormatException e) {
				request.setDiscountAmount(0d);
			}
		} else {
			request.setDiscountAmount(0d);
		}

		if (selectedItem == 0) {
			try {
				request.setDiscount(Double.parseDouble(discount.getText().toString()));
			} catch (NumberFormatException e) {
				request.setDiscount(0d);
			}
		} else {
			request.setDiscount(0d);
		}

		request.setComment(comment.getText().toString());

		BA.getEventBus().post(new AdminBookRequestEvent(request));
		setWaitScreen(true);
	}

	@Subscribe
	public void onAdminBookResponseReceived(AdminBookResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				ToastUtil.display(this, "Успешно забронировали");
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(this, event.getResult().getMessage());
		}
		else{
			ToastUtil.display(this, "Ошибка при брони");
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


	@OnClick(R.id.client_layout)
	public void onCLientLayoutClicked() {
		Intent intent = new Intent(this, ClientSearchActivity.class);
		intent.putExtra(OPENING_ANIMATION,false);
		startActivityForResult(intent, ActivityForResult.ACTIVITY_SEARCH);
	}


	protected void setData(ClientInfo client) {
		this.selectedClient = client;
		request.setCompanyClient(URL_COMPANY_CLIENT_PREFIX + client.getId());
		if(selectedClient.getDiscount() > 0){
			discount.setText(selectedClient.getDiscount()+"");
			spinner.setVisibility(View.GONE);
			discount.setEnabled(false);
			if (serviceAdapter != null && serviceAdapter.getData() != null)
				calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
		}
		String clientInformationText = "";

		if (!TextUtils.isEmpty(client.getName())) {
			clientInformationText = client.getName().trim();
		} else {
			clientInformationText = "";
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			if (!TextUtils.isEmpty(filter.getCarNo())) {
				carNo.setText(filter.getCarNo(), false);
				carNo.setSelection(carNo.getText().length());
			}

			if (!TextUtils.isEmpty(filter.getSearchText())) {
				searchField.setText(filter.getSearchText(), false);
				searchField.setSelection(searchField.getText().length());
			}
		} else {
			if (!TextUtils.isEmpty(filter.getCarNo())) {
				carNo.setText(carNo.getText().toString());
				carNo.setSelection(carNo.getText().length());
			}

			if (!TextUtils.isEmpty(filter.getSearchText())) {
				searchField.setText(filter.getSearchText());
				searchField.setSelection(searchField.getText().length());
			}
		}

		if (!TextUtils.isEmpty(client.getCarNo(queryText))) {
			if (client.getCarNo().contains("(")) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
					clientInformationText = clientInformationText + " " + client.getCarNo(queryText).substring(0, client.getCarNo(queryText).indexOf("(")).trim();
				} else {
					clientInformationText = clientInformationText + " " + client.getCarNo(queryText).substring(0, client.getCarNo(queryText).indexOf("(")).trim();
				}

				if (TextUtils.isEmpty(client.getName())) {
					clientInformationText = client.getCarNo(queryText).substring(client.getCarNo(queryText).indexOf("("), client.getCarNo(queryText).length()).replaceAll("\\(", "").replaceAll("\\)", "").trim() + " " + clientInformationText;
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
					clientInformationText = clientInformationText + " " + client.getCarNo(queryText).trim();
				} else {
					clientInformationText = clientInformationText + " " + client.getCarNo(queryText).trim();
				}
			}
		}

		if (!TextUtils.isEmpty(client.getPhone()) && client.getPhone().length() > 2) {
			clientInformationText = clientInformationText + " " + client.getPhone();
		}

		if (client.getCarType(queryText) != null) {
			selectedCarType = CarType.toEnum(client.getCarType(queryText));
			if (selectedCarType != null && selectedCarType != CarType.Unknown) {
				switch (selectedCarType.getValue()) {
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

		selectedClientInformationLayout.setVisibility(View.VISIBLE);
		clientInformationsLayout.setVisibility(View.GONE);
		selectedClientInformationText.setText(clientInformationText);

		clientDepositAmount.setText("из " + client.getDepositAmount());
		clientBonusAmount.setText("из " + client.getAccruedBonuses());
		depositAmount.setText("0");
		bonusAmount.setText("0");
		bonusAmount.setSelection(bonusAmount.getText().length());
		depositAmount.setSelection(depositAmount.getText().length());
		depositAmount.setEnabled(true);
		bonusAmount.setEnabled(true);
		closeKeyboard();
		queryText = "";
	}


	@OnClick(R.id.closeClient)
	public void onCloseClientRequested() {
		selectedClientInformationLayout.setVisibility(View.GONE);
		clientInformationsLayout.setVisibility(View.VISIBLE);
		selectedClientInformationText.setText("");
		selectedClient = null;
		request.setCompanyClient("");
		depositAmount.setEnabled(false);
		bonusAmount.setEnabled(false);
		clientDepositAmount.setText("из 0");
		clientBonusAmount.setText("из 0");
		depositAmount.setText("0");
		bonusAmount.setText("0");
		bonusAmount.setSelection(bonusAmount.getText().length());
		depositAmount.setSelection(depositAmount.getText().length());

		spinner.setVisibility(View.VISIBLE);
		discount.setText("0");
		discount.setEnabled(true);
		if (serviceAdapter != null && serviceAdapter.getData() != null)
			calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
	}

	@Subscribe
	public void onAdminClientsReceived(final AdminClientsResponseEvent event) {
		setWaitScreen(false);

		if (carNo != null) carNo.stopLoading();
		if (searchField != null) searchField.stopLoading();

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
					if (!TextUtils.isEmpty(filter.getSearchText()))
						clientsAdapter.setSearchedPossibleCarNo(filter.getSearchText() != null ? filter.getSearchText() : "");
					else
						clientsAdapter.setSearchedPossibleCarNo(filter.getCarNo() != null ? filter.getCarNo() : "");
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
	public void onSearchSubmitted(SearchEvent event) {
		if (event != null && event.getQuery() != null && filter != null && washer != null) {
			queryText = event.getQuery();
			if (clientsAdapter != null && carNo != null && searchField != null) {
				if (event.getQuery().equals(carNo.getText().toString())) {
					filter.setSearchText("");
					filter.setCarNo(event.getQuery());
				} else if (event.getQuery().equals(searchField.getText().toString())) {
					filter.setSearchText(event.getQuery());
					filter.setCarNo("");
				}

			} else filter.setSearchText(event.getQuery());
			filter.setCarwash(washer.getId());
			filter.setOffset(Integer.toString(0));
			filter.setClientType("all");
			BA.getEventBus().post(new AdminClientsRequestEvent(filter));
		}
	}



	@OnClick(R.id.plateDetection)
	public void plateDetectionClicked(){
//		Intent intent = new Intent(this, OcrCaptureActivity.class);
//		intent.putExtra(OcrCaptureActivity.AutoFocus, true);
//		intent.putExtra(OcrCaptureActivity.UseFlash, false);
//		startActivityForResult(intent, RC_OCR_CAPTURE);
	}

	@Subscribe
	public void onAdminClientResponseReceived(AdminClientResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				ClientInfo client = event.getResult().getClientInfo();
				if(!TextUtils.isEmpty(textPrediction) && !TextUtils.isEmpty(client.getCarNo())) {
					ToastUtil.display(this, "Клиент найден в базе");
					setData(client);
				}
			}
			else{
				ToastUtil.display(this,"Клиент не найден в базе");
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
					ToastUtil.display(this,"Номер машины не выбран");
				}
			} else {
				ToastUtil.display(this,"Ошибка при обработке номера машин");
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@OnFocusChange(R.id.discount)
	public void onFocusChangeDiscount(boolean focus) {
		if (!focus) {
			if (TextUtils.isEmpty(discount.getText().toString()))
				discount.setText("0");
			else {
				try {
					int value = Integer.parseInt(discount.getText().toString());
					if (value > 100) {
						discount.setText("100");
					}
				} catch (Exception e) {
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

	@OnFocusChange(R.id.bonusAmount)
	public void onFocusChangeBonus(boolean focus) {
		if (!focus) {
			if (TextUtils.isEmpty(bonusAmount.getText().toString()))
				bonusAmount.setText("0");
			calculatePaymentTypesTransactionAmount();
		} else {
			if (!TextUtils.isEmpty(bonusAmount.getText().toString()) && bonusAmount.getText().toString().equals("0")) {
				bonusAmount.setText("");
			}
		}
	}

	@OnFocusChange(R.id.depositAmount)
	public void onFocusChangeDeposit(boolean focus) {
		if (!focus) {
			if (TextUtils.isEmpty(depositAmount.getText().toString()))
				depositAmount.setText("0");
			calculatePaymentTypesTransactionAmount();
		} else {
			if (!TextUtils.isEmpty(depositAmount.getText().toString()) && depositAmount.getText().toString().equals("0")) {
				depositAmount.setText("");
			}
		}
	}

	@OnFocusChange(R.id.cashAmount)
	public void onFocusChangeCash(boolean focus) {
		if (!focus) {
			if (TextUtils.isEmpty(cashAmount.getText().toString()))
				cashAmount.setText("0");
			calculatePaymentTypesTransactionAmount();
			cashAmount.clearFocus();
		} else {
			if (!TextUtils.isEmpty(cashAmount.getText().toString()) && cashAmount.getText().toString().equals("0")) {
				cashAmount.setText("");
			}
		}

	}

	@OnFocusChange(R.id.cardAmount)
	public void onFocusChangeCard(boolean focus) {
		if (!focus) {
			if (TextUtils.isEmpty(cardAmount.getText().toString()))
				cardAmount.setText("0");
			calculatePaymentTypesTransactionAmount();
		} else {
			if (!TextUtils.isEmpty(cardAmount.getText().toString()) && cardAmount.getText().toString().equals("0")) {
				cardAmount.setText("");
			}
		}
	}
}
