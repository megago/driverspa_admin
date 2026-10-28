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
import com.driverspa.admin.payment.PaymentFlow;
import com.driverspa.model.api.response.BookResponse;
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
import com.driverspa.admin.shift.ShiftCrew;
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
import com.driverspa.util.otto.ws.AdminRejectQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminStartQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminStartQueueResponseEvent;
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

    @BindView(R.id.searchField)
    DelayAutoCompleteTextView searchField;
    @BindView(R.id.no_staffs)
    View noStaffs;
    @BindView(R.id.selectedClientInformationLayout)
    View selectedClientInformationLayout;
    @BindView(R.id.selectedClientInformationText)
    TextView selectedClientInformationText;
    @BindView(R.id.closeClient)
    View closeClient;

    @BindView(R.id.clientInformationsLayout)
    View clientInformationsLayout;

    @BindView(R.id.grid_staffs)
    ExpandableGridView gridStaffs;

    // --- Wizard widgets (not bound via ButterKnife since they're optional in older layouts) ---
    private Button btnBack, btnNext;
    private View stepHeader1, stepHeader2, stepHeader3;
    private View stepDot1, stepDot2, stepDot3;
    private TextView stepLabel1, stepLabel2, stepLabel3;
    private View stepConnector12, stepConnector23;
    private View stepCarHost, stepServicesHost, stepPaymentHost;

    private static final int STEP_CAR = 0;
    private static final int STEP_SERVICES = 1;
    private static final int STEP_PAYMENT = 2;

    public static final int MODE_BOX = 0;
    public static final int MODE_QUEUE = 1;
    /** "Other services" is replaced by the shop: it can no longer be opened (its tab is hidden). */
    private static final int MODE_OTHER = 2;
    /** Which of MODE_BOX / MODE_QUEUE the screen opens in (MODE_BOX by default). */
    public static final String EXTRA_MODE = "add_mode";

    private int currentStep = STEP_CAR;
    private int wizardMode = MODE_BOX;
    /** True while setData() sets the car type from a selected client (so the
     *  car-type lock below allows that programmatic change). */
    private boolean settingCarTypeFromClient = false;
    /** The request sent is a shop sale, which is paid as soon as it is created. */
    private boolean pendingSale = false;

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

    boolean negativeAnswer = false;
    boolean isSpinnerTouched = false;
    private String queryText = "";

    /** The queued car being sent to a box (opened with Constants.EXTRA_BOOK), null when adding. */
    private BookInfo queuedBook;
    private static final int MENU_DELETE_QUEUE = 1;

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
        titleView.setText(BA.str(R.string.send_to_box));
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

        String bookExtra = getIntent().getStringExtra(Constants.EXTRA_BOOK);
        if (!TextUtils.isEmpty(bookExtra)) {
            queuedBook = BookInfo.deserialize(bookExtra);
            showQueuedBook();
            return;
        }

        int mode = getIntent().getIntExtra(EXTRA_MODE, MODE_BOX);
        if (mode == MODE_QUEUE) onTomorrowButtonClick();
    }

    // -------------------------------------------------------------------------
    // Opened from the live queue: the "send to box" flow for that queued car
    // -------------------------------------------------------------------------

    /** Fills the car step from the queued car. Car type, client and discount wait for the washer. */
    private void showQueuedBook() {
        titleView.setText(BA.str(R.string.number_pre) + queuedBook.getOrder());
        buttonBook.setText(BA.str(R.string.send_to_box));
        // The car is already chosen: no client search, the plate is fixed.
        searchLayout.setVisibility(View.GONE);
        closeClient.setVisibility(View.GONE);

        String clientKey = queuedBook.getClientKey() != null ? queuedBook.getClientKey() : "";
        String plate = clientKey;
        String name = "";
        if (clientKey.contains("(")) {
            plate = clientKey.substring(0, clientKey.indexOf("("));
            name = clientKey.substring(clientKey.indexOf("(")).replaceAll("\\(", "").replaceAll("\\)", "");
        }
        carNo.setText(plate.trim(), false);
        carNo.setEnabled(false);
        clientName.setText(name.trim());
        if (!TextUtils.isEmpty(queuedBook.getMobile()) && queuedBook.getMobile().length() > 2)
            mobile.setText(queuedBook.getMobile().substring(2));
        if (!TextUtils.isEmpty(queuedBook.getCarMark()))
            carMark.setText(queuedBook.getCarMark());
        if (!TextUtils.isEmpty(queuedBook.getComment()))
            comment.setText(queuedBook.getComment());

        ClientInfo client = queuedBook.getCompanyClient();
        if (client != null) {
            // A client's car: shown as the selected client, nothing to edit.
            selectedClient = client;
            if (!TextUtils.isEmpty(client.getId()))
                request.setCompanyClient(URL_COMPANY_CLIENT_PREFIX + client.getId());
            String info = (name.trim() + " " + plate.trim()).trim();
            if (!TextUtils.isEmpty(queuedBook.getMobile()) && queuedBook.getMobile().length() > 2)
                info = info + " " + queuedBook.getMobile();
            selectedClientInformationText.setText(info);
            selectedClientInformationLayout.setVisibility(View.VISIBLE);
            clientInformationsLayout.setVisibility(View.GONE);
        }
    }

    /** Called once the washer (menu, boxes, staff) is loaded. */
    private void applyQueuedBookWasherData() {
        if (!TextUtils.isEmpty(queuedBook.getCartype())) {
            try {
                selectCarType(CarType.toEnum(Integer.parseInt(queuedBook.getCartype())));
            } catch (NumberFormatException e) {
            }
        }

        Integer queuedDiscount = queuedBook.getDiscount();
        if (queuedDiscount != null && queuedDiscount > 0) {
            discount.setText(queuedDiscount + "");
            if (selectedClient != null) {
                // A client's discount is not edited here (as when the client is selected).
                spinner.setVisibility(View.GONE);
                discount.setEnabled(false);
            }
        }
        if (selectedClient != null)
            analyzeWasherCountCampaign(selectedClient);
    }

    /** Selects a car type button as if by the client (allowed while a client is selected). */
    private void selectCarType(CarType carType) {
        View button = null;
        if (carType != null) {
            switch (carType) {
                case Mini: button = buttonMini; break;
                case Sedan: button = buttonSedan; break;
                case Universal: button = buttonUniversal; break;
                case Crossover: button = buttonCrossover; break;
                case MiniJeep: button = buttonMiniJeep; break;
                case Jeep: button = buttonBigJeep; break;
                case Minivan: button = buttonMiniVan; break;
                case Minibus: button = buttonMinbus; break;
                case Moto: button = buttonMoto; break;
                default: break;
            }
        }
        if (button == null || button.getVisibility() != View.VISIBLE)
            return;
        settingCarTypeFromClient = true;
        button.performClick();
        settingCarTypeFromClient = false;
    }

    private void confirmDeleteQueue() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(BA.str(R.string.delete_queue_q))
                .setNegativeButton(BA.str(R.string.no_word), null)
                .setPositiveButton(BA.str(R.string.yes_word), (dialog, which) -> {
                    if (washer == null) return;
                    BookingRequest rejectRequest = new BookingRequest();
                    rejectRequest.setId(queuedBook.getId());
                    rejectRequest.setMobile(queuedBook.getMobile());
                    rejectRequest.setTime(Instant.now());
                    rejectRequest.setCarwash(URL_PREFIX + washer.getId());
                    rejectRequest.setOffline(true);
                    rejectRequest.setQueued(true);
                    rejectRequest.setDirectWash(false);
                    BA.getEventBus().post(new AdminRejectQueueRequestEvent(rejectRequest));
                    setWaitScreen(true);
                })
                .show();
    }

    @Subscribe
    public void onAdminStartQueueResponseReceived(AdminStartQueueResponseEvent event) {
        onQueueActionResponse(event.getResult());
    }

    @Subscribe
    public void onAdminRejectQueueResponseReceived(AdminRejectQueueResponseEvent event) {
        onQueueActionResponse(event.getResult());
    }

    private void onQueueActionResponse(com.driverspa.model.api.response.BookResponseHolder result) {
        setWaitScreen(false);
        if (result != null) {
            if (BaseAssist.isSuccess(result)) {
                finish();
                overridePendingTransitionWithCommonCloseTransition();
            } else
                ToastUtil.display(this, result.getMessage());
        } else {
            ToastUtil.display(this, BA.str(R.string.err_add_queue));
        }
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


        wizardMode = MODE_BOX;
        applyStepVisibilityForMode();
        showStep(STEP_CAR);
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
        if (queuedBook != null) {
            menu.add(Menu.NONE, MENU_DELETE_QUEUE, Menu.NONE, BA.str(R.string.delete))
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
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
        titleView.setText(BA.str(R.string.send_to_box));

        this.selectedCarType = CarType.Sedan;
        boxesLayout.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
        carTypesLayout.setVisibility(View.VISIBLE);
        clientInformationsLayout.setVisibility(View.VISIBLE);
        selectedClientInformationLayout.setVisibility(View.GONE);
        selectedClient = null;
        // Before the washer arrives onWasherReceived() builds the services for selectedCarType.
        if (washer != null) createService(selectedCarType.getValue());
        buttonBook.setText(BA.str(R.string.send_to_box));
        setWizardMode(MODE_BOX);
    }


    @OnClick(R.id.Other)
    public void onOtherServicesButtonClick() {
        onCloseClientRequested();
        this.selectedCarType = CarType.Other;
        boxesLayout.setVisibility(View.GONE);
        searchLayout.setVisibility(View.GONE);
        carTypesLayout.setVisibility(View.GONE);
        clientInformationsLayout.setVisibility(View.GONE);
        selectedClientInformationLayout.setVisibility(View.GONE);
        selectedClient = null;
        // Before the washer arrives onWasherReceived() builds the services for selectedCarType.
        if (washer != null) createService(selectedCarType.getValue());
        buttonToBox.setSelected(false);
        buttonOtherServices.setSelected(true);
        buttonToQueue.setSelected(false);
        priceLayout.setVisibility(View.VISIBLE);
        buttonBook.setVisibility(View.VISIBLE);
        buttonQueue.setVisibility(View.GONE);
        detailLayout.setVisibility(View.VISIBLE);
        titleView.setText(BA.str(R.string.other_services2));
        buttonBook.setText(BA.str(R.string.order_verb));
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
        titleView.setText(BA.str(R.string.add_to_queue));
        boxesLayout.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
        carTypesLayout.setVisibility(View.VISIBLE);
        clientInformationsLayout.setVisibility(selectedClient == null ? View.VISIBLE : View.GONE);
        selectedClientInformationLayout.setVisibility(selectedClient == null ? View.GONE : View.VISIBLE);
        setWizardMode(MODE_QUEUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, 0);
                break;
            case MENU_DELETE_QUEUE:
                confirmDeleteQueue();
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

                    if (queuedBook != null) applyQueuedBookWasherData();
                }
            }, 200);
        }
    }

    @OnClick(R.id.btnQueue)
    public void createQueueRequest() {
        carNo.setError(null);
        closeKeyboard();
        String сarNoStr = carNo.getText().toString();
        String phoneStr = mobile.getText().toString();
        phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

        if (selectedClient == null) {
            if (TextUtils.isEmpty(сarNoStr)) {
                ToastUtil.displayAtTop(this, BA.str(R.string.enter_car_plate));
                if (carNo.hasFocus())
                    carNo.setError(null);
                else
                    carNo.setError("");
                titleView.requestFocus();
                return;
            }
            request.setCarMark(carMark.getText().toString());
        } else {
            // The plate/phone inputs are hidden once a client is chosen: take them from the client.
            сarNoStr = selectedClient.getCarNo();
            if (!TextUtils.isEmpty(selectedClient.getPhone()) && selectedClient.getPhone().length() > 3) {
                phoneStr = selectedClient.getPhone().substring(2);
            }
            request.setCarMark(selectedClient.getCarMark());
        }

        request.setCarNo(сarNoStr);
        request.setMobile(TextUtils.isEmpty(phoneStr) ? "" : "+7" + phoneStr);
        request.setClientKey(сarNoStr);
        String nameStr = selectedClient != null ? selectedClient.getName() : clientName.getText().toString();
        if (!TextUtils.isEmpty(nameStr) && !TextUtils.isEmpty(nameStr.trim()))
            request.setClientKey(сarNoStr.trim() + " (" + nameStr.trim() + ")");
            request.setTime(Instant.now());
        request.setCarwash(URL_PREFIX + washer.getId());
        request.setOffline(true);
        request.setQueued(true);
        request.setDirectWash(false);
        // Queued without a box: drop one left over from a failed "send to box" attempt.
        request.setBoxId(null);
        // Kept so the queued car opens with its car type when it is sent to a box later.
        if (selectedCarType != null && selectedCarType != CarType.Unknown)
            request.setCarType(selectedCarType.getValue());
        request.setPaid(false); // paid when it is finished (PaymentFlow)
        request.setNocash(false);
        request.setPriceDetails(new ArrayList<BookInfo.PriceDetail>());
        pendingSale = false;
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
        carNo.setError(null);
        closeKeyboard();

        if (selectedCarType != CarType.Other) {
            String сarNoStr = carNo.getText().toString();
            String phoneStr = mobile.getText().toString();
            phoneStr = phoneStr.replaceAll("\\+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("-", "").replaceAll(" ", "");

            if (selectedClient == null || queuedBook != null) {
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

            if (selectedCarType == null) {
                ToastUtil.displayAtTop(this, BA.str(R.string.select_car));
                return;
            }

            if (!(request.getServices() != null && request.getServices().size() > 0) && !(request.getGroupServices() != null && request.getGroupServices().size() > 0)) {
                ToastUtil.displayAtTop(this, BA.str(R.string.select_services));
                return;
            }

            if (TextUtils.isEmpty(boxId)) {
                ToastUtil.displayAtTop(this, BA.str(R.string.select_box));
                return;
            }

            request.setCarNo(сarNoStr);
            request.setMobile(TextUtils.isEmpty(phoneStr) ? "" : "+7" + phoneStr);
            request.setClientKey(сarNoStr);
            String nameStr = selectedClient != null && queuedBook == null ? selectedClient.getName() : clientName.getText().toString();
            if (!TextUtils.isEmpty(nameStr) && !TextUtils.isEmpty(nameStr.trim()))
                request.setClientKey(сarNoStr.trim() + " (" + nameStr.trim() + ")");
            request.setCarType(selectedCarType.getValue());
            request.setBoxId(boxId);
                  request.setTime(Instant.now());
            request.setCarwash(URL_PREFIX + washer.getId());
            request.setOffline(true);
            request.setQueued(true);
            request.setDirectWash(true);
            // Paid when it is finished: "Завершить" opens the payment window (PaymentFlow).
            request.setPaid(false);
            request.setNocash(false);
            request.setPriceDetails(new ArrayList<BookInfo.PriceDetail>());
            pendingSale = false;

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
                ToastUtil.displayAtTop(this, BA.str(R.string.select_services));
                return;
            }

            request.setCarNo("");
            request.setMobile("");
            request.setClientKey(BA.str(R.string.shop));
            request.setCarType(selectedCarType.getValue());
                  request.setTime(Instant.now());
            request.setCarwash(URL_PREFIX + washer.getId());
            request.setOffline(true);
            request.setQueued(true);
            request.setDirectWash(true);
            // A sale is paid right after it is created (onAdminBookResponseReceived).
            request.setPaid(false);
            request.setNocash(false);
            request.setPriceDetails(new ArrayList<BookInfo.PriceDetail>());
            pendingSale = true;
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

        if (queuedBook != null) {
            request.setId(queuedBook.getId());
            BA.getEventBus().post(new AdminStartQueueRequestEvent(request));
        } else {
            BA.getEventBus().post(new AdminBookRequestEvent(request));
        }
        setWaitScreen(true);
    }

    @Subscribe
    public void onAdminBookResponseReceived(AdminBookResponseEvent event) {
        setWaitScreen(false);
        if (event.getResult() != null) {
            if (BaseAssist.isSuccess(event.getResult())) {
                BookResponse created = event.getResult().getResponse();
                if (pendingSale && created != null && !TextUtils.isEmpty(created.getId())) {
                    // A shop sale is paid at once: the payment window, then it is finished. Closing
                    // the window leaves the sale open and unpaid in the orders.
                    PaymentFlow.payNewSale(this, created.getId(), washer, () -> {
                        finish();
                        overridePendingTransitionWithCommonCloseTransition();
                    });
                    return;
                }
                finish();
                overridePendingTransitionWithCommonCloseTransition();
            } else
                ToastUtil.display(this, event.getResult().getMessage());
        } else {
            ToastUtil.display(this, BA.str(R.string.err_add_queue));
        }
    }

    @OnClick(R.id.more)
    public void moreFieldsClicked() {
        if (detailLayout.getVisibility() == View.GONE) {
            detailLayout.setVisibility(View.VISIBLE);
            moreButton.setText(BA.str(R.string.additional));
            moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up_white_24dp, 0);
            buttonBook.setEnabled(true);
        } else {
            detailLayout.setVisibility(View.GONE);
            moreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down_white_24dp, 0);
            moreButton.setText(BA.str(R.string.additional));
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
        request.setCompanyClient(null);

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
        // While a shift is open with a crew, the crew (plus anyone already picked) is offered (docs/shift.md).
        ArrayList<String> chosen = new ArrayList<String>();
        if (request != null && request.getStaffs() != null) chosen.addAll(request.getStaffs());
        items = ShiftCrew.filter(gridStaffs, washer != null ? washer.getId() : null, items, chosen, () -> createStaffGrid(washer));
        staffGridAdapter = new StaffGridAdapter(this, items);
        if (request != null && request.getStaffs() != null && !request.getStaffs().isEmpty()) ShiftCrew.reselect(staffGridAdapter, request.getStaffs());
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
                // Don't auto-fill the cash field with the full price — leave the
                // payment amounts at 0 so the price difference shows the amount
                // still owed instead of always reading 0.
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
                // Leave payment amounts at 0 so the difference reflects the
                // amount still owed (see note above).
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
    public void onFocusChangeCarNo(boolean focus) {
        if (focus) {
            carNo.setError(null);
        }
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
                    ToastUtil.display(this, BA.str(R.string.client_found));
                    setData(client);
                }
            } else {
                ToastUtil.display(this, BA.str(R.string.client_not_found));
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
                    ToastUtil.display(this, BA.str(R.string.car_number_not_selected));
                }
            } else {
                ToastUtil.display(this, BA.str(R.string.err_process_car_number));
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
            // When a client is selected, the car type comes from that client's
            // card and can't be changed here — warn instead of switching.
            if (selectedClient != null && !settingCarTypeFromClient) {
                ToastUtil.displayAtTop(AdminAddQueuedBookActivity.this,
                        BA.str(R.string.cant_change_cartype_here));
                return;
            }
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

}

