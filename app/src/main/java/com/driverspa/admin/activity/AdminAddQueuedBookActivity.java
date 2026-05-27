package com.driverspa.admin.activity;

import static com.driverspa.util.Constants.URL_COMPANY_CLIENT_PREFIX;
import static com.driverspa.util.Constants.URL_PREFIX;

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
import com.driverspa.model.Box;
import com.driverspa.model.CarType;
import com.driverspa.model.ClientInfo;
import com.driverspa.model.ReversePrices;
import com.driverspa.model.ServiceItem;
import com.driverspa.model.Staff;
import com.driverspa.model.WashCountCampaign;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
//import com.driverspa.ocr.OcrCaptureActivity;
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

public class AdminAddQueuedBookActivity extends AdminBaseActivity {

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

    @BindView(R.id.btnBook)
    Button buttonBook;
    @BindView(R.id.btnQueue)
    Button buttonQueue;
    @BindView(R.id.toBox)
    View buttonToBox;
    @BindView(R.id.Other)
    View buttonOtherServices;
    @BindView(R.id.carTypesLayout)
    View carTypesLayout;
    @BindView(R.id.boxesLayout)
    View boxesLayout;
    @BindView(R.id.paidLayout)
    View paidLayout;
    @BindView(R.id.searchLayout)
    View searchLayout;
    @BindView(R.id.toQueue)
    View buttonToQueue;

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
    @BindView(R.id.txtBookTime)
    TextView txtBookTime;
    CarType selectedCarType;
    @BindView(R.id.txtPrice)
    TextView txtPrice;
    @BindView(R.id.txtTime)
    TextView txtTime;
    TextView titleView;

    @BindView(R.id.paid)
    CheckBox paid;
    @BindView(R.id.nocash)
    CheckBox nocash;
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

    int selectedItem = 0;
    @BindView(R.id.discount)
    EditText discount;
    @BindView(R.id.discountAmount)
    EditText discountAmount;
    @BindView(R.id.discountSpinner)
    NDSpinner spinner;
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
    @BindView(R.id.washCountCampaignDescription)
    TextView washCountCampaignDescription;
    MaskedWatcher maskedWatcher;
    @BindView(R.id.clients)
    TextView clients;

    @BindView(R.id.priceLayout)
    View priceLayout;
    @BindView(R.id.nocashLayout)
    View nocashLayout;

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

    // --- Wizard widgets (not bound via ButterKnife since they're optional in older layouts) ---
    private Button btnBack, btnNext;
    private View stepHeader1, stepHeader2, stepHeader3;
    private View stepDot1, stepDot2, stepDot3;
    private TextView stepLabel1, stepLabel2, stepLabel3;
    private View stepConnector12, stepConnector23;
    private View stepCarHost, stepServicesHost, stepPaymentHost;
    private View singleAmountLayout, splitAmountLayout;
    private EditText singleAmount;
    private View chipCash, chipCard, chipBonus, chipDeposit, chipSplit;

    private static final int STEP_CAR = 0;
    private static final int STEP_SERVICES = 1;
    private static final int STEP_PAYMENT = 2;

    private static final int MODE_BOX = 0;
    private static final int MODE_QUEUE = 1;
    private static final int MODE_OTHER = 2;

    private int currentStep = STEP_CAR;
    private int wizardMode = MODE_BOX;
    /** 0=cash, 1=card, 2=bonus, 3=deposit, -1=split (advanced layout) */
    private int currentPaymentField = 0;
    /** Guards mirroring between singleAmount and the four backing fields. */
    private boolean amountSyncing = false;

    private StaffGridAdapter staffGridAdapter;
    BookingRequest request;

    protected FilterableClientsAdapter clientsAdapter;
    protected ServicesGridAdapter serviceAdapter;
    protected GroupServicesGridAdapter groupServiceAdapter;
    protected OfflineBoxesGridAdapter boxAdapter;
    TextWatcher discountAmountWatcher;

