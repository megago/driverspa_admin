package com.driverspa.admin.fragment;

import static com.driverspa.util.Constants.APPROVED;
import static com.driverspa.util.Constants.CANCELED;
import static com.driverspa.util.Constants.DAY_MINUTES;
import static com.driverspa.util.Constants.FOR_DATE;
import static com.driverspa.util.Constants.GRID_BOOK_ID;
import static com.driverspa.util.Constants.GRID_BOX_ID;
import static com.driverspa.util.Constants.GRID_EXPIRED;
import static com.driverspa.util.Constants.GRID_ID;
import static com.driverspa.util.Constants.GRID_STATUS;
import static com.driverspa.util.Constants.GRID_TITLE;
import static com.driverspa.util.Constants.PENDING;
import static com.driverspa.util.Constants.QUEUED_APPROVED;
import static com.driverspa.util.Constants.QUEUED_REJECTED;
import static com.driverspa.util.Constants.REJECTED;
import static com.driverspa.util.Constants.SLOT_AVAILABLE;
import static com.driverspa.util.Constants.SLOT_BUSY;
import static com.driverspa.util.Constants.SLOT_NOT_AVAILABLE;
import static com.driverspa.util.Constants.TEST;
import static com.driverspa.util.Constants.TODAY;
import static com.driverspa.util.Constants.TOMORROW;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import com.driverspa.adapter.AdminWasherTimeTableAdapter;
import com.driverspa.adapter.TabBarAdapter;
import com.driverspa.assist.BaseAssist;
import com.google.android.material.appbar.AppBarLayout;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import com.driverspa.BA;
import com.driverspa.R;
import com.driverspa.fragment.DatePickerFragment;
import com.driverspa.listener.DatePickerDateSetListener;
import com.driverspa.model.BookInfo;
import com.driverspa.model.Box;
import com.driverspa.model.PushData;
import com.driverspa.model.Washer;
import com.driverspa.model.api.request.BookingRequest;
import com.driverspa.model.api.response.WasherTimeTableResponse;
import com.driverspa.model.api.response.WasherTimeTableResponse.BoxItem;
import com.driverspa.util.Functions;
import com.driverspa.util.ToastUtil;
import com.driverspa.util.otto.NewBookingPushResponseEvent;
import com.driverspa.util.otto.SwipeActionEvent;
import com.driverspa.util.otto.ws.AdminBookApproveRequestEvent;
import com.driverspa.util.otto.ws.AdminBookApproveResponseEvent;
import com.driverspa.util.otto.ws.AdminBookCancelRequestEvent;
import com.driverspa.util.otto.ws.AdminBookCancelResponseEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientRequestEvent;
import com.driverspa.util.otto.ws.AdminBookNoClientResponseEvent;
import com.driverspa.util.otto.ws.AdminBookStopRequestEvent;
import com.driverspa.util.otto.ws.AdminBookStopResponseEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminFinishQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidRequestEvent;
import com.driverspa.util.otto.ws.AdminMarkAsClientPaidResponseEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminPostponeQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueRequestEvent;
import com.driverspa.util.otto.ws.AdminRejectQueueResponseEvent;
import com.driverspa.util.otto.ws.AdminWasherInfoRequestEvent;
import com.driverspa.util.otto.ws.WasherGetResponseEvent;
import com.driverspa.util.otto.ws.WasherTimeTableBookRequestEvent;
import com.driverspa.util.otto.ws.WasherTimeTableBookResponseEvent;
import com.driverspa.view.ExpandableGridView;
import com.driverspa.view.NDSpinner;

public class AdminTimeTableFragment extends AdminBaseFragment implements View.OnClickListener,DatePickerDateSetListener {

	@Override
	public void onClick(View v) {
		loadTimeTableData();
	}

	public interface ActivityActions{
		public void openAddBook(String boxName, String boxId, Long gridTime, String gridTitle, String bookDay);
		public void openBookInfo(String bookId);
		public void editEntry(BookInfo bookInfo);
	}

