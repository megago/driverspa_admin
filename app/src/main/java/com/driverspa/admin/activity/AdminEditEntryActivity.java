package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.URL_PREFIX;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TextView;

import com.driverspa.adapter.GroupServicesGridAdapter;
import com.driverspa.adapter.OfflineBoxesGridAdapter;
import com.driverspa.adapter.ServicesGridAdapter;
import com.driverspa.assist.BaseAssist;
import com.splunk.mint.Mint;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
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
import com.driverspa.adapter.StaffGridAdapter;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Box;
import com.driverspa.model.CarType;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServiceItem;
import com.driverspa.model.Staff;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.util.Constants;
import com.driverspa.util.Functions;
import com.driverspa.util.MaskedWatcher;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.UserPreferences;
import com.driverspa.util.otto.GroupServiceUnselectClicked;
import com.driverspa.util.otto.ServiceUnselectClicked;
import com.driverspa.util.otto.ws.AdminBookInfoRequestEvent;
import com.driverspa.util.otto.ws.AdminBookInfoResponseEvent;
import com.driverspa.util.otto.ws.AdminEditQueueOrBookRequestEvent;
import com.driverspa.util.otto.ws.AdminEditQueueOrBookResponseEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.view.ExpandableGridView;
import com.driverspa.view.NDSpinner;

public class AdminEditEntryActivity extends AdminBaseActivity {

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
	@BindView(R.id.clientName)
	EditText clientName;
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
	@BindView(R.id.nocashLayout)
	View nocashLayout;
	@BindView(R.id.nocash)
	CheckBox nocash;
	@BindView(R.id.discountSpinner)
	NDSpinner spinner;
	@BindView(R.id.discountAmount)
	EditText discountAmount;

	int selectedItem = 0;
	TextWatcher discountAmountWatcher;

	@BindView(R.id.clientInformationsLayout)
	View clientInformationsLayout;

	@BindView(R.id.no_staffs)
	View noStaffs;
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
	Double finalPrice;
	Double finalPriceDifference;

	// Payment-method chips (same flow/design as AdminAddQueuedBookActivity)
	private View singleAmountLayout, splitAmountLayout;
	private EditText singleAmount;
	private View chipCash, chipCard, chipBonus, chipDeposit, chipSplit;
	/** 0=cash,1=card,2=bonus,3=deposit, -1=split. */
	private int currentPaymentField = 0;
	/** Guards mirroring between singleAmount and the four backing fields. */
	private boolean amountSyncing = false;
	@BindView(R.id.carTypesLayout)
	View carTypesLayout;
	@BindView(R.id.boxesLayout)
	View boxesLayout;
	@BindView(R.id.paidLayout)
	View paidLayout;

	List<BookInfo.PriceDetail> priceDetails = new ArrayList<>();

	BookingRequest request;
	HashMap<Integer,Double> menuServicesPrices = new HashMap<Integer,Double>();

	@BindView(R.id.grid_group_services)
	ExpandableGridView gridGroupServices;
	@BindView(R.id.grid_services)
	ExpandableGridView gridServices;
	@BindView(R.id.grid_boxes)
	ExpandableGridView gridBoxes;
	boolean allowTypeChange = false;
	private ServicesGridAdapter serviceAdapter;
	private GroupServicesGridAdapter groupServiceAdapter;
	private OfflineBoxesGridAdapter boxAdapter;
	MaskedWatcher maskedWatcher;
	@BindView(R.id.selectedClientInformationLayout)
	View selectedClientInformationLayout;
	@BindView(R.id.selectedClientInformationText)
	TextView selectedClientInformationText;
	boolean isSpinnerTouched = false;
	ClientInfo selectedClient;

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

	@Override
    protected void onCreate(Bundle arg0) {
    	super.onCreate(arg0);
		Mint.initAndStartSession(this.getApplication(), "fa7fe554");
		Mint.setUserIdentifier(UserPreferences.getUserPhone(this));

    	setContentView(R.layout.activity_admin_edit_entry);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);

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

