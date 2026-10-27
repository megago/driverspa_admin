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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
	private StaffGridAdapter staffGridAdapter;
	ClientInfo selectedClient;
	Double finalPrice;
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

	// --- Wizard widgets (looked up via findViewById; optional in older layouts) ---
	private android.widget.Button btnBack, btnNext, buttonBook;
	private View stepHeader1, stepHeader2, stepHeader3;
	private View stepDot1, stepDot2, stepDot3;
	private TextView stepLabel1, stepLabel2, stepLabel3;
	private View stepConnector12, stepConnector23;
	private View stepCarHost, stepServicesHost, stepPaymentHost;

	private static final int STEP_CAR = 0;
	private static final int STEP_SERVICES = 1;
	private static final int STEP_PAYMENT = 2;

	private int currentStep = STEP_CAR;
	private boolean settingCarTypeFromClient = false;

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
    	setContentView(R.layout.activity_admin_add_book);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
		titleView.setText(BA.str(R.string.add_booking));

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

		titleView.setText(boxName +" - "+ (bookDay.equals(TODAY)?BA.str(R.string.today_at_lc)+gridTitle:BA.str(R.string.tomorrow_at_lc)+gridTitle));
		txtBookTime.setText(bookDay.equals(TODAY)?BA.str(R.string.today_at)+gridTitle:BA.str(R.string.tomorrow_at)+gridTitle);


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

		setupWizard();
	}

	// -------------------------------------------------------------------------
	// Wizard (3-step flow): Авто → Услуги → Оплата
	// -------------------------------------------------------------------------

	private void setupWizard() {
		btnBack = findViewById(R.id.btnBack);
		btnNext = findViewById(R.id.btnNext);
		buttonBook = findViewById(R.id.btnBook);
		stepHeader1 = findViewById(R.id.stepHeader1);
		stepHeader2 = findViewById(R.id.stepHeader2);
		stepHeader3 = findViewById(R.id.stepHeader3);
		stepDot1 = findViewById(R.id.stepDot1);
		stepDot2 = findViewById(R.id.stepDot2);
		stepDot3 = findViewById(R.id.stepDot3);
		stepLabel1 = findViewById(R.id.stepLabel1);
		stepLabel2 = findViewById(R.id.stepLabel2);
		stepLabel3 = findViewById(R.id.stepLabel3);
		stepConnector12 = findViewById(R.id.stepConnector12);
		stepConnector23 = findViewById(R.id.stepConnector23);
		stepCarHost = findViewById(R.id.stepCarHost);
		stepServicesHost = findViewById(R.id.stepServicesHost);
		stepPaymentHost = findViewById(R.id.stepPaymentHost);

		// Booking time slot shown as a banner in the car step.
		if (txtBookTime != null) txtBookTime.setVisibility(View.VISIBLE);

		if (btnBack != null) {
			btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { onWizardBack(); }
			});
		}
		if (btnNext != null) {
			btnNext.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { onWizardNext(); }
			});
		}
		if (stepHeader1 != null) {
			stepHeader1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { showStep(STEP_CAR); }
			});
		}
		if (stepHeader2 != null) {
			stepHeader2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { showStep(STEP_SERVICES); }
			});
		}
		if (stepHeader3 != null) {
			stepHeader3.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { showStep(STEP_PAYMENT); }
			});
		}

		showStep(STEP_CAR);
	}

	private void onWizardNext() {
		if (currentStep < STEP_PAYMENT) showStep(currentStep + 1);
		else createBookingRequest();
	}

	private void onWizardBack() {
		if (currentStep > STEP_CAR) showStep(currentStep - 1);
		else { finish(); overridePendingTransitionWithCommonCloseTransition(); }
	}

	private void showStep(int step) {
		currentStep = step;
		if (stepCarHost != null) stepCarHost.setVisibility(step == STEP_CAR ? View.VISIBLE : View.GONE);
		if (stepServicesHost != null) stepServicesHost.setVisibility(step == STEP_SERVICES ? View.VISIBLE : View.GONE);
		if (stepPaymentHost != null) stepPaymentHost.setVisibility(step == STEP_PAYMENT ? View.VISIBLE : View.GONE);

		applyHeaderState(stepDot1, stepLabel1, step == STEP_CAR);
		applyHeaderState(stepDot2, stepLabel2, step == STEP_SERVICES);
		applyHeaderState(stepDot3, stepLabel3, step == STEP_PAYMENT);

		boolean isLast = (step == STEP_PAYMENT);
		if (btnNext != null) btnNext.setVisibility(isLast ? View.GONE : View.VISIBLE);
		if (buttonBook != null) buttonBook.setVisibility(isLast ? View.VISIBLE : View.GONE);

	}

	private void applyHeaderState(View dot, TextView label, boolean selected) {
		if (dot != null) {
			dot.setActivated(!selected);
			dot.setSelected(selected);
		}
		if (label != null) {
			if (selected) {
				label.setTextColor(getResources().getColor(R.color.White));
				label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
			} else {
				label.setTextColor(getResources().getColor(R.color.BlueLight));
				label.setTypeface(android.graphics.Typeface.DEFAULT);
			}
		}
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
						ToastUtil.display(BA.getContext(), BA.str(R.string.err_no_price_for_type));
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
		txtTime.setText(BA.str(R.string.zero_min));
		request.setServices(new ArrayList<Integer>());
		request.setGroupServices(new ArrayList<String>());
	}

	public void calculatePriceAndTime(ArrayList<ServiceItem> data, ArrayList<Washer.GroupMenu> groupData) {
		txtPrice.setText("0 ₸");
		finalPrice = 0d;
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
			if (selectedItem == 0) { //discount type is %
				int discnt = 0;
				if (!TextUtils.isEmpty(discount.getText().toString()))
					discnt = Integer.parseInt(discount.getText().toString());
				double totalPriceWithDiscount = (int) (Math.round(totalPrice) - Math.round(totalPrice) * discnt / 100);
				txtPrice.setText(totalPriceWithDiscount + " ₸");
				finalPrice = totalPriceWithDiscount;
				// Leave payment amounts at 0 so the difference shows the amount still owed.
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

		if(selectedCarType == null) {
			ToastUtil.displayAtTop(this, BA.str(R.string.select_car));
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
				ToastUtil.displayAtTop(this, BA.str(R.string.enter_car_plate));
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
			ToastUtil.displayAtTop(this, BA.str(R.string.select_services));
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
		// Paid when it is finished: "Завершить" opens the payment window (PaymentFlow).
		request.setPaid(false);
		request.setNocash(false);
		request.setPriceDetails(new ArrayList<BookInfo.PriceDetail>());
		request.setTime(Instant.ofEpochMilli(gridTime));
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
				ToastUtil.display(this, BA.str(R.string.booked_ok));
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(this, event.getResult().getMessage());
		}
		else{
			ToastUtil.display(this, BA.str(R.string.err_booking));
		}
	}


	private View.OnClickListener buttonSelector = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (selectedClient != null && !settingCarTypeFromClient) {
				ToastUtil.displayAtTop(AdminAddBookActivity.this,
						BA.str(R.string.cant_change_cartype_here));
				return;
			}
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

		settingCarTypeFromClient = true;
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

		settingCarTypeFromClient = false;

		selectedClientInformationLayout.setVisibility(View.VISIBLE);
		clientInformationsLayout.setVisibility(View.GONE);
		selectedClientInformationText.setText(clientInformationText);

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

}