	@BindView(R.id.txtNoTime)
	TextView washerNoTime;
	@BindView(R.id.btnRepeat)
	Button buttonRepeat;
	@BindView(R.id.gridView)
	ExpandableGridView gridview;
	@BindView(R.id.btnToday)
	TextView buttonToday;
	@BindView(R.id.btnTomorrow)
	TextView buttonTomorrow;
	@BindView (R.id.tabView)
	Gallery tabView;
	@BindView(R.id.scrollView)
	NestedScrollView scrollView;
	@BindView(R.id.progressBar)
	ProgressBar progressBar;

	@BindView(R.id.forwardButton)
	View forwardButton;
	@BindView(R.id.rewardButton)
	View rewardButton;

	private List<HashMap<String,String>> gridList;
	private List<String> joinSlots;
	private AdminWasherTimeTableAdapter adapter;
	private String selectedDay;
	private String boxId;
	private Washer washer;
	WasherTimeTableResponse timeTable;
	//Key is the book id as string
	HashMap<String,BookInfo> bookingObjects;
	//Key is the slot time as integer
	HashMap<Integer,List<BookInfo>> bookingGridObjects;
	private ActivityActions activityActions;
	HashMap<String,PushData> bookingPushData;
	int index;
	String boxName;
	Long gridTime;
	String gridTitle;
	int selectedBox;
	int boxesCount;
	java.time.Instant serverTime;
	int scrollViewMax = 0;
	boolean tabletSize;
	boolean tabletBigSize;
	int currentTimeInterval = 30;
	Long currentTime = -1L;

	boolean selectionWithoutUpdate = false;
	NDSpinner spinner;
	ProgressBar actionBarProgress;
	Toolbar toolbar;
	AppBarLayout appBarLayout;
	View actionBarRefreshButton;
	boolean visible = false;
	View addQueueButton;
	View addBookButton;
	TextView titleView;