		spinner.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isSpinnerTouched = true;
				return false;
			}
		});


		book = BookInfo.deserialize(getIntent().getStringExtra(Constants.EXTRA_BOOK));

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
					if(value > 100){
						discount.setText("100");
					}
				}
				catch(Exception e){}
				if(serviceAdapter != null && serviceAdapter.getData() != null && groupServiceAdapter != null && groupServiceAdapter.getData() != null)
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
				if(serviceAdapter != null && serviceAdapter.getData() != null && groupServiceAdapter != null && groupServiceAdapter.getData() != null)
					calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
			}
		};

		discountAmount.addTextChangedListener(discountAmountWatcher);
		createActionBarSpinner();

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

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth()-(int) Functions.dipToPixels(this, 10);

		ViewGroup.LayoutParams layoutParamsServices = gridServices.getLayoutParams();
		layoutParamsServices.width = width;
		gridServices.setLayoutParams(layoutParamsServices);
		gridGroupServices.setLayoutParams(layoutParamsServices);

		//set layout params same as grid services
		gridBoxes.setLayoutParams(layoutParamsServices);
		gridStaffs.setLayoutParams(layoutParamsServices);

		boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
		boolean tabletBigSize = getResources().getBoolean(R.bool.isBigTablet);

		if(!tabletSize) {
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
			gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
			gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
		}
		else if(!tabletBigSize) {
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
			gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
			gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
		}
		else{
			gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
			gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
			gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
			gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
		}

		singleAmountLayout = findViewById(R.id.singleAmountLayout);
		splitAmountLayout = findViewById(R.id.splitAmountLayout);
		singleAmount = findViewById(R.id.singleAmount);
		chipCash = findViewById(R.id.chipCash);
		chipCard = findViewById(R.id.chipCard);
		chipBonus = findViewById(R.id.chipBonus);
		chipDeposit = findViewById(R.id.chipDeposit);
		chipSplit = findViewById(R.id.chipSplit);
		setupPaymentChips();
	}

	private void setupPaymentChips() {
		View.OnClickListener chipListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				if (id == R.id.chipCash) selectPaymentChip(0);
				else if (id == R.id.chipCard) selectPaymentChip(1);
				else if (id == R.id.chipBonus) selectPaymentChip(2);
				else if (id == R.id.chipDeposit) selectPaymentChip(3);
				else if (id == R.id.chipSplit) selectPaymentChip(-1);
			}
		};
		if (chipCash != null) chipCash.setOnClickListener(chipListener);
		if (chipCard != null) chipCard.setOnClickListener(chipListener);
		if (chipBonus != null) chipBonus.setOnClickListener(chipListener);
		if (chipDeposit != null) chipDeposit.setOnClickListener(chipListener);
		if (chipSplit != null) chipSplit.setOnClickListener(chipListener);

		if (singleAmount != null) {
			singleAmount.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				@Override
				public void afterTextChanged(Editable s) {
					if (amountSyncing) return;
					String v = s.toString();
					if (v.length() == 0) v = "0";
					mirrorSingleAmount(v);
				}
			});
		}
		// Visual default only; do not mirror until finalPrice is populated.
		selectPaymentChip(0, false);
	}

	/**
	 * Routes the value entered in the single "Сумма" field into the underlying
	 * cash/card/bonus/deposit EditTexts that the existing price logic watches.
	 */
	private void mirrorSingleAmount(String value) {
		if (finalPrice == null) return;
		amountSyncing = true;
		try {
			if (cashAmount != null) cashAmount.setText(currentPaymentField == 0 ? value : "0");
			if (cardAmount != null) cardAmount.setText(currentPaymentField == 1 ? value : "0");
			if (bonusAmount != null) bonusAmount.setText(currentPaymentField == 2 ? value : "0");
			if (depositAmount != null) depositAmount.setText(currentPaymentField == 3 ? value : "0");
		} finally {
			amountSyncing = false;
		}
	}

	private void selectPaymentChip(int idx) {
		selectPaymentChip(idx, true);
	}

	private void selectPaymentChip(int idx, boolean syncAmounts) {
		currentPaymentField = idx;
		if (chipCash != null) chipCash.setSelected(idx == 0);
		if (chipCard != null) chipCard.setSelected(idx == 1);
		if (chipBonus != null) chipBonus.setSelected(idx == 2);
		if (chipDeposit != null) chipDeposit.setSelected(idx == 3);
		if (chipSplit != null) chipSplit.setSelected(idx == -1);

		boolean split = (idx == -1);
		if (singleAmountLayout != null) singleAmountLayout.setVisibility(split ? View.GONE : View.VISIBLE);
		if (splitAmountLayout != null) splitAmountLayout.setVisibility(split ? View.VISIBLE : View.GONE);

		if (syncAmounts && !split && singleAmount != null) {
			String v = singleAmount.getText() != null ? singleAmount.getText().toString() : "0";
			if (v.length() == 0) v = "0";
			mirrorSingleAmount(v);
		}
	}

	/**
	 * Picks the matching chip from the already-loaded cash/card/bonus/deposit
	 * fields: a single non-zero type selects that chip (with its value shown in
	 * the single "Сумма" field); two or more selects the split view.
	 */
	private void syncPaymentChipsFromAmounts() {
		int cash = parseAmount(cashAmount);
		int card = parseAmount(cardAmount);
		int bonus = parseAmount(bonusAmount);
		int deposit = parseAmount(depositAmount);
		int nonZero = (cash > 0 ? 1 : 0) + (card > 0 ? 1 : 0) + (bonus > 0 ? 1 : 0) + (deposit > 0 ? 1 : 0);

		if (nonZero > 1) {
			selectPaymentChip(-1, false);
			return;
		}

		int idx = 0;
		int val = cash;
		if (card > 0) { idx = 1; val = card; }
		else if (bonus > 0) { idx = 2; val = bonus; }
		else if (deposit > 0) { idx = 3; val = deposit; }
		else if (cash > 0) { idx = 0; val = cash; }

		selectPaymentChip(idx, false);
		if (singleAmount != null) {
			amountSyncing = true;
			try {
				singleAmount.setText(val + "");
			} finally {
				amountSyncing = false;
			}
		}
	}

	private int parseAmount(EditText field) {
		if (field == null || field.getText() == null) return 0;
		try {
			return (int) Double.parseDouble(field.getText().toString());
		} catch (NumberFormatException e) {
			return 0;
		}
	}


	protected void createActionBarSpinner(){
		if(spinner != null) {
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


	private void setData(BookInfo book){
		titleView.setText("Изменить ");
		paid.setChecked(book.getPaid());
		nocash.setChecked(book.getNocash());
		carNo.setText(book.getClientKey().replaceAll("\\(","").replaceAll("\\)","").trim());

		if(!TextUtils.isEmpty(book.getClientKey())){
			if(book.getClientKey().contains("(")) {
				carNo.setText(book.getClientKey().substring(0, book.getClientKey().indexOf("(")).trim());
				clientName.setText(book.getClientKey().substring(book.getClientKey().indexOf("("),book.getClientKey().length()).replaceAll("\\(", "").replaceAll("\\)", "").trim());
			}
			else {
				carNo.setText(book.getClientKey().trim());
				clientName.setText("");
			}
		}
		else{
			carNo.setText("");
		}

		if(!book.getMobile().equals(UserPreferences.getUserPhone(BA.getContext()))) {
			mobile.setVisibility(View.VISIBLE);
			if(!TextUtils.isEmpty(book.getMobile()))
				if(book.getMobile().length() > 2) {
					mobile.setText(book.getMobile().substring(2,book.getMobile().length()));
				}
			else
				mobile.setVisibility(View.GONE);
		}
		else
			mobile.setVisibility(View.GONE);

		if(!book.isOffline() || !book.isQueued()) {
			mobile.setEnabled(false);
			clientName.setEnabled(false);
		}
		else {
			mobile.setEnabled(true);
			clientName.setEnabled(true);
		}

		if(!TextUtils.isEmpty(book.getComment())){
			comment.setText(book.getComment());
		}

		if (!TextUtils.isEmpty(book.getCarMark())) {
			carMark.setText(book.getCarMark());
		}

		if(book.getDiscount() != null && book.getDiscount() > 0){
			discount.setText(book.getDiscount()+"");
		}

//		if(book.getDiscountAmount()!= null && book.getDiscountAmount() > 0){
//			discountAmount.setText(book.getDiscountAmount()+"");
//			selectedItem = 1;
//			isSpinnerTouched = true;
//			spinner.setSelection(selectedItem);
//			isSpinnerTouched = false;
//			discountAmount.setVisibility(View.VISIBLE);
//			discount.setVisibility(View.GONE);
//		}

		if(book.getCartype()!=null){
			switch(book.getCartype()){
				case "10":
					buttonMini.performClick();
					break;
				case "1":
					buttonSedan.performClick();
					break;
				case "11":
					buttonUniversal.performClick();
					break;
				case "2":
					buttonCrossover.performClick();
					break;
				case "30":
					buttonMiniJeep.performClick();
					break;
				case "3":
					buttonBigJeep.performClick();
					break;
				case "4":
					buttonMiniVan.performClick();
					break;
				case "41":
					buttonMinbus.performClick();
					break;
				case "5":
					buttonMoto.performClick();
					break;
				case "6":
					selectedCarType = CarType.Other;
					createService(selectedCarType.getValue());
					closeKeyboard();
					carTypesLayout.setVisibility(View.GONE);
					boxesLayout.setVisibility(View.GONE);
					break;
			}
		}

		extraLayout.setVisibility(View.VISIBLE);

		depositAmount.setEnabled(true);
		bonusAmount.setEnabled(true);
		calculatePaymentTypesTransactionAmount();
		createStaffGrid(washer);

		String clientInformationText = "";
		selectedClient = book.getCompanyClient();

		if(selectedClient != null && !book.getCartype().equals("6")) {
			boxId = book.getBoxId();
			createBoxGrid(washer);

			if(selectedClient.getDiscount() > 0){
				discount.setText(selectedClient.getDiscount()+"");
				spinner.setVisibility(View.GONE);
				discount.setEnabled(false);
				if (serviceAdapter != null && serviceAdapter.getData() != null)
					calculatePriceAndTime((ArrayList) serviceAdapter.getData(), (ArrayList) groupServiceAdapter.getData());
			}

			if (!TextUtils.isEmpty(selectedClient.getName())) {
				clientInformationText = selectedClient.getName().trim();
			} else {
				clientInformationText = "";
			}

			if (!TextUtils.isEmpty(selectedClient.getCarNo())) {
				if (selectedClient.getCarNo().contains("(")) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						clientInformationText = clientInformationText + " " + selectedClient.getCarNo().substring(0, selectedClient.getCarNo().indexOf("(")).trim();
					} else {
						clientInformationText = clientInformationText + " " + selectedClient.getCarNo().substring(0, selectedClient.getCarNo().indexOf("(")).trim();
					}

					if (TextUtils.isEmpty(selectedClient.getName())) {
						clientInformationText = selectedClient.getCarNo().substring(selectedClient.getCarNo().indexOf("("), selectedClient.getCarNo().length()).replaceAll("\\(", "").replaceAll("\\)", "").trim() + " " + clientInformationText;
					}
				} else {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						clientInformationText = clientInformationText + " " + selectedClient.getCarNo().trim();
					} else {
						clientInformationText = clientInformationText + " " + selectedClient.getCarNo().trim();
					}
				}
			}

			if (!TextUtils.isEmpty(selectedClient.getPhone()) && selectedClient.getPhone().length() > 2) {
				clientInformationText = clientInformationText + " " + Functions.formatPhoneNumber("+7 ### ### ## ##",selectedClient.getPhone());
			}
			selectedClientInformationText.setText(clientInformationText);

			clientDepositAmount.setText("из " + selectedClient.getDepositAmount());
			clientBonusAmount.setText("из " + selectedClient.getAccruedBonuses());

			if(book.getPriceDetails() != null && book.getPriceDetails().size() > 0){
				for(BookInfo.PriceDetail pd : book.getPriceDetails()){
					if("C".equals(pd.getPaymentType())){
						cashAmount.setText(pd.getAmount().intValue()+"");
					}
					else if("CD".equals(pd.getPaymentType())){
						cardAmount.setText(pd.getAmount().intValue()+"");
					}
					else if("B".equals(pd.getPaymentType())){
						bonusAmount.setText(pd.getAmount().intValue()+"");
					}
					else if("DA".equals(pd.getPaymentType())){
						depositAmount.setText(pd.getAmount().intValue()+"");
					}
				}
			}
		}
		else{
			clientInformationText = book.getClientKey();
			selectedClientInformationText.setText(clientInformationText);
			depositAmount.setEnabled(false);
			bonusAmount.setEnabled(false);

			if(selectedClient != null) {
				clientDepositAmount.setText("из " + selectedClient.getDepositAmount());
				clientBonusAmount.setText("из " + selectedClient.getAccruedBonuses());
			}
			else{
				clientDepositAmount.setText("из 0");
				clientBonusAmount.setText("из 0");
			}
			paidLayout.setVisibility(View.GONE);
		}
	}

	protected void selectDiscountType(){
		if(selectedItem == 0){
			discount.setText("0");
			discountAmount.setText("0");
			discount.setVisibility(View.VISIBLE);
			discountAmount.setVisibility(View.GONE);
		}
		else{
			discount.setText("0");
			discountAmount.setText("0");
			discount.setVisibility(View.GONE);
			discountAmount.setVisibility(View.VISIBLE);
		}
		if (serviceAdapter != null && serviceAdapter.getData() != null && groupServiceAdapter != null && groupServiceAdapter.getData() != null)
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
	public void onBookingInfoReceived(AdminBookInfoResponseEvent event) {
		setWaitScreen(false);

		if(event.getResult() != null) {
			if (BaseAssist.isSuccess(event.getResult())) {
				book = event.getResult().getResponse();
				setData(book);
				syncPaymentChipsFromAmounts();
			}
			else{
				ToastUtil.display(this, event.getResult().getMessage());
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
		}
		else{
			ToastUtil.display(this, "Ошибка при получении данных");
			finish();
			overridePendingTransitionWithCommonCloseTransition();
		}
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		washer = event.getWasher();
	    if(washer!=null){
			makeCarTypeButtons(washer.getCarTypes());
		}
		setWaitScreen(true);
		BA.getEventBus().post(new AdminBookInfoRequestEvent(book.getId()));
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
							if (key != null && key.equals(carType)) {
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
					if (p.getCarType() != null && p.getCarType().equals(carType)) {
						groupServiceItems.add(g);
					}
				}
			}
		}

		//Service adapter
		serviceAdapter = new ServicesGridAdapter(this, serviceItems);
		gridServices.setAdapter(serviceAdapter);
		gridServices.setExpanded(true);

		if(serviceItems.size() > 0 && book.getServices() != null && book.getServices().size() > 0 && book.getCartype().equals(carType.toString())){
			request.setServices(new ArrayList<Integer>());
			final ArrayList<Integer> requestServices = new ArrayList<Integer>();
			for (int i = 0; i < serviceItems.size(); i++) {
				for(int j = 0; j < book.getServices().size(); j++) {
					if (serviceItems.get(i) != null && book.getServices().get(j) != null && book.getServices().get(j).equals(serviceItems.get(i).getServiceId()) ) {
						requestServices.add(serviceAdapter.getData().get(i).getServiceId());
						serviceAdapter.setSelectedPosition(i, true);
					}
				}
			}
			request.setServices(new ArrayList<Integer>(requestServices));
			calculatePriceAndTime((ArrayList) serviceAdapter.getData(),groupServiceAdapter!=null?(ArrayList) groupServiceAdapter.getData():null);
			requestServices.clear();
		}

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

		//selected items
		if(groupServiceItems != null && groupServiceItems.size() > 0 && book.getGroupServices() != null && book.getGroupServices().size()>0){
			final ArrayList<String> requestGroupServices = new ArrayList<String>();
			for (int i = 0; i < groupServiceItems.size(); i++) {
				Washer.GroupMenu g = groupServiceItems.get(i);
				for(String s : book.getGroupServices()){
					if(s.equals(g.getId())){
						requestGroupServices.add(groupServiceAdapter.getData().get(i).getId());
						groupServiceAdapter.setSelectedPosition(i, true);
					}
				}
			}
			request.setGroupServices(new ArrayList<String>(requestGroupServices));
			calculatePriceAndTime((ArrayList) serviceAdapter.getData(),groupServiceAdapter!=null?(ArrayList) groupServiceAdapter.getData():null);
			requestGroupServices.clear();
		}

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

	protected void createBoxGrid(Washer washer) {
		//Boxes
		ArrayList<Box> boxItems = new ArrayList<Box>();
		if (washer != null && washer.getBoxSettings() != null && washer.getBoxSettings().size() > 0) {
			boxItems = washer.getBoxSettings();
			boxItems = Functions.filterDisableddBoxes(boxItems);
		}

		//Box adapter
		boxAdapter = new OfflineBoxesGridAdapter(this, boxItems);
		gridBoxes.setAdapter(boxAdapter);
		gridBoxes.setExpanded(true);

		//selected items
		if(boxItems != null && boxItems.size() > 0 && book.getBoxId() != null){
			final ArrayList<String> requestGroupServices = new ArrayList<String>();
			for (int i = 0; i < boxItems.size(); i++) {
				Box b = boxItems.get(i);
					if(boxId.equals(b.getUid())){
						boxAdapter.setSelectedPosition(i, true);
					}
			}
			request.setBoxId(boxId);
			calculatePriceAndTime((ArrayList) serviceAdapter.getData(),groupServiceAdapter!=null?(ArrayList) groupServiceAdapter.getData():null);
			requestGroupServices.clear();
		}

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
				boxId = ((Box) boxAdapter.getItem(position)).getUid();
			}
		});

		if (boxItems.size() == 0) {
			gridBoxes.setVisibility(View.GONE);
			noOfflineBox.setVisibility(View.VISIBLE);
		} else {
			gridBoxes.setVisibility(View.VISIBLE);
			noOfflineBox.setVisibility(View.GONE);
		}
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

		//selected items
		if(items != null && items.size() > 0 && book.getStaffs() != null && book.getStaffs().size()>0){
			final ArrayList<String> requestStaffs = new ArrayList<String>();
			for (int i = 0; i < items.size(); i++) {
				Staff st = items.get(i);
				for(String s : book.getStaffs()){
					if(s.equals(st.getUid())){
						requestStaffs.add(staffGridAdapter.getData().get(i).getUid());
						staffGridAdapter.setSelectedPosition(i, true);
					}
				}
			}
			request.setStaffs(new ArrayList<String>(requestStaffs));
			requestStaffs.clear();
		}

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
		discountAmount.addTextChangedListener(discountAmountWatcher);
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
				if (data.get(i).getServiceId() != null && data.get(i).getServiceId().equals(service)) {
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
								if (p.getCarType() != null && p.getCarType().equals(selectedCarType.getValue())) {
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

	@OnClick(R.id.btnEdit)
	public void editRequest(){
		calculatePaymentTypesTransactionAmount();
		String phoneStr = mobile.getText().toString();
		phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");
		String carMarkStr = carMark.getText().toString().trim();

		if(selectedCarType == null) {
			ToastUtil.displayAtTop(this, "Выберите машину");
			return;
		}

		if(!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)){
			ToastUtil.displayAtTop(this, "Выберите услуги");
			return;
		}

		if (finalPriceDifference != 0) {
			ToastUtil.displayAtTop(this, "Остаток не равен нулю! Распределите суммы правильно!");
			return;
		}

		request.setId(book.getId());
		request.setBoxId(boxId);
		request.setMobile(TextUtils.isEmpty(phoneStr)?"":"+7"+phoneStr);
		if (TextUtils.isEmpty(carMarkStr) && selectedClient != null && !TextUtils.isEmpty(selectedClient.getCarMark())) {
			carMarkStr = selectedClient.getCarMark();
		} else if (TextUtils.isEmpty(carMarkStr) && book != null && !TextUtils.isEmpty(book.getCarMark())) {
			carMarkStr = book.getCarMark();
		}
		request.setCarMark(carMarkStr);
		request.setCarType(selectedCarType.getValue());
		request.setPaid(paid.isChecked());
		request.setCarwash(URL_PREFIX+washer.getId());
		if(selectedCarType!=CarType.Other) {
			request.setClientKey(carNo.getText().toString());
			if (!TextUtils.isEmpty(clientName.getText().toString().trim()))
				request.setClientKey(carNo.getText().toString().trim() + " (" + clientName.getText().toString() + ")");
		}
		else{
			request.setClientKey(book.getClientKey());
		}

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

		BA.getEventBus().post(new AdminEditQueueOrBookRequestEvent(request));
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
	public void onAdminQueueResponseReceived(AdminEditQueueOrBookResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				finish();
				overridePendingTransitionWithCommonCloseTransition();
			}
			else
				ToastUtil.display(this, event.getResult().getMessage());
		}
		else{
			ToastUtil.display(this, "Ошибка при изменении");
		}
	}

	protected void makeCarTypeButtons(List<Integer> cartypes) {
		buttonMini.setVisibility(View.GONE);
		buttonSedan.setVisibility(View.GONE);
		buttonUniversal.setVisibility(View.GONE);
		buttonCrossover.setVisibility(View.GONE);
		buttonMiniJeep.setVisibility(View.GONE);
		buttonBigJeep.setVisibility(View.GONE);
		buttonMiniVan.setVisibility(View.GONE);
		buttonMinbus.setVisibility(View.GONE);
		buttonMoto.setVisibility(View.GONE);

		if (cartypes.contains(CarType.Mini.getValue())) {
			buttonMini.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Mini;
		}
		if (cartypes.contains(CarType.Sedan.getValue())) {
			buttonSedan.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Sedan;
		}
		if (cartypes.contains(CarType.Universal.getValue())) {
			buttonUniversal.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Universal;
		}
		if (cartypes.contains(CarType.Crossover.getValue())) {
			buttonCrossover.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Crossover;
		}
		if (cartypes.contains(CarType.MiniJeep.getValue())) {
			buttonMiniJeep.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.MiniJeep;
		}
		if (cartypes.contains(CarType.Jeep.getValue())) {
			buttonBigJeep.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Jeep;
		}
		if (cartypes.contains(CarType.Minivan.getValue())) {
			buttonMiniVan.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Minivan;
		}
		if (cartypes.contains(CarType.Minibus.getValue())) {
			buttonMinbus.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Minibus;
		}
		if (cartypes.contains(CarType.Moto.getValue())) {
			buttonMoto.setVisibility(View.VISIBLE);
				if (selectedCarType == null) selectedCarType = CarType.Moto;
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


}