    HashMap<Integer, Double> menuServicesPrices = new HashMap<Integer, Double>();
    int RC_OCR_CAPTURE = 9999;
    AdminSearchFilter filter = new AdminSearchFilter();
    String textPrediction;

    ClientInfo selectedClient;
    Double finalPrice;
    Double finalPriceDifference;

    List<BookInfo.PriceDetail> priceDetails = new ArrayList<>();
    boolean negativeAnswer = false;
    boolean isSpinnerTouched = false;
    private String queryText = "";

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Mint.initAndStartSession(this.getApplication(), "fa7fe554");
        Mint.setUserIdentifier(UserPreferences.getUserPhone(this));
        setContentView(R.layout.activity_admin_add_queue);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        titleView = (TextView) mToolbar.findViewById(R.id.action_bar_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ButterKnife.bind(this);

        buttonToBox.setSelected(true);
        buttonToQueue.setSelected(false);
        titleView.setText("Отправить в бокс");
        groupBtn.setSelected(true);
        serviceBtn.setSelected(false);

        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });

        prepareInitialViews();
        setupWizard();
    }

    // -------------------------------------------------------------------------
    // Wizard (3-step flow): Авто → Услуги → Оплата
    // -------------------------------------------------------------------------

    private void setupWizard() {
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
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
        singleAmountLayout = findViewById(R.id.singleAmountLayout);
        splitAmountLayout = findViewById(R.id.splitAmountLayout);
        singleAmount = findViewById(R.id.singleAmount);
        chipCash = findViewById(R.id.chipCash);
        chipCard = findViewById(R.id.chipCard);
        chipBonus = findViewById(R.id.chipBonus);
        chipDeposit = findViewById(R.id.chipDeposit);
        chipSplit = findViewById(R.id.chipSplit);

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
                public void onClick(View v) { if (isStepAllowed(STEP_CAR)) showStep(STEP_CAR); }
            });
        }
        if (stepHeader2 != null) {
            stepHeader2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { if (isStepAllowed(STEP_SERVICES)) showStep(STEP_SERVICES); }
            });
        }
        if (stepHeader3 != null) {
            stepHeader3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { if (isStepAllowed(STEP_PAYMENT)) showStep(STEP_PAYMENT); }
            });
        }

        setupPaymentChips();

        wizardMode = MODE_BOX;
        applyStepVisibilityForMode();
        showStep(STEP_CAR);
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
        // Initial state: cash chip visually selected but do NOT mirror into
        // cashAmount — the existing TextWatcher would dereference finalPrice
        // which is null before a service is picked, causing a startup NPE.
        selectPaymentChip(0, false);
    }

    /**
     * Routes the value entered in the single "Сумма" field into the underlying
     * cash/card/bonus/deposit EditTexts that the existing price-calculation code
     * already watches.
     */
    private void mirrorSingleAmount(String value) {
        // The four amount fields are watched by calculatePaymentTypesTransactionAmount,
        // which dereferences finalPrice. Skip until a service has been selected and
        // finalPrice is populated, otherwise setText would trigger an NPE.
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

    /**
     * @param syncAmounts when false, only updates visual chip state and does
     *                    not touch the underlying cash/card/bonus/deposit
     *                    EditTexts. Used at init time, before the price-calc
     *                    watchers are safe to fire.
     */
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

    private void onWizardNext() {
        int next = nextAllowedStep(currentStep);
        if (next == currentStep) {
            // Already on the last allowed step — treat as submit.
            triggerFinishAction();
            return;
        }
        showStep(next);
    }

    private void onWizardBack() {
        int prev = previousAllowedStep(currentStep);
        if (prev == currentStep) {
            // Already on the first allowed step — close.
            onBackPressed();
            return;
        }
        showStep(prev);
    }

    private void triggerFinishAction() {
        if (wizardMode == MODE_QUEUE) {
            if (buttonQueue != null) buttonQueue.performClick();
        } else {
            if (buttonBook != null) buttonBook.performClick();
        }
    }

    private boolean isStepAllowed(int step) {
        if (wizardMode == MODE_QUEUE) return step == STEP_CAR;
        if (wizardMode == MODE_OTHER) return step != STEP_CAR;
        return true;
    }

    private int nextAllowedStep(int from) {
        for (int s = from + 1; s <= STEP_PAYMENT; s++) {
            if (isStepAllowed(s)) return s;
        }
        return from;
    }

    private int previousAllowedStep(int from) {
        for (int s = from - 1; s >= STEP_CAR; s--) {
            if (isStepAllowed(s)) return s;
        }
        return from;
    }

    private void showStep(int step) {
        if (!isStepAllowed(step)) step = nextAllowedStep(STEP_CAR - 1);
        currentStep = step;

        if (stepCarHost != null) stepCarHost.setVisibility(step == STEP_CAR ? View.VISIBLE : View.GONE);
        if (stepServicesHost != null) stepServicesHost.setVisibility(step == STEP_SERVICES ? View.VISIBLE : View.GONE);
        if (stepPaymentHost != null) stepPaymentHost.setVisibility(step == STEP_PAYMENT ? View.VISIBLE : View.GONE);

        applyHeaderState(stepDot1, stepLabel1, isStepAllowed(STEP_CAR), step == STEP_CAR);
        applyHeaderState(stepDot2, stepLabel2, isStepAllowed(STEP_SERVICES), step == STEP_SERVICES);
        applyHeaderState(stepDot3, stepLabel3, isStepAllowed(STEP_PAYMENT), step == STEP_PAYMENT);

        boolean isLast = (nextAllowedStep(step) == step);
        if (btnNext != null) btnNext.setVisibility(isLast ? View.GONE : View.VISIBLE);
        if (buttonBook != null) {
            buttonBook.setVisibility(isLast && wizardMode != MODE_QUEUE ? View.VISIBLE : View.GONE);
        }
        if (buttonQueue != null) {
            buttonQueue.setVisibility(isLast && wizardMode == MODE_QUEUE ? View.VISIBLE : View.GONE);
        }
    }

    private void applyHeaderState(View dot, TextView label, boolean enabled, boolean selected) {
        if (dot != null) {
            dot.setActivated(enabled && !selected);
            dot.setSelected(selected);
            dot.setAlpha(enabled ? 1f : 0.35f);
        }
        if (label != null) {
            label.setAlpha(enabled ? 1f : 0.35f);
            if (selected) {
                label.setTextColor(getResources().getColor(R.color.White));
                label.setTypeface(label.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                label.setTextColor(getResources().getColor(R.color.BlueLight));
                label.setTypeface(android.graphics.Typeface.DEFAULT);
            }
        }
    }

    private void applyStepVisibilityForMode() {
        // Hide indicator headers for steps that aren't part of the current mode.
        if (stepHeader1 != null) stepHeader1.setVisibility(isStepAllowed(STEP_CAR) ? View.VISIBLE : View.GONE);
        if (stepHeader2 != null) stepHeader2.setVisibility(isStepAllowed(STEP_SERVICES) ? View.VISIBLE : View.GONE);
        if (stepHeader3 != null) stepHeader3.setVisibility(isStepAllowed(STEP_PAYMENT) ? View.VISIBLE : View.GONE);
        if (stepConnector12 != null) {
            stepConnector12.setVisibility(
                    isStepAllowed(STEP_CAR) && isStepAllowed(STEP_SERVICES) ? View.VISIBLE : View.GONE);
        }
        if (stepConnector23 != null) {
            stepConnector23.setVisibility(
                    isStepAllowed(STEP_SERVICES) && isStepAllowed(STEP_PAYMENT) ? View.VISIBLE : View.GONE);
        }
    }

    private void setWizardMode(int mode) {
        wizardMode = mode;
        applyStepVisibilityForMode();
        int firstStep = nextAllowedStep(STEP_CAR - 1);
        showStep(firstStep);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @OnClick(R.id.toBox)
    public void onTodayButtonClick() {
        buttonToBox.setSelected(true);
        buttonToQueue.setSelected(false);
        buttonOtherServices.setSelected(false);
        priceLayout.setVisibility(View.VISIBLE);
        buttonBook.setVisibility(View.VISIBLE);
        buttonQueue.setVisibility(View.GONE);
        detailLayout.setVisibility(View.VISIBLE);
        titleView.setText("Отправить в бокс");

        this.selectedCarType = CarType.Sedan;
        boxesLayout.setVisibility(View.VISIBLE);
        paidLayout.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
        carTypesLayout.setVisibility(View.VISIBLE);
        clientInformationsLayout.setVisibility(View.VISIBLE);
        selectedClientInformationLayout.setVisibility(View.GONE);
        selectedClient = null;
        createService(selectedCarType.getValue());
        buttonBook.setText("Отправить в бокс");
        setWizardMode(MODE_BOX);
    }


    @OnClick(R.id.Other)
    public void onOtherServicesButtonClick() {
        onCloseClientRequested();
        this.selectedCarType = CarType.Other;
        boxesLayout.setVisibility(View.GONE);
        paidLayout.setVisibility(View.GONE);
        searchLayout.setVisibility(View.GONE);
        carTypesLayout.setVisibility(View.GONE);
        clientInformationsLayout.setVisibility(View.GONE);
        selectedClientInformationLayout.setVisibility(View.GONE);
        selectedClient = null;
        createService(selectedCarType.getValue());
        buttonToBox.setSelected(false);
        buttonOtherServices.setSelected(true);
        buttonToQueue.setSelected(false);
        priceLayout.setVisibility(View.VISIBLE);
        buttonBook.setVisibility(View.VISIBLE);
        buttonQueue.setVisibility(View.GONE);
        detailLayout.setVisibility(View.VISIBLE);
        titleView.setText("Другие услуги");
        buttonBook.setText("Заказать");
        setWizardMode(MODE_OTHER);
    }

    @OnClick(R.id.toQueue)
    public void onTomorrowButtonClick() {
        priceLayout.setVisibility(View.GONE);
        buttonToBox.setSelected(false);
        buttonToQueue.setSelected(true);
        buttonOtherServices.setSelected(false);
        clientInformationsLayout.setVisibility(View.VISIBLE);
        buttonBook.setVisibility(View.GONE);
        buttonQueue.setVisibility(View.VISIBLE);
        detailLayout.setVisibility(View.GONE);
        titleView.setText("Добавить в очередь");
        boxesLayout.setVisibility(View.VISIBLE);
        paidLayout.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
        carTypesLayout.setVisibility(View.VISIBLE);
        clientInformationsLayout.setVisibility(View.VISIBLE);
        selectedClientInformationLayout.setVisibility(View.GONE);
        setWizardMode(MODE_QUEUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, 0);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(0, 0);
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
                    createBoxGrid(washer);
                    createStaffGrid(washer);
                    carNo.setThreshold(2);
                    clientsAdapter = new FilterableClientsAdapter(AdminAddQueuedBookActivity.this, new ArrayList<ClientInfo>(), washer);
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
                }
            }, 200);
        }
    }

    @OnClick(R.id.btnQueue)
    public void createQueueRequest() {
        carNo.setError(null);
        closeKeyboard();
        String сarNoStr = carNo.getText().toString();
        if (TextUtils.isEmpty(сarNoStr)) {
            ToastUtil.displayAtTop(this, "Введите гос.номер машины");
            if (carNo.hasFocus())
                carNo.setError(null);
            else
                carNo.setError("");
            titleView.requestFocus();
            return;
        }

        String phoneStr = mobile.getText().toString();
        phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

        request.setCarNo(сarNoStr);
        request.setMobile(TextUtils.isEmpty(phoneStr) ? "" : "+7" + phoneStr);
        request.setClientKey(сarNoStr);
        if (!TextUtils.isEmpty(clientName.getText().toString().trim()))
            request.setClientKey(сarNoStr.trim() + " (" + clientName.getText().toString() + ")");
            request.setTime(Instant.now());
        request.setCarwash(URL_PREFIX + washer.getId());
        request.setOffline(true);
        request.setQueued(true);
        request.setDirectWash(false);
        request.setCarMark(carMark.getText().toString());
        request.setPaid(paid.isChecked());
        request.setComment(comment.getText().toString());
        try {
            int value = Integer.parseInt(discount.getText().toString());
            if (value > 100) {
                discount.setText("100");
            }
            request.setDiscount(Double.parseDouble(discount.getText().toString()));
        } catch (NumberFormatException e) {
            request.setDiscount(0d);
        }

        BA.getEventBus().post(new AdminBookRequestEvent(request));
        setWaitScreen(true);
    }

    @OnClick(R.id.btnBook)
    public void createBookingRequest() {
        calculatePaymentTypesTransactionAmount();
        carNo.setError(null);
        closeKeyboard();

        if (selectedCarType != CarType.Other) {
            String сarNoStr = carNo.getText().toString();
            String phoneStr = mobile.getText().toString();
            phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

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

            if (selectedCarType == null) {
                ToastUtil.displayAtTop(this, "Выберите машину");
                return;
            }

            if (!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)) {
                ToastUtil.displayAtTop(this, "Выберите услуги");
                return;
            }

            if (TextUtils.isEmpty(boxId)) {
                ToastUtil.displayAtTop(this, "Выберите бокс");
                return;
            }

            if (finalPriceDifference != 0) {
                ToastUtil.displayAtTop(this, "Остаток не равен нулю! Распределите суммы правильно!");
                return;
            }

            request.setCarNo(сarNoStr);
            request.setMobile(TextUtils.isEmpty(phoneStr) ? "" : "+7" + phoneStr);
            request.setClientKey(сarNoStr);
            if (!TextUtils.isEmpty(clientName.getText().toString()))
                request.setClientKey(сarNoStr.trim() + " (" + clientName.getText().toString() + ")");
            request.setCarType(selectedCarType.getValue());
            request.setBoxId(boxId);
                  request.setTime(Instant.now());
            request.setCarwash(URL_PREFIX + washer.getId());
            request.setOffline(true);
            request.setQueued(true);
            request.setDirectWash(true);
            request.setPaid(paid.isChecked());
            request.setNocash(nocash.isChecked());

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
        }
        else if(selectedCarType == CarType.Other){
            if (!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)) {
                ToastUtil.displayAtTop(this, "Выберите услуги");
                return;
            }

            if (finalPriceDifference != 0) {
                ToastUtil.displayAtTop(this, "Остаток не равен нулю! Распределите суммы правильно!");
                return;
            }

            request.setCarNo("");
            request.setMobile("");
            request.setClientKey("Магазин");
            request.setCarType(selectedCarType.getValue());
                  request.setTime(Instant.now());
            request.setCarwash(URL_PREFIX + washer.getId());
            request.setOffline(true);
            request.setQueued(true);
            request.setDirectWash(true);
            request.setPaid(true);
            request.setComment(comment.getText().toString());

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
        }

        BA.getEventBus().post(new AdminBookRequestEvent(request));
        setWaitScreen(true);
    }

    @Subscribe
    public void onAdminBookResponseReceived(AdminBookResponseEvent event) {
        setWaitScreen(false);
        if (event.getResult() != null) {
            if (BaseAssist.isSuccess(event.getResult())) {
                finish();
                overridePendingTransitionWithCommonCloseTransition();
            } else
                ToastUtil.display(this, event.getResult().getMessage());
        } else {
            ToastUtil.display(this, "Ошибка при добавлении очереди");
        }
    }

    @OnClick(R.id.more)
    public void moreFieldsClicked() {
        if (detailLayout.getVisibility() == View.GONE) {
            detailLayout.setVisibility(View.VISIBLE);
            moreButton.setText("Дополнительно");
            moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
            buttonBook.setEnabled(true);
        } else {
            detailLayout.setVisibility(View.GONE);
            moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
            moreButton.setText("Дополнительно");
            buttonBook.setEnabled(false);
        }
    }

    @OnClick(R.id.extra)
    public void extraFieldsClicked() {
        if (extraLayout.getVisibility() == View.GONE) {
            extraLayout.setVisibility(View.VISIBLE);
            extraButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
        } else {
            extraLayout.setVisibility(View.GONE);
            extraButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
        }
    }

    @OnClick(R.id.paid_text)
    public void onPaidTextClicked() {
        paid.setChecked(!paid.isChecked());
    }

    @OnClick(R.id.nocashText)
    public void onNocashTextClicked() {
        nocash.setChecked(!nocash.isChecked());
    }

    @OnClick(R.id.client_layout)
    public void onCLientLayoutClicked() {
        Intent intent = new Intent(this, ClientSearchActivity.class);
        intent.putExtra(OPENING_ANIMATION, false);
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
        request.setCompanyClient(null);
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

    protected void prepareInitialViews() {
        selectedCarType = CarType.Sedan;
        groupBtn.setSelected(true);
        serviceBtn.setSelected(false);

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


        try {
            maskedWatcher = new MaskedWatcher("### ###-##-##");
            mobile.addTextChangedListener(maskedWatcher);
        } catch (ParseException e) {
            e.printStackTrace();
        }

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
        buttonCrossover.setSelected(false);
        buttonBigJeep.setSelected(false);
        buttonMinbus.setSelected(false);
        buttonMoto.setSelected(false);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth() - (int) Functions.dipToPixels(this, 10);

        ViewGroup.LayoutParams layoutParamsServices = gridServices.getLayoutParams();
        layoutParamsServices.width = width;
        gridServices.setLayoutParams(layoutParamsServices);
        gridGroupServices.setLayoutParams(layoutParamsServices);
        gridBoxes.setLayoutParams(layoutParamsServices);
        gridStaffs.setLayoutParams(layoutParamsServices);

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        boolean tabletBigSize = getResources().getBoolean(R.bool.isBigTablet);

        if (!tabletSize) {
            gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
            gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
            gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
            gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 4);
        } else if (!tabletBigSize) {
            gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
            gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
            gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
            gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 6);
        } else {
            gridServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
            gridGroupServices.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
            gridBoxes.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
            gridStaffs.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(this, 40)) / 8);
        }

        //set layout params same as grid services
        clients.setPaintFlags(clients.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        createActionBarSpinner();
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

    @OnFocusChange(R.id.carNo)
    public void onFocusChangeCarNo(boolean focus) {
        if (focus) {
            carNo.setError(null);
        }
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

    public void analyzeWasherCountCampaign(ClientInfo client) {
        washCountCampaignDescription.setVisibility(View.GONE);
        if (washer != null && washer.getWashCountCampaigns() != null && washer.getWashCountCampaigns().size() > 0) {
            WashCountCampaign selectedCampaign = null;
            boolean hasSameFactor = false;
            WashCountCampaign selectedCampaignOld = null;
            for (WashCountCampaign campaign : washer.getWashCountCampaigns()) {
                if ((client.getFinishedBooksCount() + 1) % campaign.getWashCount() == 0) {
                    if (selectedCampaign != null) {
                        hasSameFactor = true;
                        selectedCampaignOld = selectedCampaign;
                    }
                    selectedCampaign = campaign;
                }
            }

            if (hasSameFactor) {
                if (selectedCampaign.getWashCount() < selectedCampaignOld.getWashCount()) {
                    if (((client.getFinishedBooksCount() + 1) / selectedCampaign.getWashCount()) % 2 == 0) {
                        selectedCampaign = selectedCampaignOld;
                    }
                } else {
                    if (((client.getFinishedBooksCount() + 1) / selectedCampaignOld.getWashCount()) % 2 != 0) {
                        selectedCampaign = selectedCampaignOld;
                    }
                }
            }

            if (selectedCampaign != null) {
                washCountCampaignDescription.setVisibility(View.VISIBLE);
                washCountCampaignDescription.setText(selectedCampaign.getDescription());
            }
        }
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
    public void plateDetectionClicked() {
//        Intent intent = new Intent(this, OcrCaptureActivity.class);
//        intent.putExtra(OcrCaptureActivity.AutoFocus, true);
//        intent.putExtra(OcrCaptureActivity.UseFlash, false);
//        startActivityForResult(intent, RC_OCR_CAPTURE);
    }

    @Subscribe
    public void onAdminClientResponseReceived(AdminClientResponseEvent event) {
        setWaitScreen(false);
        if (event.getResult() != null) {
            if (BaseAssist.isSuccess(event.getResult())) {
                ClientInfo client = event.getResult().getClientInfo();
                if (!TextUtils.isEmpty(textPrediction) && !TextUtils.isEmpty(client.getCarNo())) {
                    ToastUtil.display(this, "Клиент найден в базе");
                    setData(client);
                }
            } else {
                ToastUtil.display(this, "Клиент не найден в базе");
            }
        } else {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityForResult.ACTIVITY_SEARCH) {
            if (resultCode == ActivityForResult.ACTIVITY_SEARCH_HAS_RESULT) {
                try {
                    ClientInfo client = ClientInfo.deserialize(data.getStringExtra(Constants.CLIENT_INFO));
                    setData(client);
                } catch (Exception e) {
                }
            }
        }

        if (requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
//                    textPrediction = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    if (!TextUtils.isEmpty(textPrediction))
                        textPrediction = textPrediction.replaceAll(" ", "").trim().replaceAll("\n", "");
                    setWaitScreen(true);
                    carNo.setText(textPrediction);
                    mobile.setText("");
                    clientName.setText("");
                    washCountCampaignDescription.setVisibility(View.GONE);
                    carNo.setSelection(carNo.getText().length());
                    carNo.requestFocus();
                } else {
                    ToastUtil.display(this, "Номер машины не выбран");
                }
            } else {
                ToastUtil.display(this, "Ошибка при обработке номера машин");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
        }
        if (cartypes.contains(CarType.Sedan.getValue())) {
            buttonSedan.setVisibility(View.VISIBLE);
        }
        if (cartypes.contains(CarType.Universal.getValue())) {
            buttonUniversal.setVisibility(View.VISIBLE);
        }
        if (cartypes.contains(CarType.Crossover.getValue())) {
            buttonCrossover.setVisibility(View.VISIBLE);
        }
        if (cartypes.contains(CarType.MiniJeep.getValue())) {
            buttonMiniJeep.setVisibility(View.VISIBLE);
        }
        if (cartypes.contains(CarType.Jeep.getValue())) {
            buttonBigJeep.setVisibility(View.VISIBLE);
        }
        if (cartypes.contains(CarType.Minivan.getValue())) {
            buttonMiniVan.setVisibility(View.VISIBLE);
        }
        if (cartypes.contains(CarType.Minibus.getValue())) {
            buttonMinbus.setVisibility(View.VISIBLE);
        }
        if (cartypes.contains(CarType.Moto.getValue())) {
            buttonMoto.setVisibility(View.VISIBLE);
        }
    }

    protected View.OnClickListener buttonSelector = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == buttonSedan.getId() && !buttonSedan.isSelected()) {
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
            } else if (view.getId() == buttonMini.getId() && !buttonMini.isSelected()) {
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
            } else if (view.getId() == buttonUniversal.getId() && !buttonUniversal.isSelected()) {
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
            } else if (view.getId() == buttonMiniJeep.getId() && !buttonMiniJeep.isSelected()) {
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
            } else if (view.getId() == buttonMiniVan.getId() && !buttonMiniVan.isSelected()) {
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
            } else if (view.getId() == buttonCrossover.getId() && !buttonCrossover.isSelected()) {
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
            } else if (view.getId() == buttonBigJeep.getId() && !buttonBigJeep.isSelected()) {
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
            } else if (view.getId() == buttonMoto.getId() && !buttonMoto.isSelected()) {
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
            } else if (view.getId() == buttonMinbus.getId() && !buttonMinbus.isSelected()) {
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