	boolean loading = false;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.systemDefault());

	private Handler handler;
	long lastUpdateTime = System.currentTimeMillis();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Long currentTime = System.currentTimeMillis();
			long diffMinutes = (currentTime - lastUpdateTime) / (60 * 1000);
			if(diffMinutes > 1){
				loadTimeTableDataWithoutLoader();
			}
			handler.postDelayed(runnable, 30000);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_admin_timetable, container,false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ButterKnife.bind(this, view);
		selectedDay = TODAY;
		buttonToday.setSelected(true);
		handler = new Handler();
		toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_actionbar);
		appBarLayout = (AppBarLayout)getActivity().findViewById(R.id.appbar);
		spinner = (NDSpinner) toolbar.findViewById(R.id.spinner);
		actionBarProgress = (ProgressBar) toolbar.findViewById(R.id.progress);
		actionBarRefreshButton = toolbar.findViewById(R.id.refresh);
		addQueueButton = getActivity().findViewById(R.id.button_queue_book);
		addBookButton = getActivity().findViewById(R.id.button_add_book);
		titleView = (TextView) toolbar.findViewById(R.id.action_bar_title);
		if (currentTimeInterval == 30) {
			titleView.setText(BA.str(R.string.min_30m));
		} else {
			titleView.setText(BA.str(R.string.min_15m));
		}

		addBookButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBookButtonClicked();
			}
		});

		ViewTreeObserver vto = scrollView.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				scrollViewMax = scrollView.getChildAt(0).getMeasuredHeight() - scrollView.getMaxScrollAmount()*2;
			}
		});

		ViewGroup.LayoutParams layoutParams = gridview.getLayoutParams();
		WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth()-(int) Functions.dipToPixels(getActivity(), 37);
		layoutParams.width = width; //this is in pixels
		gridview.setLayoutParams(layoutParams);
		tabletSize = getResources().getBoolean(R.bool.isTablet);
		tabletBigSize = getResources().getBoolean(R.bool.isBigTablet);
		if(!tabletSize)
			gridview.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(getActivity(), 40)) / 4);
		else if(!tabletBigSize)
			gridview.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(getActivity(), 40)) / 6);
		else
			gridview.setColumnWidth((display.getWidth() - (int) Functions.dipToPixels(getActivity(), 40)) / 8);

		gridview.setVisibility(View.GONE);
	}

	@Subscribe
	public void onWasherReceived(WasherGetResponseEvent event) {
		if(event.getWasher() != null) {
			washer = event.getWasher();
			final ArrayList<Box> boxSettingses =  Functions.filterDisableddBoxes(washer.getBoxSettings());
			final TabBarAdapter tabViewAdapter = new TabBarAdapter(getActivity(), boxSettingses);
			boxesCount = tabViewAdapter.getCount();
			navigationButtonVisibility();
			tabView.setAdapter(tabViewAdapter);
			if (TextUtils.isEmpty(boxId)) {
				boxId = boxSettingses.get(0).getUid();
				boxName = boxSettingses.get(0).getBoxName();
			} else {
				if (tabViewAdapter.getCount() > selectedBox)
					tabView.setSelection(selectedBox);
			}
			tabView.setCallbackDuringFling(false);
			tabView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> arg0, View arg1,final int position, long arg3) {
					boxId = boxSettingses.get(position).getUid();
					boxName = boxSettingses.get(position).getBoxName();
					tabViewAdapter.setSelectedTab(position);
					tabViewAdapter.notifyDataSetChanged();
					if (timeTable != null) loadBoxTimeTable();
					selectedBox = position;
					navigationButtonVisibility();
				}
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

			loadTimeTableDataWithoutLoader();
		}
		else
			BA.getEventBus().post(new AdminWasherInfoRequestEvent(""));
	}

	private void navigationButtonVisibility(){
		if(boxesCount == 1){
			forwardButton.setVisibility(View.GONE);
		}
		else{
			forwardButton.setVisibility(View.VISIBLE);
		}
		if(boxesCount == 1){
			rewardButton.setVisibility(View.GONE);
		}
		else{
			rewardButton.setVisibility(View.VISIBLE);
		}
	}

	@OnClick(R.id.rewardButton)
	public void rewardButtonClicked(){
		if(selectedBox > 0) {
			selectedBox--;
			tabView.setSelection(selectedBox);
		}
	}

	@OnClick(R.id.forwardButton)
	public void forardButtonClicked(){
		if(selectedBox < boxesCount-1) {
			selectedBox++;
			tabView.setSelection(selectedBox);
		}
	}

	@Subscribe
	public void onWasherTimeTableReceived(WasherTimeTableBookResponseEvent event){
		loading = false;
		addBookButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		tabView.setVisibility(View.VISIBLE);
		if(event.getWasherTimeTable() != null){
			timeTable = event.getWasherTimeTable();
			if(timeTable.getServerTime()!=null) {
				serverTime = timeTable.getServerTime();
			}
			if(timeTable.getResult().getBookingObjects() != null) {
				bookingObjects = timeTable.getResult().getBookingObjects();
				bookingGridObjects = getGridBooks(bookingObjects);
			}
			loadBoxTimeTable();
		}
		else{
			washerNoTime.setVisibility(View.VISIBLE);
			buttonRepeat.setVisibility(View.VISIBLE);
		}
	}

	public void loadTimeTableData(){
		if(!loading) {
			loading = true;
			lastUpdateTime = System.currentTimeMillis();
			gridview.setVisibility(View.GONE);
			tabView.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			washerNoTime.setVisibility(View.GONE);
			buttonRepeat.setVisibility(View.GONE);
			BA.getEventBus().post(new WasherTimeTableBookRequestEvent(washer.getId(), selectedDay, currentTime));
		}
	}

	public void loadTimeTableDataWithoutLoader(){
		if(!loading) {
			loading = true;
			lastUpdateTime = System.currentTimeMillis();
			BA.getEventBus().post(new WasherTimeTableBookRequestEvent(washer.getId(), selectedDay, currentTime));
		}
	}

	@OnClick(R.id.btnToday)
	public void onTodayButtonClick(){
		selectedDay = TODAY;
		buttonToday.setSelected(true);
		buttonTomorrow.setSelected(false);
		loadTimeTableData();
	}

	@OnClick(R.id.btnTomorrow)
	public void onTomorrowButtonClick(){
		selectedDay = TOMORROW;
		buttonToday.setSelected(false);
		buttonTomorrow.setSelected(true);
		loadTimeTableData();
	}

	public void onDateClick(){
		DatePickerFragment picker =  DatePickerFragment.newInstance(currentTime*1000);
		picker.setDatePickerDateSetListener(this);
		picker.show(getChildFragmentManager(), "datePicker");
	}

	@Override
	public void onDatePickerDataset(Long time,String type) {
		currentTime = time/1000;
		selectedDay = FOR_DATE;
		createActionBarSpinner();
		loadTimeTableData();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityActions = (ActivityActions) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		activityActions = null;
	}

	@OnClick(R.id.btnRepeat)
	public void onButtonRepeatClicked(){
		loadTimeTableData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		loading = false;
		BA.getEventBus().register(this);
		handler.postDelayed(runnable, 30000);
	}

	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(runnable);
		BA.getEventBus().unregister(this);
	}

	public void loadBoxTimeTable(){
		addBookButton.setVisibility(View.GONE);
		joinSlots = new ArrayList<String>();

		HashMap<String, BoxItem> boxMap = timeTable.getResult().getBoxes();
		if(boxMap != null && boxMap.size() > 0){
			BoxItem selectedBox = boxMap.get(boxId);
			if (selectedBox != null && selectedBox.getSlots() != null) {
				for (int i = 0; i < selectedBox.getSlots().size(); i++) {
					String item = selectedBox.getSlots().get(i);
					if(joinSlots.size() >= i+1){
						if(item.equals(SLOT_AVAILABLE) || !item.equals(SLOT_NOT_AVAILABLE)){
							joinSlots.set(i,item);
						}
					}
					else {
						joinSlots.add(item);
					}
				}
			}
			createGridList(washer,joinSlots);
		}
		else{
			washerNoTime.setVisibility(View.VISIBLE);
			buttonRepeat.setVisibility(View.VISIBLE);
		}

//		  final Animation fadeIn = new AlphaAnimation(0, 1);
//		  fadeIn.setDuration(500);
//		  gridview.startAnimation(fadeIn);
	}

	public void createGridList(final Washer washer, List<String> slots){
		gridList = new ArrayList<HashMap<String,String>>();
		Calendar calendar = Calendar.getInstance();

		if(serverTime != null) calendar.setTimeInMillis(serverTime.toEpochMilli());

		int currHour = calendar.get(Calendar.HOUR_OF_DAY);
		int currMin = calendar.get(Calendar.MINUTE);

		int todayStartTime = currHour*60+currMin;
		int startTime = 0, endTime = 0;

		if(washer.getSchedule().getToday() != null && washer.getSchedule().getToday().size() > 0){
			startTime = 0;//washer.getSchedule().getToday().get(0).getFrom();
			endTime = 1439;//washer.getSchedule().getToday().get(0).getTo();

			String lastBookId = "";
			int gridIndex = index =  0, time = 0;

			for(int i = 0; i < (DAY_MINUTES)/currentTimeInterval; i++){
				if(time >= startTime && time <= endTime){

					HashMap<String,String> gridMap = new HashMap<String,String>();
					HashMap<String,String> gridMapAdditional = new HashMap<String,String>();
					boolean slotAddExist = false;
					gridMap.put(GRID_ID, Integer.toString(time));
					gridMap.put(GRID_BOOK_ID, null);
					gridMap.put(GRID_EXPIRED, Boolean.FALSE.toString());
					gridMap.put(GRID_TITLE, String.format("%02d", time/60)+":"+String.format("%02d", time%60));
					gridMap.put(GRID_STATUS, SLOT_AVAILABLE);

					List<BookInfo> gridBooks = bookingGridObjects.get(time);
					//Sort the grid items with time
					if(gridBooks!=null && gridBooks.size() > 0) {
						Collections.sort(gridBooks, new Comparator<BookInfo>() {
							@Override
							public int compare(BookInfo lhs, BookInfo rhs) {
											if (lhs.getTime().toEpochMilli() > rhs.getTime().toEpochMilli())
									return 1;
								else
									return -1;
							}
						});
					}

					if(gridBooks != null && gridBooks.size() > 0){
						for(int k = 0; k < gridBooks.size(); k++){
							//More than 0 books at the grid
							BookInfo gridBook = gridBooks.get(k);
							if(gridBook.getBoxId() != null && boxId != null && gridBook.getBoxId().equals(boxId)) {
								ZonedDateTime gridBookTime = gridBook.getTime().atZone(ZoneId.systemDefault());
								int hour = gridBookTime.getHour();
								int minute = gridBookTime.getMinute();
								HashMap<String, String> gridBookMap = new HashMap<String, String>();
								gridBookMap.put(GRID_ID, Integer.toString(time));
								gridBookMap.put(GRID_TITLE, String.format("%02d", hour) + ":" + String.format("%02d", minute));
								gridBookMap.put(GRID_STATUS, SLOT_BUSY);
								gridBookMap.put(GRID_BOOK_ID, gridBook.getId());
								gridBookMap.put(GRID_BOX_ID, gridBook.getId());
								if(index == 0 && selectedDay.equals(TODAY) && time >= todayStartTime) {
									index = gridIndex;
								}
								else
								if(selectedDay.equals(TODAY) && time < todayStartTime){
									gridBookMap.put(GRID_EXPIRED, Boolean.TRUE.toString());
								}
								gridList.add(gridBookMap);
								gridIndex++;
								ZonedDateTime gridEndBookTime = gridBook.getEndTime().atZone(ZoneId.systemDefault());
								int endHour = gridEndBookTime.getHour();
								int endMinute = gridEndBookTime.getMinute();

								while (endHour * 60 + endMinute > time+currentTimeInterval && bookingGridObjects.get(time+currentTimeInterval) == null) {
									time += currentTimeInterval;
								}
							}
							else{
								if(index == 0 && selectedDay.equals(TODAY) && time >= todayStartTime) {
									index = gridIndex;
								}
								else
								if(selectedDay.equals(TODAY) && time < todayStartTime){
									gridMap.put(GRID_EXPIRED, Boolean.TRUE.toString());
								}

								gridIndex++;
								gridList.add(gridMap);
							}
						}
					}
					else {
						if(index == 0 && selectedDay.equals(TODAY) && time >= todayStartTime) {
							index = gridIndex;
						}
						else
						if(selectedDay.equals(TODAY) && time < todayStartTime){
							gridMap.put(GRID_EXPIRED, Boolean.TRUE.toString());
						}

						gridIndex++;
						gridList.add(gridMap);
					}
				}
				time += currentTimeInterval;
			}
		}

		if(gridList.size() > 0){
			gridview.setVisibility(View.VISIBLE);
			adapter = new AdminWasherTimeTableAdapter(getActivity(),gridList,bookingObjects,washer);
			gridview.setAdapter(adapter);
			gridview.setExpanded(true);
			adapter.notifyDataSetChanged();
			gridview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					addBookButton.setVisibility(View.GONE);

					if(gridList.get(position).get(GRID_STATUS).equals(SLOT_BUSY)){
						final BookInfo book = bookingObjects.get(gridList.get(position).get(GRID_BOOK_ID));
						if(!book.isQueued()) {
							//Bookings
							if(book.getStatus().equals(APPROVED)) {
								AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
								b.setItems(R.array.admin_approved_book_options, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										switch (i) {
											case 0:
												setWaitScreen(true);
												BA.getEventBus().post(new AdminBookStopRequestEvent(book.getId()));
												break;
											case 1:
												setWaitScreen(true);
												BA.getEventBus().post(new AdminBookNoClientRequestEvent(book.getId()));
												break;
											case 2:
												setWaitScreen(true);
												BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
												break;
											case 3:
												activityActions.editEntry(book);
												break;
											case 4:
												activityActions.openBookInfo(book.getId());
												break;
										}
									}
								});
								b.setTitle(BA.str(R.string.service_rendered_to)+book.getClientKey()+"?");
								b.show();
							}
							else if(book.getStatus().equals(PENDING)){
								AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
								b.setItems(R.array.admin_pending_book_options, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										switch (i) {
											case 0:
												setWaitScreen(true);
												BA.getEventBus().post(new AdminBookApproveRequestEvent(book.getId()));
												break;
											case 1:
												setWaitScreen(true);
												BA.getEventBus().post(new AdminBookCancelRequestEvent(book.getId()));
												break;
											case 2:
												activityActions.openBookInfo(book.getId());
												break;
										}
									}
								});
								b.setTitle(BA.str(R.string.op_booking_client)+book.getClientKey()+"");
								b.show();
							}
							else {
								activityActions.openBookInfo(book.getId());
							}
						}
						else{
							//Queued booking
							if(book.getStatus().equals(QUEUED_APPROVED)){
								AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
								if(book.getPaid()) {
									b.setItems(R.array.admin_approved_queue_options, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											switch (i) {
												case 0:
													setWaitScreen(true);
													BookingRequest request = new BookingRequest();
													request.setId(book.getId());
													request.setDuration(10);
													BA.getEventBus().post(new AdminPostponeQueueRequestEvent(request));
													break;
												case 1:
													setWaitScreen(true);
													request = new BookingRequest();
													request.setId(book.getId());
													BA.getEventBus().post(new AdminFinishQueueRequestEvent(request));
													break;
												case 2:
													setWaitScreen(true);
													request = new BookingRequest();
													request.setId(book.getId());
													BA.getEventBus().post(new AdminRejectQueueRequestEvent(request));
													break;
												case 3:
													activityActions.editEntry(book);
													break;
												case 4:
													activityActions.openBookInfo(book.getId());
													break;
											}
										}
									});
								}
								else{
									b.setItems(R.array.admin_approved_queue_not_paid_options, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											switch (i) {
												case 0:
													setWaitScreen(true);
													BookingRequest request = new BookingRequest();
													request.setId(book.getId());
													request.setPaid(true);
													BA.getEventBus().post(new AdminMarkAsClientPaidRequestEvent(request));
													break;
												case 1:
													setWaitScreen(true);
													request = new BookingRequest();
													request.setId(book.getId());
													request.setDuration(10);
													BA.getEventBus().post(new AdminPostponeQueueRequestEvent(request));
													break;
												case 2:
													setWaitScreen(true);
													request = new BookingRequest();
													request.setId(book.getId());
													BA.getEventBus().post(new AdminFinishQueueRequestEvent(request));
													break;
												case 3:
													setWaitScreen(true);
													request = new BookingRequest();
													request.setId(book.getId());
													BA.getEventBus().post(new AdminRejectQueueRequestEvent(request));
													break;
												case 4:
													activityActions.editEntry(book);
													break;
												case 5:
													activityActions.openBookInfo(book.getId());
													break;
											}
										}
									});

								}
								b.setTitle(BA.str(R.string.op_client)+book.getClientKey().replaceAll("\\(","").replaceAll("\\)","")+"");
								b.show();
							}
							else{
								if(!book.getPaid()) {
									AlertDialog.Builder b = new MaterialAlertDialogBuilder(getActivity());
									b.setItems(R.array.admin_not_paid_options, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											switch (i) {
												case 0:
													setWaitScreen(true);
													BookingRequest request = new BookingRequest();
													request.setId(book.getId());
													request.setPaid(true);
													BA.getEventBus().post(new AdminMarkAsClientPaidRequestEvent(request));
													break;
												case 1:
													activityActions.openBookInfo(book.getId());
													break;
											}
										}
									});
									b.setTitle(BA.str(R.string.op_booking_client)+book.getClientKey()+"");
									b.show();
								}
								else{
									activityActions.openBookInfo(book.getId());
								}

							}
						}
					}
					else if(gridList.get(position).get(GRID_STATUS).equals(SLOT_AVAILABLE)){
						HashMap<String,String> selectedTimeMap = gridList.get(position);
						int hour = Integer.parseInt(selectedTimeMap.get(GRID_ID))/60;
						int minut = Integer.parseInt(selectedTimeMap.get(GRID_ID))%60;
						Calendar cal = Calendar.getInstance();
						if(selectedDay.equals(TOMORROW)) cal.add(Calendar.DATE, 1);
						cal.set(Calendar.HOUR_OF_DAY,hour);
						cal.set(Calendar.MINUTE,minut);
						cal.set(Calendar.SECOND,0);
						cal.set(Calendar.MILLISECOND,0);
						long bookDate = cal.getTimeInMillis();
						if(!selectedDay.equals(FOR_DATE) && System.currentTimeMillis() <= bookDate) {
							adapter.setSelectedPosition(position);
							if((adapter.getSelectedView() != null && v != adapter.getSelectedView())||adapter.getSelectedView() == null) {
								adapter.setSelectedView(v);
								v.setSelected(true);
								addBookButton.setVisibility(View.VISIBLE);
							}
							else{
								if (adapter.getSelectedView() != null) adapter.getSelectedView().setSelected(false);
								adapter.setSelectedView(null);
								v.setSelected(false);
								addBookButton.setVisibility(View.GONE);
							}
							gridTime = bookDate;
							gridTitle = selectedTimeMap.get(GRID_TITLE);
						}
					}
				}
			});

			if(selectedDay.equals(TODAY) && index < gridList.size()) {
				WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
				Display display = wm.getDefaultDisplay();
				int width = display.getWidth()-(int) Functions.dipToPixels(getActivity(), 37);
				int divider = 4;
				if(tabletSize) divider = 6;
				int offset = (index/divider)*(width/divider)-width/divider;
				if(offset < 0){
					offset = 0;
				}
				else if(offset > gridview.getHeight()){
					offset = gridview.getHeight();
				}
				final int scrollTo = offset;
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						scrollView.smoothScrollTo(0,scrollTo);
					}
				}, 100);
			}
		}
		else{
			washerNoTime.setVisibility(View.VISIBLE);
			buttonRepeat.setVisibility(View.VISIBLE);
		}
	}

	public void onBookButtonClicked(){
		if(washer != null && (washer.getStatus().equals(APPROVED) || washer.getStatus().equals(TEST))){
			activityActions.openAddBook(boxName, boxId,gridTime, gridTitle,selectedDay);
		}
		else
			ToastUtil.display(BA.getContext(),BA.str(R.string.wash_not_activated_short));
	}

	@Subscribe
	public void onBookingPushReceived(NewBookingPushResponseEvent event){
		if (event != null && event.getPush() != null) {
			bookingPushData = event.getPush().getData();
			if(washer != null) {
				loadTimeTableDataWithoutLoader();
			}
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		loading = false;
		visible = isVisibleToUser;
		if(visible) {
			if(titleView != null) {
				titleView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_time, 0, 0, 0);
				titleView.setVisibility(View.VISIBLE);
				if (currentTimeInterval == 30) {
					titleView.setText(BA.str(R.string.min_30m));
				} else {
					titleView.setText(BA.str(R.string.min_15m));
				}
				titleView.setCompoundDrawablePadding((int)Functions.dipToPixels(getActivity(),5f));
				titleView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (currentTimeInterval == 30) {
							currentTimeInterval = 15;
							titleView.setText(BA.str(R.string.min_15m));
							loadBoxTimeTable();
						} else {
							currentTimeInterval = 30;
							titleView.setText(BA.str(R.string.min_30m));
							loadBoxTimeTable();
						}
					}
				});
			}

			if(appBarLayout != null)
				appBarLayout.setExpanded(true, true);
			if(spinner != null)
				createActionBarSpinner();
			if(addBookButton != null) {
				if(!selectedDay.equals(FOR_DATE))
					loadTimeTableDataWithoutLoader();
				spinner.setVisibility(View.VISIBLE);
				addQueueButton.setVisibility(View.GONE);
				actionBarRefreshButton.setOnClickListener(this);
				actionBarRefreshButton.setVisibility(View.VISIBLE);
			}
		}
		else{
			if(titleView != null) {
				titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
			if(addBookButton != null) {
				addQueueButton.setVisibility(View.GONE);
				addBookButton.setVisibility(View.GONE);
			}
		}
	}

	private void createActionBarSpinner(){
		if(spinner != null) {
			String[] valuesArray = new String[]{BA.str(R.string.today), BA.str(R.string.tomorrow), BA.str(R.string.orders_on_date)};
			if(currentTime != -1) {
				String dateStr = DATE_FORMATTER.format(Instant.ofEpochSecond(currentTime));
				valuesArray = new String[]{BA.str(R.string.today), BA.str(R.string.tomorrow), BA.str(R.string.orders_on_date_pre)+dateStr};
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.navigation_spinner_item, valuesArray);
			adapter.setDropDownViewResource(R.layout.home_spinner_dropdown_item);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (!selectionWithoutUpdate){
						switch (pos) {
							case 0:
								onTodayButtonClick();
								break;
							case 1:
								onTomorrowButtonClick();
								break;
							case 2:
								onDateClick();
								break;
						}
					}
					if(view != null)
						selectionWithoutUpdate = false;
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
			spinner.setAdapter(adapter);

			if (selectedDay.equals(TODAY)) {
				selectionWithoutUpdate = true;
				spinner.setSelection(0);
			} else if (selectedDay.equals(TOMORROW)) {
				selectionWithoutUpdate = true;
				spinner.setSelection(1);
			}
			else if (selectedDay.equals(FOR_DATE)) {
				selectionWithoutUpdate = true;
				spinner.setSelection(2);
			}
		}
	}

	private HashMap<Integer,List<BookInfo>> getGridBooks(HashMap<String,BookInfo> bookingObjects){
		HashMap<Integer,List<BookInfo>> gridBooks = new HashMap<Integer,List<BookInfo>>();
		if(bookingObjects != null && bookingObjects.size() > 0) {
			bookingObjects.values().stream()
					.filter(book -> !book.getStatus().equals(REJECTED)
							&& !book.getStatus().equals(CANCELED)
							&& !book.getStatus().equals(QUEUED_REJECTED))
					.forEach(book -> {
						ZonedDateTime bookTime = book.getTime().atZone(ZoneId.systemDefault());
						int hour = bookTime.getHour();
						int minute = bookTime.getMinute();
						int gridTime = hour * 60 + (minute < 30 ? 0 : 30);
						List<BookInfo> books = gridBooks.get(gridTime);
						if (books != null) {
							books.add(book);
							gridBooks.put(gridTime, books);
						} else {
							books = new ArrayList<BookInfo>();
							books.add(book);
							gridBooks.put(gridTime, books);
						}
					});
		}
		return gridBooks;
	}

	@Subscribe
	public void onBookApproveReceived(AdminBookApproveResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadTimeTableDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.booking_accepted));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_accept_booking));
		}
	}

	@Subscribe
	public void onBookStopReceived(AdminBookStopResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadTimeTableDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.service_finished));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_stop_service));
		}
	}


	@Subscribe
	public void onBookNoClientReceived(AdminBookNoClientResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadTimeTableDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.order_rejected_noshow));

			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.error_typo));
		}
	}

	@Subscribe
	public void onBookCancelReceived(AdminBookCancelResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())){
				loadTimeTableDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.order_rejected_typo));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_cancel_booking));
		}
	}


	@Subscribe
	public void onAdminRejectQueueResponseReceived(AdminRejectQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
//				ToastUtil.display(this, "Успешно забронировали");
				loadTimeTableDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.queue_rejected_typo));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_add_queue));
		}
	}

	@Subscribe
	public void onAdminFinishQueueResponseReceived(AdminFinishQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				loadTimeTableDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.service_provided));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_add_queue));
		}
	}

	@Subscribe
	public void onAdminPostponeQueueResponseReceived(AdminPostponeQueueResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				loadTimeTableDataWithoutLoader();
				ToastUtil.display(BA.getContext(),BA.str(R.string.extended_10min_ok));
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.err_extend));
		}
	}

	@Subscribe
	public void onAdminMarkAsClientPaidResponseReceived(AdminMarkAsClientPaidResponseEvent event){
		setWaitScreen(false);
		if(event.getResult() != null){
			if(BaseAssist.isSuccess(event.getResult())) {
				loadTimeTableDataWithoutLoader();
			}
			else
				ToastUtil.display(getActivity(), event.getResult().getMessage());
		}
		else{
			ToastUtil.display(getActivity(), BA.str(R.string.error_word));
		}
	}


	@Subscribe
	public void SwipeDirectionEventReceived(SwipeActionEvent event) {
		if (visible && boxesCount > 1 && 1!=1) {
			Animation fadeIn = new AlphaAnimation(0, 1);
			fadeIn.setDuration(300);

			if (event.isSwipeRight()) {
				if (boxesCount > selectedBox + 1) {
//					if(appBarLayout != null)
//						appBarLayout.setExpanded(true, false);
					tabView.setSelection(selectedBox + 1, true);
					gridview.startAnimation(fadeIn);
				}
			} else {
				if (0 <= selectedBox - 1) {
					tabView.setSelection(selectedBox - 1, true);
//					if(appBarLayout != null)
//						appBarLayout.setExpanded(true, false);
					gridview.startAnimation(fadeIn);
				}
			}
		}
	}
}
